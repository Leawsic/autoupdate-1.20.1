package com.leawsic.autoupdate.utils;

import com.google.gson.Gson;
import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.UpdateCheckResult;
import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.data.mod.ModInfo;
import com.leawsic.autoupdate.data.mod.RemoteModList;
import com.leawsic.autoupdate.render.ToastManager;
import com.leawsic.autoupdate.render.screen.ModUpdateSelectionScreen;
import com.leawsic.autoupdate.render.screen.UpdateResultScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {

    // URL to fetch the remote mod list JSON from
    private static final String REMOTE_MOD_LIST_URL = Config.getInstance().getConfigInfoFromFile(null).modListUrl;

    /**
     * Fetch remote mod list and compare with local mods to find updates
     */
    private static CompletableFuture<UpdateCheckResult> checkForUpdates() {
        return compareRemoteWithLocal().thenApply(comparisonResult -> {
            if (!comparisonResult.success){
                return UpdateCheckResult.failure();
            }
            return UpdateCheckResult.success(comparisonResult.packVersion,comparisonResult.missingMods);
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
     * 检查更新并自动下载缺失模组的Future方法
     */
    private static CompletableFuture<UpdateCheckResult> checkForUpdatesWithDownload() {
        return compareRemoteWithLocal().thenApply(comparisonResult -> {
            if (!comparisonResult.success){
                return UpdateCheckResult.failure();
            }
            if (!comparisonResult.missingMods.isEmpty()){
                return UpdateCheckResult.downloading(comparisonResult.missingMods,comparisonResult.packVersion);
            }else {
                return UpdateCheckResult.successWithNoNeedToDownload(comparisonResult.packVersion,
                        new ModDownloadManager.DownloadResult(0,0,true));
            }
        });
    }

    /**
     * 带下载功能的更新检查
     */
    public static void checkUpdateWithDownload(MinecraftClient client, String updateScreenTranslateKey) {
        client.getToastManager().add(SystemToast.create(client, SystemToast.Type.NARRATOR_TOGGLE, 
                Text.translatable(AutoUpdate.MOD_ID + updateScreenTranslateKey + ".checking"), Text.empty()));
        
        // 非阻塞执行更新检查
        UpdateChecker.checkForUpdatesWithDownload()
                .thenAccept(result -> client.execute(() -> {
                    if (result.success()) {
                        if (result.isDownloading()) {
                            // 如果有模组正在下载，启动下载并等待完成
                            startDownloadAndShowResult(client, result.missingMods());
                        } else {
                            // 如果没有需要更新的模组，显示完成Toast
                            client.getToastManager().add(ToastManager.getToast(client, 
                                    AutoUpdate.MOD_ID + updateScreenTranslateKey + ".noNeedToUpdateToast"));
                        }
                    } else {
                        client.getToastManager().add(ToastManager.getToast(client,
                                AutoUpdate.MOD_ID + updateScreenTranslateKey + ".failToGetModsListToast"));
                        AutoUpdate.LOGGER.warn("Failed to check for updates");
                    }
                }))
                .exceptionally(e -> {
                    AutoUpdate.LOGGER.warn("Failed to get update check result: {}", e.getMessage());
                    client.execute(() -> client.getToastManager().add(ToastManager.getToast(client,
                            AutoUpdate.MOD_ID + updateScreenTranslateKey + ".failToGetModsListToast")));
                    return null;
                });
    }

    /**
     * 启动下载并显示结果界面
     */
    private static void startDownloadAndShowResult(MinecraftClient client, List<ModInfo> missingMods) {
        // 在开始下载前显示Toast提示
        client.getToastManager().add(ToastManager.getToast(client, 
                AutoUpdate.MOD_ID + ".download.autoDownloadStarted"));
        
        ModDownloadManager downloadManager = new ModDownloadManager();
        // 启动下载并等待完成
        downloadManager.downloadMissingMods(missingMods)
                .thenAccept(downloadResult -> client.execute(() -> {
                    // 下载完成后显示结果界面
                    boolean success = downloadResult.overallSuccess();
                    int downloadedCount = downloadResult.successCount();
                    // 重新加载本地模组列表以反映新下载的模组
                    LocalModListManager.getInstance().loadModInfos();
                    // 显示下载结果界面
                    client.setScreen(new UpdateResultScreen(
                            Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.title"),
                            client.currentScreen,
                            downloadedCount,
                            success
                    ));
                }))
                .exceptionally(e -> {
                    AutoUpdate.LOGGER.warn("Download failed: {}", e.getMessage());
                    client.execute(() -> {
                        // 下载失败时也显示结果界面
                        client.setScreen(new UpdateResultScreen(
                                Text.translatable(AutoUpdate.MOD_ID + ".resultScreen.title"),
                                client.currentScreen,
                                0,
                                false
                        ));
                    });
                    return null;
                });
    }

    public static void checkUpdate(MinecraftClient client, String updateScreenTranslateKey) {
        client.getToastManager().add(SystemToast.create(client, SystemToast.Type.NARRATOR_TOGGLE,
                Text.translatable(AutoUpdate.MOD_ID + updateScreenTranslateKey + ".checking"), Text.empty()));
        
        // 非阻塞执行更新检查
        UpdateChecker.checkForUpdates()
                .thenAccept(result -> client.execute(() -> {
                    if (result.success() && !result.missingMods().isEmpty()) {
                        // 打开模组更新选择界面
                        client.setScreen(new ModUpdateSelectionScreen(
                                Text.translatable(AutoUpdate.MOD_ID + updateScreenTranslateKey + ".selectionTitle"),
                                client.currentScreen,
                                result.missingMods()
                        ));
                    } else if (result.missingMods().isEmpty() && result.success()) {
                        client.getToastManager().add(ToastManager.getToast(client, 
                                AutoUpdate.MOD_ID + updateScreenTranslateKey + ".noNeedToUpdateToast"));
                    } else {
                        client.getToastManager().add(ToastManager.getToast(client,
                                AutoUpdate.MOD_ID + updateScreenTranslateKey + ".failToGetModsListToast"));
                        AutoUpdate.LOGGER.warn("Fail to check need-updating mods");
                    }
                }))
                .exceptionally(e -> {
                    AutoUpdate.LOGGER.warn("Fail to get the result of update-checker {}", e.getMessage());
                    client.execute(() -> client.getToastManager().add(ToastManager.getToast(client,
                            AutoUpdate.MOD_ID + updateScreenTranslateKey + ".failToGetModsListToast")));
                    return null;
                });
    }
    private static CompletableFuture<ComparisonResult> compareRemoteWithLocal() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RemoteModList remoteModList = fetchRemoteModList();
                if (remoteModList == null) {
                    return ComparisonResult.failure();
                }
                List<ModInfo> missingMods = new ArrayList<>();
                for (ModInfo remoteMod : remoteModList.getModInfos()) {
                    ModInfo localMod = LocalModListManager.getInstance().getModById(remoteMod.getId());
                    if (localMod==null){
                        missingMods.add(remoteMod);
                    }
                }
                return ComparisonResult.success(missingMods, remoteModList.getPackVersion());
            } catch (Exception e) {
                AutoUpdate.LOGGER.error("Error during update comparison: {}", e.getMessage());
                return ComparisonResult.failure();
            }
        });
    }

    private record ComparisonResult(boolean success, List<ModInfo> missingMods, String packVersion) {

        static ComparisonResult failure() {
                return new ComparisonResult(false, Collections.emptyList(), null);
            }

            static ComparisonResult success(List<ModInfo> modsToUpdate, String packVersion) {
                return new ComparisonResult(true, modsToUpdate, packVersion);
            }
        }
}