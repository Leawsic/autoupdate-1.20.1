package com.leawsic.autoupdate.data;

import com.leawsic.autoupdate.data.mod.ModInfo;
import com.leawsic.autoupdate.tool.ModDownloadManager.DownloadResult;

import java.util.ArrayList;
import java.util.List;

public record UpdateCheckResult(boolean success, List<ModInfo> modsToUpdate, String packVersion, 
                               DownloadResult downloadResult, boolean downloading, List<ModInfo> missingMods) {

    public static UpdateCheckResult success(List<ModInfo> modsToUpdate, String packVersion) {
        return new UpdateCheckResult(true, modsToUpdate, packVersion, null, false, new ArrayList<>());
    }

    public static UpdateCheckResult successWithDownload(List<ModInfo> modsToUpdate, String packVersion, DownloadResult downloadResult) {
        return new UpdateCheckResult(true, modsToUpdate, packVersion, downloadResult, false, new ArrayList<>());
    }

    public static UpdateCheckResult downloading(List<ModInfo> missingMods, List<ModInfo> modsToUpdate, String packVersion) {
        return new UpdateCheckResult(true, modsToUpdate, packVersion, null, true, missingMods);
    }

    public static UpdateCheckResult failure() {
        return new UpdateCheckResult(false, new ArrayList<>(), "", null, false, new ArrayList<>());
    }

    public DownloadResult getDownloadResult() {
        return downloadResult;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public List<ModInfo> missingMods() {
        return missingMods;
    }
}