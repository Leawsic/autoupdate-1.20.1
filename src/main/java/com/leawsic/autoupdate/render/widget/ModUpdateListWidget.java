package com.leawsic.autoupdate.render.widget;

import com.leawsic.autoupdate.data.mod.ModInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ModUpdateListWidget extends AlwaysSelectedEntryListWidget<ModUpdateListWidget.ModEntry> {
    private final List<ModInfo> modInfoList;

    public ModUpdateListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom,int itemHeight,List<ModInfo> modInfoList) {
        super(minecraftClient, width, height, top, bottom, itemHeight);
        this.modInfoList=modInfoList;
        this.refreshEntries();
    }

    public void refreshEntries(){
        this.clearEntries();
        for (ModInfo modInfo:modInfoList){
            this.addEntry(new ModEntry(modInfo));
        }
    }

    @Override
    public int getRowWidth() {
        return this.width-20;
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.width-6;
    }

    public class ModEntry extends Entry<ModEntry>{
        private final ModInfo modInfo;
        private final Text modName;
        private final Text modVersion;

        public ModEntry(ModInfo modInfo){
            this.modInfo=modInfo;
            this.modName=Text.literal(modInfo.getName());
            this.modVersion=Text.literal(modInfo.getVersion());
        }

        public ModInfo getModInfo(){
            return modInfo;
        }

        @Override
        public Text getNarration() {
            return Text.literal("Mod "+modInfo.getName()+" Version "+modInfo.getVersion());
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            TextRenderer textRenderer=client.textRenderer;

            if (this.isFocused()){
                context.fill(x-2,y-2,x+entryWidth-8,y+entryHeight+2,0x33ffffff);
            }
            context.drawTextWithShadow(textRenderer,modName,x+20,y+2,0xffffff);
            context.drawTextWithShadow(textRenderer,modVersion,x+20,y+12,0xcccccc);
            context.drawTextWithShadow(textRenderer,Text.literal(modInfo.getId()),x+20,y+22,0x888888);
            context.fill(x+5,y+5,x+8,y+15,0xffff5555);
        }

        public ModInfo getSelectedModInfo(){
            ModEntry selected=getSelectedOrNull();
            return selected!=null?selected.getModInfo():null;
        }
        public int getUpdateModCount(){
            //todo get need-updating mods count
            return 0;
        }
    }
}
