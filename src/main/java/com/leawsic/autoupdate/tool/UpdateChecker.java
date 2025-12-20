package com.leawsic.autoupdate.tool;

import com.google.gson.Gson;
import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.UpdateCheckResult;
import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.data.mod.ModInfo;
import com.leawsic.autoupdate.data.mod.RemoteModList;
import com.leawsic.autoupdate.render.ToastManager;
import com.leawsic.autoupdate.render.screen.ModUpdateSelectionScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UpdateChecker {
    private static final String MODRINTH_API = "https://api.modrinth.com/v2/project/%s/version?loaders=[\"fabric\"]&game_versions=[\"%s\"]";
    private static final String CURSEFORGE_API = "https://api.curseforge.com/v1/mods/%s/files";

    // URL to fetch the remote mod list JSON from
    private static final String REMOTE_MOD_LIST_URL = Config.getInstance().getConfigInfoFromFile(null).modListUrl;

    public UpdateChecker() {}

    /**
     * Fetch remote mod list and compare with local mods to find updates
     */
    private static CompletableFuture<UpdateCheckResult> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Fetch the remote mod list
                RemoteModList remoteModList = fetchRemoteModList();

                // Map to store mods that need updates
                List<ModInfo> modsToUpdate=new ArrayList<>();

                //todo 完善更新逻辑--支持更多更新判断模式 目前只是比较: 是否存在 & 版本是否相同
                if (remoteModList != null) {
                    for (ModInfo remoteMod : remoteModList.getModInfos()) {
                        ModInfo localMods = LocalModListManager.getInstance().getModById(remoteMod.getId());

                        // If mod exists locally and versions don't match, mark for update
                        if (localMods != null && !localMods.getVersion().equals(remoteMod.getVersion())) {
                            modsToUpdate.add(remoteMod);
                        }
                        // If mod doesn't exist locally, mark for download
                        else if (localMods == null) {
                            modsToUpdate.add(remoteMod);
                        }
                    }
                }else {
                    return UpdateCheckResult.failure();
                }

                return UpdateCheckResult.success(modsToUpdate,remoteModList.getPackVersion());
            } catch (Exception e){
                AutoUpdate.LOGGER.error("{}", e.getMessage());
                return UpdateCheckResult.failure();
            }
        });
    }

    /**
     * Fetch the remote mod list from the server
     */
    private static RemoteModList fetchRemoteModList(){
        try {
            URL url = new URL(REMOTE_MOD_LIST_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);

            if (connection.getResponseCode()==HttpURLConnection.HTTP_OK) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                Gson gson = new Gson();
                return gson.fromJson(reader, RemoteModList.class);
            }else {
                AutoUpdate.LOGGER.warn("Fail to fetch remote mods list--Bad response {}",connection.getResponseCode());
                return null;
            }
        } catch (IOException e) {
            AutoUpdate.LOGGER.warn("Error when fetching remote mods list!");
            return null;
        }
    }
    /**
     * 检查更新并自动下载缺失模组
     */
    public static CompletableFuture<UpdateCheckResult> checkForUpdatesWithDownload() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取远程模组列表
                RemoteModList remoteModList = fetchRemoteModList();
                List<ModInfo> modsToUpdate = new ArrayList<>();
                List<ModInfo> missingMods = new ArrayList<>();

                if (remoteModList != null) {
                    for (ModInfo remoteMod : remoteModList.getModInfos()) {
                        ModInfo localMod = LocalModListManager.getInstance().getModById(remoteMod.getId());

                        if (localMod != null && !localMod.getVersion().equals(remoteMod.getVersion())) {
                            modsToUpdate.add(remoteMod);
                        } else if (localMod == null) {
                            missingMods.add(remoteMod);
                        }
                    }

                    // 如果配置了自动下载缺失模组，则下载
                    if (Config.getInstance().getConfigInfoFromFile(null).autoDownloadMissingMod && !missingMods.isEmpty()) {
                        AutoUpdate.LOGGER.info("Auto-downloading {} missing mods", missingMods.size());
                        ModDownloadManager downloadManager = new ModDownloadManager();
                        
                        CompletableFuture<Boolean> downloadFuture = downloadManager.downloadMissingMods(missingMods);
                        boolean downloadSuccess = downloadFuture.get(); // 等待下载完成
                        
                        if (downloadSuccess) {
                            AutoUpdate.LOGGER.info("Auto-download completed successfully");
                            // 重新加载本地模组列表以反映新下载的模组
                            LocalModListManager.getInstance().loadModInfos();
                        } else {
                            AutoUpdate.LOGGER.warn("Auto-download completed with some failures");
                        }
                    }
                } else {
                    return UpdateCheckResult.failure();
                }

                return UpdateCheckResult.success(modsToUpdate, remoteModList.getPackVersion());
            } catch (Exception e) {
                AutoUpdate.LOGGER.error("Error during update check with download: {}", e.getMessage());
                return UpdateCheckResult.failure();
            }
        });
    }

    public static void checkUpdate(MinecraftClient client, String updateScreenTranslateKey) {
        try {
            UpdateCheckResult result = UpdateChecker.checkForUpdates().get();
            if (result.success() && !result.modsToUpdate().isEmpty()) {
                // 打开模组更新选择界面
                client.setScreen(new ModUpdateSelectionScreen(
                        Text.translatable(AutoUpdate.MOD_ID + updateScreenTranslateKey + ".selectionTitle"),
                        client.currentScreen,
                        result.modsToUpdate()
                ));
            } else if (result.modsToUpdate().isEmpty() && result.success()) {
                client.getToastManager().add(ToastManager.getToast(client, AutoUpdate.MOD_ID + updateScreenTranslateKey + ".noNeedToUpdateToast"));
            } else {
                client.getToastManager().add(ToastManager.getToast(client,
                        AutoUpdate.MOD_ID + updateScreenTranslateKey + ".failToGetModsListToast"));
                AutoUpdate.LOGGER.warn("Fail to check need-updating mods");
            }
        } catch (InterruptedException | ExecutionException e) {
            AutoUpdate.LOGGER.warn("Fail to get the result of update-checker {}", e.getMessage());
        }
    }
    /**
     * 带下载功能的更新检查
     */
    public static void checkUpdateWithDownload(MinecraftClient client, String updateScreenTranslateKey) {
        try {
            UpdateCheckResult result = UpdateChecker.checkForUpdatesWithDownload().get();
            if (result.success() && !result.modsToUpdate().isEmpty()) {
                // 打开模组更新选择界面
                client.setScreen(new ModUpdateSelectionScreen(
                        Text.translatable(AutoUpdate.MOD_ID + updateScreenTranslateKey + ".selectionTitle"),
                        client.currentScreen,
                        result.modsToUpdate()
                ));
            } else if (result.modsToUpdate().isEmpty() && result.success()) {
                client.getToastManager().add(ToastManager.getToast(client, 
                        AutoUpdate.MOD_ID + updateScreenTranslateKey + ".noNeedToUpdateToast"));
            } else {
                client.getToastManager().add(ToastManager.getToast(client,
                        AutoUpdate.MOD_ID + updateScreenTranslateKey + ".failToGetModsListToast"));
                AutoUpdate.LOGGER.warn("Failed to check for updates");
            }
        } catch (InterruptedException | ExecutionException e) {
            AutoUpdate.LOGGER.warn("Failed to get update check result: {}", e.getMessage());
        }
    }
}