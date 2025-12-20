package com.leawsic.autoupdate.tool;

import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.api.ModrinthAPI;
import com.leawsic.autoupdate.data.mod.ModInfo;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModDownloadManager {
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String LOADER = "fabric";
    
    private final ModrinthAPI modrinthAPI;
    private final File modsDir;

    public ModDownloadManager() {
        this.modrinthAPI = new ModrinthAPI();
        this.modsDir = getModsDirectory();
    }

    /**
     * 获取模组目录
     */
    private File getModsDirectory() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            return new File(client.runDirectory, "mods");
        }
        return new File("mods"); // 备用路径
    }

    /**
     * 下载缺失的模组列表
     */
    public CompletableFuture<Boolean> downloadMissingMods(List<ModInfo> missingMods) {
        return CompletableFuture.supplyAsync(() -> {
            if (missingMods == null || missingMods.isEmpty()) {
                AutoUpdate.LOGGER.info("No missing mods to download");
                return true;
            }

            AutoUpdate.LOGGER.info("Starting download of {} missing mods", missingMods.size());
            int successCount = 0;

            for (ModInfo mod : missingMods) {
                try {
                    boolean success = downloadMod(mod);
                    if (success) {
                        successCount++;
                        AutoUpdate.LOGGER.info("Successfully downloaded mod: {}", mod.getName());
                    } else {
                        AutoUpdate.LOGGER.warn("Failed to download mod: {}", mod.getName());
                    }
                    
                    // 添加延迟避免请求过于频繁
                    Thread.sleep(500);
                    
                } catch (Exception e) {
                    AutoUpdate.LOGGER.error("Error downloading mod {}: {}", mod.getName(), e.getMessage());
                }
            }

            AutoUpdate.LOGGER.info("Download completed: {}/{} mods downloaded successfully", 
                    successCount, missingMods.size());
            return successCount > 0;
        });
    }

    /**
     * 下载单个模组
     */
    private boolean downloadMod(ModInfo mod) {
        try {
            AutoUpdate.LOGGER.info("Attempting to download mod: {} (ID: {}, Version: {})", 
                    mod.getName(), mod.getId(), mod.getVersion());

            // 首先尝试使用模组ID和指定版本下载
            if (mod.getId() != null && !mod.getId().isEmpty() && mod.getVersion() != null && !mod.getVersion().isEmpty()) {
                AutoUpdate.LOGGER.info("Trying to download with ID and version: {} - {}", mod.getId(), mod.getVersion());
                boolean success = modrinthAPI.downloadModByIdAndVersion(mod.getId(), mod.getVersion(), MINECRAFT_VERSION, LOADER, modsDir);
                if (success) {
                    AutoUpdate.LOGGER.info("Successfully downloaded mod {} with specific version", mod.getName());
                    return true;
                } else {
                    AutoUpdate.LOGGER.warn("Failed to download mod {} with specific version, trying alternative methods", mod.getName());
                }
            }

            // 如果ID+版本下载失败，尝试使用模组ID下载（兼容旧逻辑）
            if (mod.getId() != null && !mod.getId().isEmpty()) {
                AutoUpdate.LOGGER.info("Trying to download with ID only: {}", mod.getId());
                boolean success = modrinthAPI.downloadModById(mod.getId(), MINECRAFT_VERSION, LOADER, modsDir);
                if (success) {
                    AutoUpdate.LOGGER.info("Successfully downloaded mod {} with ID only", mod.getName());
                    return true;
                } else {
                    AutoUpdate.LOGGER.warn("Failed to download mod {} with ID, trying name search", mod.getName());
                }
            }

            // 如果ID下载失败，尝试使用模组名称搜索下载
            if (mod.getName() != null && !mod.getName().isEmpty()) {
                AutoUpdate.LOGGER.info("Trying to download with name search: {}", mod.getName());
                return modrinthAPI.downloadModByName(mod.getName(), MINECRAFT_VERSION, LOADER, modsDir);
            }

            AutoUpdate.LOGGER.error("All download methods failed for mod: {}", mod.getName());
            return false;
            
        } catch (IOException e) {
            AutoUpdate.LOGGER.error("IO error downloading mod {}: {}", mod.getName(), e.getMessage());
            return false;
        } catch (Exception e) {
            AutoUpdate.LOGGER.error("Unexpected error downloading mod {}: {}", mod.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * 检查模组目录是否存在指定模组文件
     */
    public boolean isModFileExists(String modId) {
        if (modsDir.exists() && modsDir.isDirectory()) {
            File[] modFiles = modsDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (modFiles != null) {
                for (File modFile : modFiles) {
                    // 这里可以添加更精确的文件名匹配逻辑
                    if (modFile.getName().toLowerCase().contains(modId.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}