package com.leawsic.autoupdate.data;

import com.leawsic.autoupdate.data.mod.ModInfo;

import java.util.ArrayList;
import java.util.List;

public record UpdateCheckResult(boolean success, List<ModInfo> modsToUpdate,String version) {

    public static UpdateCheckResult success(List<ModInfo> modsToUpdate,String version) {
        return new UpdateCheckResult(true, modsToUpdate,version);
    }

    public static UpdateCheckResult failure() {
        return new UpdateCheckResult(false, new ArrayList<>(),null);
    }
}