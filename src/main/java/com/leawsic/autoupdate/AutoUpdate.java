package com.leawsic.autoupdate;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AutoUpdate implements ModInitializer {
	public static final String MOD_ID = "autoupdate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String MOD_VER = "alpha";

    @Override
	public void onInitialize() {
	}
    public static Version getVersion(){
        return FabricLoader.getInstance().getAllMods().stream().filter(modContainer -> Objects.equals(modContainer.getMetadata().getId(), AutoUpdate.MOD_ID)).findFirst().orElseThrow().getMetadata().getVersion();
    }
}