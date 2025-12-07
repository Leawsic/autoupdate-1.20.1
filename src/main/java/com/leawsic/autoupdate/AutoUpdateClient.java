package com.leawsic.autoupdate;

import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.render.screen.ModUpdateScreen;
import com.leawsic.autoupdate.tool.UpdateChecker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class AutoUpdateClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    private boolean reminded=false;
    @Override
    public void onInitializeClient() {
        Config.getInstance().initializeModDir();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(AutoUpdate.MOD_ID,"update_checker_trigger");
            }

            @Override
            public void reload(ResourceManager manager) {
                if (!reminded) {
                    UpdateChecker.checkUpdate(MinecraftClient.getInstance(), ModUpdateScreen.updateScreenTranslateKey);
                    reminded=true;
                }
            }
        });
    }
}
