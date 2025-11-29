package com.leawsic.autoupdate.data;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class ConfigInfo{
    public static final String MOD_LIST_URL_STRING="mod_list_url";
    public static final String REPLACE_REALMS_BUTTON_BOOL="replace_realms_button";
    public static final String AUTO_DOWNLOAD_MISSING_MOD_BOOL="auto_download_missing_mod";

    public String getModListUrl() {
        return modListUrl;
    }
    public void setModListUrl(String modListUrl) {
        this.modListUrl = modListUrl;
    }

    public boolean getReplaceRealmsBtn() {
        return replaceRealmsBtn;
    }
    public void setReplaceRealmsBtn(boolean replaceRealmsBtn) {
        this.replaceRealmsBtn = replaceRealmsBtn;
    }

    public boolean getAutoDownloadMissingMod() {
        return autoDownloadMissingMod;
    }
    public void setAutoDownloadMissingMod(boolean autoDownloadMissingMod) {
        this.autoDownloadMissingMod = autoDownloadMissingMod;
    }

    @SerializedName(MOD_LIST_URL_STRING)
    String modListUrl;
    @SerializedName(REPLACE_REALMS_BUTTON_BOOL)
    boolean replaceRealmsBtn;
    @SerializedName(AUTO_DOWNLOAD_MISSING_MOD_BOOL)
    boolean autoDownloadMissingMod;
    public static JsonObject getDefaultJson(){
        JsonObject d=new JsonObject();

        d.addProperty(MOD_LIST_URL_STRING,"http://110.42.233.196/modsList-2025-11-29_18.03.15.json");
        d.addProperty(REPLACE_REALMS_BUTTON_BOOL,false);
        d.addProperty(AUTO_DOWNLOAD_MISSING_MOD_BOOL,false);

        return d;
    }
}
