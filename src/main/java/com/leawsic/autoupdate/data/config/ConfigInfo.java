package com.leawsic.autoupdate.data.config;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class ConfigInfo {
    public static final ConfigInfo DEFAULT = new ConfigInfo(
            "http://127.0.0.1:3000/",
            false,
            false,
            true,
            "1");

    private static final String MOD_LIST_URL_STRING = "mod_list_url";
    private static final String REPLACE_REALMS_BUTTON_BOOL = "replace_realms_button";
    private static final String AUTO_DOWNLOAD_MISSING_MOD_BOOL = "auto_download_missing_mod";
    private static final String START_WITH_CHECK = "start_with_check";
    private static final String CURRENT_PACK_VER = "current_pack_ver";

    @SerializedName(MOD_LIST_URL_STRING)
    private String modListUrl;
    @SerializedName(REPLACE_REALMS_BUTTON_BOOL)
    private boolean replaceRealmsBtn;
    @SerializedName(AUTO_DOWNLOAD_MISSING_MOD_BOOL)
    private boolean autoDownloadMissingMod;
    @SerializedName(START_WITH_CHECK)
    private boolean startWithCheck;
    @SerializedName(CURRENT_PACK_VER)
    private String currentPackVer;

    public ConfigInfo(String modListUrl, boolean replaceRealmsBtn, boolean autoDownloadMissingMod,
                      boolean startWithCheck, String currentPackVer) {
        this.modListUrl = modListUrl;
        this.replaceRealmsBtn = replaceRealmsBtn;
        this.autoDownloadMissingMod = autoDownloadMissingMod;
        this.startWithCheck = startWithCheck;
        this.currentPackVer = currentPackVer;
    }

    public static void refreshPackVer(String newPackVer) {
        ConfigInfo currentInfo = Objects.requireNonNull(Config.getInstance().getInfo());
        currentInfo.currentPackVer = newPackVer;
        Config.getInstance().refreshConfigInfoToFile(currentInfo);
    }

    public String getCurrentPackVer() {
        return currentPackVer;
    }

    public String getModListUrl() {
        return modListUrl;
    }

    public boolean isStartWithCheck() {
        return startWithCheck;
    }

    public boolean isReplaceRealmsBtn() {
        return replaceRealmsBtn;
    }

    public boolean isAutoDownloadMissingMod() {
        return autoDownloadMissingMod;
    }

    public JsonObject getJson() {
        JsonObject d = new JsonObject();

        d.addProperty(MOD_LIST_URL_STRING, this.modListUrl);
        d.addProperty(REPLACE_REALMS_BUTTON_BOOL, this.replaceRealmsBtn);
        d.addProperty(AUTO_DOWNLOAD_MISSING_MOD_BOOL, this.autoDownloadMissingMod);
        d.addProperty(START_WITH_CHECK, this.startWithCheck);
        d.addProperty(CURRENT_PACK_VER, this.currentPackVer);

        return d;
    }
}
