package com.leawsic.autoupdate.data.config;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class ConfigInfo{
    private static final String MOD_LIST_URL_STRING="mod_list_url";
    private static final String REPLACE_REALMS_BUTTON_BOOL="replace_realms_button";
    private static final String AUTO_DOWNLOAD_MISSING_MOD_BOOL="auto_download_missing_mod";
    private static final String START_WITH_CHECK="start_with_check";

    @SerializedName(MOD_LIST_URL_STRING)
    public String modListUrl;
    @SerializedName(REPLACE_REALMS_BUTTON_BOOL)
    public boolean replaceRealmsBtn;
    @SerializedName(AUTO_DOWNLOAD_MISSING_MOD_BOOL)
    public boolean autoDownloadMissingMod;
    @SerializedName(START_WITH_CHECK)
    public boolean startWithCheck;

    public static final ConfigInfo DEFAULT=new ConfigInfo(
            "http://127.0.0.1:8000/modsList.json",
            false,
            false,
            true);

    public ConfigInfo(String modListUrl,boolean replaceRealmsBtn,boolean autoDownloadMissingMod,boolean startWithCheck){
        this.modListUrl=modListUrl;
        this.replaceRealmsBtn=replaceRealmsBtn;
        this.autoDownloadMissingMod=autoDownloadMissingMod;
        this.startWithCheck=startWithCheck;
    }
    public JsonObject getJson(){
        JsonObject d=new JsonObject();

        d.addProperty(MOD_LIST_URL_STRING,this.modListUrl);
        d.addProperty(REPLACE_REALMS_BUTTON_BOOL,this.replaceRealmsBtn);
        d.addProperty(AUTO_DOWNLOAD_MISSING_MOD_BOOL,this.autoDownloadMissingMod);
        d.addProperty(START_WITH_CHECK,this.startWithCheck);

        return d;
    }
}
