package com.leawsic.autoupdate.render.screen;

import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.tool.UpdateChecker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ModUpdateScreen extends Screen {
    Screen parentScreen;
    public static final String updateScreenTranslateKey=".checkScreen";
    private static final String DOWNLOAD_THREAD_NAME="Autoupdate Download Thread";

    public ModUpdateScreen(Text title,Screen parent){
        super(title);
        this.parentScreen=parent;
    }

    @Override
    protected void init() {
        //检查更新按钮
        ButtonWidget checkBtn=
                ButtonWidget.builder(title,button -> UpdateChecker.checkUpdateWithButton(this.client,updateScreenTranslateKey,button)).dimensions(this.width/2-100, this.height/5-15,200,30).build();

        //导出模组列表按钮
        ButtonWidget toExportScreenBtn= ButtonWidget.builder(Text.translatable(AutoUpdate.MOD_ID+updateScreenTranslateKey+".toExportBtn"),
                button -> this.client.setScreen(new ExportModsListScreen(Text.of("exportModsListScreen"),this))).dimensions(this.width/2-100,
                this.height/3*2-15,200,30).build();

        ButtonWidget backBtn= ButtonWidget.builder(Text.translatable(AutoUpdate.MOD_ID+updateScreenTranslateKey+".backBtn"),
                button -> this.close()).dimensions(0,0,120,20).build();

        addDrawableChild(checkBtn);
        addDrawableChild(backBtn);
        addDrawableChild(toExportScreenBtn);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //必须得加
        this.renderBackground(context);

        //此super方法必须得有且得在最后
        super.render(context,mouseX,mouseY,delta);
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
