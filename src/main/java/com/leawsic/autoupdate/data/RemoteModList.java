package com.leawsic.autoupdate.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class RemoteModList {
    private String packVersion;
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

    public void setPackVersion(String packVersion) {
        this.packVersion = packVersion;
    }

    public JsonObject toJsonObject(){
        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("packVersion",this.packVersion);

        //ai还是太聪明了 jsonObject的add方法只要添加的是JsonElement或其子类就行 所以直接将modInfo列表转为JsonArray即可(作没见识状
        JsonArray jsonArray=new JsonArray();
        for (ModInfo info:this.modInfos){
            jsonArray.add(info.toJsonObject());
        }
        jsonObject.add("mods",jsonArray);
        return jsonObject;
    }
}
