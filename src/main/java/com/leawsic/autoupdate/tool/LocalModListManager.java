package com.leawsic.autoupdate.tool;

import com.google.gson.JsonArray;
import com.leawsic.autoupdate.data.ModInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocalModListManager {
    private static LocalModListManager INSTANCE;
    private final List<ModInfo> modInfos;

    private LocalModListManager(){
        modInfos =new ArrayList<>();
        loadModInfos();
    }

    public static LocalModListManager getInstance(){
        if (INSTANCE==null)
            INSTANCE=new LocalModListManager();
        return INSTANCE;
    }

    private void loadModInfos() {
        List<ModContainer> mods=
                FabricLoader.getInstance().getAllMods().stream().filter(modContainer -> !modContainer.getMetadata().getType().equals("builtin")).toList();
        for (ModContainer mod:mods){
            modInfos.add(new ModInfo(mod));
        }
    }

    public List<ModInfo> getModInfos(){
        return new ArrayList<>(modInfos);
    }

    public ModInfo getModById(String id){
        return modInfos.stream().filter(modInfo -> Objects.equals(modInfo.getId(), id)).findFirst().orElse(null);
    }
    public JsonArray getJsonLocalModList(){
        JsonArray jsonArray=new JsonArray();
        for (ModInfo ModInfo: modInfos){
            jsonArray.add(ModInfo.toJsonObject());
        }
        return jsonArray;
    }
}
