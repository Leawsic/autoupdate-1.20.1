package com.leawsic.autoupdate.data.mod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RemoteModList {
    private String packVersion;
    @SerializedName("mods")
    //教训 ---- Gson的 serializedName 一定和此值在 JsonObject 中的 key 名一样 否则解析时不会对应的
    private List<ModInfo> modInfos;

    public RemoteModList(){}

    public RemoteModList(String packVersion, List<ModInfo> modInfos){
        this.packVersion=packVersion;
        this.modInfos=modInfos;
    }

    public String getPackVersion() {
        return packVersion;
    }

    public List<ModInfo> getModInfos() {
        return modInfos;
    }

    public JsonArray getModInfosJson(){
        JsonArray jsonArray=new JsonArray();
        for (ModInfo info:this.modInfos){
            jsonArray.add(info.toJsonObject());
        }
        return jsonArray;
    }

    public JsonObject toJsonObject(){
        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("packVersion",this.packVersion);

        //jsonObject的add方法只要添加的是JsonElement或其子类就行 所以直接将modInfo列表转为JsonArray即可(没见识状
        jsonObject.add("mods",getModInfosJson());
        return jsonObject;
    }
}
