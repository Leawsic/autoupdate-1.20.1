package com.leawsic.autoupdate;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoUpdate implements ModInitializer {
	public static final String MOD_ID = "autoupdate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String MOD_VER = "0.0.1";

    @Override
	public void onInitialize() {
        LOGGER.info("\nAutoupdate mod initialized successfully!");
	}
}