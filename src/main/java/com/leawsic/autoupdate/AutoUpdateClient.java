package com.leawsic.autoupdate;

import com.leawsic.autoupdate.data.Config;
import net.fabricmc.api.ClientModInitializer;

public class AutoUpdateClient implements ClientModInitializer {

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        Config.initializeModDir();
    }
}
