package com.leawsic.autoupdate.tool;

import com.google.gson.Gson;
import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.UpdateCheckResult;
import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.data.mod.ModInfo;
import com.leawsic.autoupdate.data.mod.RemoteModList;
import com.leawsic.autoupdate.render.ToastManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;

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

                //todo 完善更新逻辑 支持更多更新判断模式 目前只是比较: 是否存在 & 版本是否相同
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
    public static void checkUpdateWithButton(MinecraftClient client, String updateScreenTranslateKey,ButtonWidget button){
        checkUpdate(client,updateScreenTranslateKey);
        button.active=false;
    }
    public static void checkUpdate(MinecraftClient client,String updateScreenTranslateKey){
        try {
            UpdateCheckResult result=UpdateChecker.checkForUpdates().get();
            if (result.success() && !result.modsToUpdate().isEmpty()){
                client.getToastManager().add(ToastManager.getToastWithArgs(client,
                        AutoUpdate.MOD_ID+updateScreenTranslateKey+".needsUpdateToast",result.version()));
            }else if (result.modsToUpdate().isEmpty() && result.success()){
                client.getToastManager().add(ToastManager.getToast(client,AutoUpdate.MOD_ID+updateScreenTranslateKey+".noNeedToUpdateToast"));
            }else {
                client.getToastManager().add(ToastManager.getToast(client,
                        AutoUpdate.MOD_ID+updateScreenTranslateKey+".failToGetModsListToast"));
                AutoUpdate.LOGGER.warn("Fail to check need-updating mods");
            }
        } catch (InterruptedException | ExecutionException e) {
            AutoUpdate.LOGGER.warn("Fail to get the result of update-checker {}", e.getMessage());
        }
    }
}
