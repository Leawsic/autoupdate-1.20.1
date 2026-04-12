package com.leawsic.autoupdate.render.screen;

import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.utils.UpdateChecker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ModUpdateScreen extends Screen {
    Screen parentScreen;
    public static final String updateScreenTranslateKey=".checkScreen";

    public ModUpdateScreen(Text title,Screen parent){
        super(title);
        this.parentScreen=parent;
    }

    @Override
    protected void init() {
        // 检查更新按钮（带自动下载）- 更美观的设计
        ButtonWidget checkBtn = ButtonWidget.builder(title, button -> {
            if (Config.getInstance().getInfo().isAutoDownloadMissingMod()) {
                UpdateChecker.checkUpdateWithDownload(this.client, updateScreenTranslateKey);
            } else {
                UpdateChecker.checkUpdate(this.client, updateScreenTranslateKey);
            }
            button.active = false;
            button.setMessage(Text.translatable(AutoUpdate.MOD_ID + updateScreenTranslateKey + ".checking"));
        }).dimensions(this.width / 2 - 120, this.height / 3 - 20, 240, 35).build();

        // 导出模组列表按钮
        ButtonWidget toExportScreenBtn= ButtonWidget.builder(
                Text.translatable(AutoUpdate.MOD_ID+updateScreenTranslateKey+".toExportBtn"),
                button -> this.client.setScreen(new ExportModsListScreen(Text.of("exportModsListScreen"),this))
        ).dimensions(this.width/2 - 120, this.height/2 + 10, 240, 30).build();

        // 返回按钮 - 更好的位置
        ButtonWidget backBtn= ButtonWidget.builder(
                Text.translatable(AutoUpdate.MOD_ID+updateScreenTranslateKey+".backBtn"),
                button -> this.close()
        ).dimensions(10, this.height - 35, 100, 25).build();

        addDrawableChild(checkBtn);
        addDrawableChild(backBtn);
        addDrawableChild(toExportScreenBtn);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渐变背景
        this.renderBackgroundTexture(context);

        // 主标题
        context.drawCenteredTextWithShadow(this.textRenderer, 
                Text.translatable(AutoUpdate.MOD_ID + updateScreenTranslateKey + ".mainTitle"),
                this.width / 2, 50, 0xFFFFFF);

        // 副标题
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable(AutoUpdate.MOD_ID + updateScreenTranslateKey + ".subtitle"),
                this.width / 2, 70, 0xCCCCCC);

        // 自动下载状态显示
        boolean autoDownload = Config.getInstance().getInfo().isAutoDownloadMissingMod();
        Text statusText = Text.translatable(
                AutoUpdate.MOD_ID + updateScreenTranslateKey + (autoDownload ? ".autoDownloadEnabled" : ".autoDownloadDisabled")
        );
        context.drawCenteredTextWithShadow(this.textRenderer,
                statusText,
                this.width / 2, this.height / 2 - 40,
                autoDownload ? 0xFF4CAF50 : 0xFFFF9800);

        // 添加装饰元素
        context.fill(this.width / 2 - 150, 90, this.width / 2 + 150, 92, 0x33FFFFFF); // 分割线
        context.fill(this.width / 2 - 150, this.height / 2 - 50, this.width / 2 + 150, this.height / 2 - 48, 0x33FFFFFF); // 分割线

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }else {
            throw new NullPointerException("Client shouldn't be null!");
        }
    }
}