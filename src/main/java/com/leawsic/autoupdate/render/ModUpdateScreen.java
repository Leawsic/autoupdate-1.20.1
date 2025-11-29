package com.leawsic.autoupdate.render;

import com.google.gson.Gson;
import com.leawsic.autoupdate.AutoUpdate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.lwjgl.system.Platform;

public class ModUpdateScreen extends Screen {
    Screen parentScreen;
    final String updateScreenTranslateKey=".checkScreen";
    private static final String DOWNLOAD_THREAD_NAME="Autoupdate Mod Download Thread";
    private Gson gson=new Gson();

    public ModUpdateScreen(Text title,Screen parent){
        super(title);
        this.parentScreen=parent;
    }

    @Override
    protected void init() {
        //检查更新按钮
        ButtonWidget checkBtn=
                ButtonWidget.builder(title,button -> {
                    this.client.getToastManager().add(SystemToast.create(this.client,SystemToast.Type.NARRATOR_TOGGLE,title, Text.of(this.client.runDirectory.getPath())));
                }).dimensions(this.width/2-100, this.height/5-15,200,30).build();

        //导出模组列表按钮
        ButtonWidget exportBtn= ButtonWidget.builder(Text.translatable(AutoUpdate.MOD_ID+updateScreenTranslateKey+".toExportBtn"),
                button -> this.client.setScreen(new ExportModsListScreen(Text.of("exportModsListScreen"),this))).dimensions(this.width/2-100,
                this.height/3*2-15,200,30).build();

        ButtonWidget backBtn= ButtonWidget.builder(Text.translatable(AutoUpdate.MOD_ID+updateScreenTranslateKey+".backBtn"),
                button -> this.close()).dimensions(0,0,120,20).build();

        addDrawableChild(checkBtn);
        addDrawableChild(backBtn);
        addDrawableChild(exportBtn);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //必须得加
        this.renderBackground(context);
        String platform=Platform.get().getName();
        int length=platform.length();
        context.drawTextWithShadow(this.textRenderer,platform, this.width/2-(length/2),0,0xffffff);

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
