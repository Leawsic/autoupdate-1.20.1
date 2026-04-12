package com.leawsic.autoupdate.utils;

import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.UpdateCheckResult;
import com.leawsic.autoupdate.data.api.PackVersionAPI;
import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.data.mod.ModInfo;
import com.leawsic.autoupdate.data.mod.RemoteModList;
import com.leawsic.autoupdate.render.ToastManager;
import com.leawsic.autoupdate.render.screen.ModUpdateSelectionScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.leawsic.autoupdate.utils.ModDownloadManager.startDownloadAndShowResult;

public class UpdateChecker {
    /**
     * Fetch remote mod list and compare with local mods to find updates
     */
    private static CompletableFuture<UpdateCheckResult> checkForUpdates() {
        return compareRemoteWithLocal().thenApply(comparisonResult -> {
            if (!comparisonResult.success) {
                return UpdateCheckResult.failure();
            }
            return UpdateCheckResult.success(comparisonResult.packVersion, comparisonResult.missingMods);
        });
    }

    /**
     * Fetch the remote mod list from the server
     */
    private static RemoteModList fetchRemoteModList(String currentPackVer) {
        if (Objects.equals(currentPackVer, PackVersionAPI.getLatestVersion())) {
            // 留空版本号以表示本地客户端包已是最新版
            return new RemoteModList(null, Objects.requireNonNull(PackVersionAPI.getLatestPack()).getModInfos());
        } else {
            return PackVersionAPI.getLatestPack();
        }
    }

    /**
     * 检查更新并自动下载缺失模组的Future方法
     */
    private static CompletableFuture<UpdateCheckResult> checkForUpdatesWithDownload() {
        return compareRemoteWithLocal().thenApply(comparisonResult -> {
            if (!comparisonResult.success) {
                return UpdateCheckResult.failure();
            }
            if (!comparisonResult.missingMods.isEmpty()) {
                return UpdateCheckResult.haveModsToDownload(comparisonResult.missingMods, comparisonResult.packVersion);
            } else {
                return UpdateCheckResult.successWithNoNeedToDownload(comparisonResult.packVersion,
                        new ModDownloadManager.DownloadResult(0, 0, true));
            }
        });
    }

    /**
     * 带下载功能的更新检查
     */
    public static void checkUpdateWithDownload(MinecraftClient client, String updateScreenTranslateKey) {
        client.getToastManager().add(SystemToast.create(client, SystemToast.Type.NARRATOR_TOGGLE,
                Text.translatable(AutoUpdate.MOD_ID + updateScreenTranslateKey + ".checking"), Text.empty()));

        // 更新检查
        UpdateChecker.checkForUpdatesWithDownload()
                .thenAccept(result -> client.execute(() -> {
                    if (result.success()) {
                        if (result.isHavingModsToDownload()) {
                            // 如果有模组正在下载，启动下载并等待完成
                            startDownloadAndShowResult(client, result.missingMods(), result.packVersion());
                        } else {
                            // 如果没有需要更新的模组，显示完成Toast
                            // 且无需刷新本地模组列表版本
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
            if (!PackVersionAPI.checkStatus()) return ComparisonResult.failure();
            try {
                RemoteModList remoteModList = fetchRemoteModList(Config.getInstance().getInfo().getCurrentPackVer());
                if (remoteModList == null) return ComparisonResult.failure();
                System.out.println(remoteModList.getModInfos().size());
                List<ModInfo> missingMods = new ArrayList<>();

                //todo 改进检查更新逻辑——直接覆盖模组也可能造成问题
                for (ModInfo remoteMod : remoteModList.getModInfos()) {
                    ModInfo localMod = LocalModListManager.getInstance().getModById(remoteMod.getId());
                    if (localMod == null) {
                        missingMods.add(remoteMod);
                    }
                }
                // todo 判断是否需要更新本地模组列表packVersion
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