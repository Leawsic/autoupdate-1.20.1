package com.leawsic.autoupdate.data.mod;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class ModInfo {
    private final String id;
    private final String name;
    private final String version;

    public ModInfo(ModContainer modContainer){
        ModMetadata metadata=modContainer.getMetadata();
        this.id=metadata.getId();
        this.name=metadata.getName();
        this.version=metadata.getVersion().getFriendlyString();
    }
    public ModInfo(String id,String name,String version){
        this.id=id;
        this.name=name;
        this.version=version;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public JsonObject toJsonObject(){
        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("id",this.id);
        jsonObject.addProperty("name",this.name);
        jsonObject.addProperty("version",this.version);
        return jsonObject;
    }
}
