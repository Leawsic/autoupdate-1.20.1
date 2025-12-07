package com.leawsic.autoupdate.data.config;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class ConfigInfo{
    public static final String MOD_LIST_URL_STRING="mod_list_url";
    public static final String REPLACE_REALMS_BUTTON_BOOL="replace_realms_button";
    public static final String AUTO_DOWNLOAD_MISSING_MOD_BOOL="auto_download_missing_mod";
    public static final String SAVE_MODS_LIST_TO_LOCAL_BOOL="save_mods_list_to_local";

    @SerializedName(MOD_LIST_URL_STRING)
    public String modListUrl;
    @SerializedName(REPLACE_REALMS_BUTTON_BOOL)
    public boolean replaceRealmsBtn;
    @SerializedName(AUTO_DOWNLOAD_MISSING_MOD_BOOL)
    public boolean autoDownloadMissingMod;
    @SerializedName(SAVE_MODS_LIST_TO_LOCAL_BOOL)
    public boolean saveModsListToLocal;

    public static final ConfigInfo DEFAULT=new ConfigInfo(
            "http://127.0.0.1:8000/modsList.json",
            false,
            false,
            false);

    public ConfigInfo(String modListUrl,boolean replaceRealmsBtn,boolean autoDownloadMissingMod,
                      boolean saveModsListToLocal){
        this.modListUrl=modListUrl;
        this.replaceRealmsBtn=replaceRealmsBtn;
        this.autoDownloadMissingMod=autoDownloadMissingMod;
        this.saveModsListToLocal=saveModsListToLocal;
    }
    public JsonObject getJson(){
        JsonObject d=new JsonObject();

        d.addProperty(MOD_LIST_URL_STRING,this.modListUrl);
        d.addProperty(REPLACE_REALMS_BUTTON_BOOL,this.replaceRealmsBtn);
        d.addProperty(AUTO_DOWNLOAD_MISSING_MOD_BOOL,this.autoDownloadMissingMod);
        d.addProperty(SAVE_MODS_LIST_TO_LOCAL_BOOL,this.saveModsListToLocal);

        return d;
    }
}
