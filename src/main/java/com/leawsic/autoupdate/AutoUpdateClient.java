package com.leawsic.autoupdate;

import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.render.screen.ModUpdateScreen;
import com.leawsic.autoupdate.utils.UpdateChecker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class AutoUpdateClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    private boolean reminded = false;

    @Override
    public void onInitializeClient() {
        Config.getInstance().initializeModDir();

        // 使用ResourceManagerHelper在资源重载阶段执行更新检查
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                if (!reminded && Config.getInstance().getInfo().isStartWithCheck()) {
                    // 根据配置选择正确的检查更新方法
                    if (Config.getInstance().getInfo().isAutoDownloadMissingMod()) {
                        UpdateChecker.checkUpdateWithDownload(net.minecraft.client.MinecraftClient.getInstance(), ModUpdateScreen.updateScreenTranslateKey);
                    } else {
                        UpdateChecker.checkUpdate(net.minecraft.client.MinecraftClient.getInstance(), ModUpdateScreen.updateScreenTranslateKey);
                    }
                    reminded = true;
                }
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier(AutoUpdate.MOD_ID, "update_checker");
            }
        });
    }
}