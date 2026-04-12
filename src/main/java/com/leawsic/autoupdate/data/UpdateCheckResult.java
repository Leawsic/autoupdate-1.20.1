package com.leawsic.autoupdate.data;

import com.leawsic.autoupdate.data.mod.ModInfo;
import com.leawsic.autoupdate.utils.ModDownloadManager.DownloadResult;

import java.util.ArrayList;
import java.util.List;

public record UpdateCheckResult(boolean success, String packVersion,
                                DownloadResult downloadResult, boolean haveModsToDownload, List<ModInfo> missingMods) {

    public static UpdateCheckResult success(String packVersion,List<ModInfo> missingMods) {
        return new UpdateCheckResult(true, packVersion, null, false, missingMods);
    }

    public static UpdateCheckResult successWithNoNeedToDownload(String packVersion, DownloadResult downloadResult) {
        return new UpdateCheckResult(true, packVersion, downloadResult, false, new ArrayList<>());
    }

    public static UpdateCheckResult haveModsToDownload(List<ModInfo> missingMods, String packVersion) {
        return new UpdateCheckResult(true, packVersion, null, true, missingMods);
    }

    public static UpdateCheckResult failure() {
        return new UpdateCheckResult(false, "", null, false, new ArrayList<>());
    }

    public boolean isHavingModsToDownload() {
        return haveModsToDownload;
    }

    public List<ModInfo> missingMods() {
        return missingMods;
    }
}