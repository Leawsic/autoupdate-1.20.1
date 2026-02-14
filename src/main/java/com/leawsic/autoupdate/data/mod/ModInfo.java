package com.leawsic.autoupdate.data.mod;

import com.google.gson.JsonObject;
import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.utils.HashCodeGenerator;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class ModInfo {
    private final String id;
    private final String name;
    private final String version;
    private final String sha1;

    public ModInfo(ModContainer modContainer) {
        ModMetadata metadata=modContainer.getMetadata();
        this.id=metadata.getId();
        this.name=metadata.getName();
        this.version=metadata.getVersion().getFriendlyString();
        this.sha1= Objects.requireNonNull(getHashCode(modContainer));
    }
    public ModInfo(String id, String name, String version, String sha1){
        this.id=id;
        this.name=name;
        this.version=version;
        this.sha1 = sha1;
    }
    private String getHashCode(ModContainer container) {
        if (container.getOrigin().getKind()== ModOrigin.Kind.NESTED) return "";
        for (Path path:container.getOrigin().getPaths()){
            if (path.toString().endsWith(".jar")){
                try {
                    return HashCodeGenerator.getSha1FromPath(path);
                } catch (IOException e) {
                    AutoUpdate.LOGGER.error("Failed to get mods' hashes");
                }
            }
        }
        return "";
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

    public String getSha1(){
        return sha1;
    }

    public JsonObject toJsonObject(){
        JsonObject jsonObject=new JsonObject();
        jsonObject.addProperty("id",this.id);
        jsonObject.addProperty("name",this.name);
        jsonObject.addProperty("version",this.version);
        jsonObject.addProperty("sha1",this.sha1);
        return jsonObject;
    }
}
