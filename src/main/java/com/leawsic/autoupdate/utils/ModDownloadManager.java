package com.leawsic.autoupdate.utils;

import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.api.ModrinthAPI;
import com.leawsic.autoupdate.data.config.ConfigInfo;
import com.leawsic.autoupdate.data.mod.ModInfo;
import com.leawsic.autoupdate.render.ToastManager;
import com.leawsic.autoupdate.render.screen.UpdateResultScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModDownloadManager {
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String LOADER = "fabric";
    public static File getModsDirectory() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            return new File(client.runDirectory, "mods");
        }
        return null;
    }
    /**
     * 下载所有的缺失模组
     */
    public CompletableFuture<DownloadResult> downloadMissingMods(List<ModInfo> missingMods) {
        return CompletableFuture.supplyAsync(() -> {
            if (missingMods == null || missingMods.isEmpty()) {
                AutoUpdate.LOGGER.info("No missing mods to download");
                return new DownloadResult(0, 0, true);
            }

            AutoUpdate.LOGGER.info("Starting download {} missing mods", missingMods.size());
            int successCount = 0;
            int totalCount = missingMods.size();

            for (ModInfo mod : missingMods) {
                try {
                    boolean success = downloadMod(mod);
                    if (success) {
                        successCount++;
                        AutoUpdate.LOGGER.info("Successfully downloaded mod: {}", mod.getName());
                    } else {
                        AutoUpdate.LOGGER.warn("Failed to download mod: {}", mod.getName());
                    }

                    Thread.sleep(500);
                    
                } catch (Exception e) {
                    AutoUpdate.LOGGER.error("Error haveModsToDownload mod {}: {}", mod.getName(), e.getMessage());
                }
            }

            boolean overallSuccess = isSuccessful(successCount,totalCount);
            AutoUpdate.LOGGER.info("Download completed: {}/{} mods downloaded successfully", 
                    successCount, totalCount);
            return new DownloadResult(successCount, totalCount, overallSuccess);
        });
    }

    /**
     * 下载单个模组
     */
    private boolean downloadMod(ModInfo mod) {
        try {
            AutoUpdate.LOGGER.info("Attempting to download mod: {} (ID: {}, Version: {})", 
                    mod.getName(), mod.getId(), mod.getVersion());

            // 必须同时有ID和版本号
            if (mod.getId() == null || mod.getId().isEmpty() || mod.getVersion() == null || mod.getVersion().isEmpty()) {
                AutoUpdate.LOGGER.error("Cannot download mod {}: missing ID or version (ID: {}, Version: {})", 
                        mod.getName(), mod.getId(), mod.getVersion());
                return false;
            }

            return ModrinthAPI.downloadFileThroughAPI(mod.getName(), MINECRAFT_VERSION,LOADER,mod.getSha1());
            
        } catch (Exception e) {
            AutoUpdate.LOGGER.error("Unexpected error haveModsToDownload mod {}: {}", mod.getName(), e.getMessage());
            return false;
        }
    }

    public static void startDownloadAndShowResult(MinecraftClient client, List<ModInfo> missingMods,String packVer) {
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
                    if (success){
                        ConfigInfo.refreshPackVer(packVer);
                    }
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

    boolean isSuccessful(int successCnt,int allCnt){
        if (allCnt>=3){
            if (allCnt%2==0){
                return successCnt>=allCnt/2;
            }else {
                return successCnt>=(allCnt-1)/2;
            }
        }else {
            return successCnt>0;
        }
    }

    /**
         * 下载结果类
         */
    public record DownloadResult(int successCount, int totalCount, boolean overallSuccess) {}
}