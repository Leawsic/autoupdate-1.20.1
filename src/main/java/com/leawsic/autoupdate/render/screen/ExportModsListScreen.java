package com.leawsic.autoupdate.render.screen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.TimeFormatter;
import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.data.mod.ModInfo;
import com.leawsic.autoupdate.data.mod.RemoteModList;
import com.leawsic.autoupdate.render.ToastManager;
import com.leawsic.autoupdate.tool.LocalModListManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ExportModsListScreen extends Screen {
    Screen parentScreen;
    final String modsListVersion ="Version of mods list";
    final String exportScreenTranslateKey=".exportScreen";
    Gson gson=new GsonBuilder().setPrettyPrinting().create();
    String modrinthSearchBaseURL="https://api.modrinth.com/v2/search/";
    public ExportModsListScreen(Text title,Screen parent) {
        super(title);
        this.parentScreen=parent;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parentScreen);
        }else {
            throw new NullPointerException("Client shouldn't be null!");
        }
    }

    @Override
    protected void init() {
        TextFieldWidget textFieldWidget=new TextFieldWidget(this.textRenderer,this.width/2-85,this.height/5-10,170,20,Text.translatable(AutoUpdate.MOD_ID+".exportScreen"+".exportListBtn"));

        ButtonWidget exportBtn=
                ButtonWidget.builder(Text.translatable(AutoUpdate.MOD_ID+".exportScreen"+".exportListBtn"),button -> {
                    List<ModInfo> modInfos=LocalModListManager.getInstance().getModInfos();
                    File modsListFile=new File(Config.getInstance().getAutoupdateDir(),
                            "modsList-%s.json".formatted(LocalDateTime.now().format(TimeFormatter.LOCAL_DATE_TIME)));
                    String listVersion=textFieldWidget.getText();

                    if (Objects.equals(listVersion, "")){
                        this.client.getToastManager().add(ToastManager.getToast(this.client,
                                AutoUpdate.MOD_ID+exportScreenTranslateKey+".emptyVersionToast"));
                        AutoUpdate.LOGGER.error("Version is empty,export fail");
                    }else {
                        try {
                            writeToJson(gson,modsListFile,listVersion,modInfos);
                            this.client.getToastManager().add(ToastManager.getToast(this.client,AutoUpdate.MOD_ID+exportScreenTranslateKey+".exportSuccessfullyToast"));
                            AutoUpdate.LOGGER.info("Mods list exported successfully!");
                            this.close();
                        } catch (IOException e) {
                            AutoUpdate.LOGGER.error(e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                }).dimensions(this.width/2-85,this.height/2-10,170,20).build();

        addDrawableChild(exportBtn);
        addDrawableChild(textFieldWidget);
    }
    private void writeToJson(Gson gson,File file,String modsListVersion,List<ModInfo> modInfos)throws IOException{
        String json=gson.toJson(new RemoteModList(modsListVersion,modInfos).toJsonObject());
        FileWriter writer=new FileWriter(file);
        writer.write(json);
        writer.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawTextWithShadow(this.textRenderer, modsListVersion,
                this.width/2- modsListVersion.length()*3+ modsListVersion.length()/2,
                this.height/10, Colors.WHITE);
        super.render(context,mouseX,mouseY,delta);
    }
}
