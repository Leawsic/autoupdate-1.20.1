package com.leawsic.autoupdate.mixin;

import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.render.screen.ModUpdateScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    @Unique
    boolean replaced=false;
    @Unique
    Text modUpdateScreenTitle=Text.of("Check mod update");
    protected TitleScreenMixin(Text title) {
        super(title);
    }
    @Inject(at = @At("TAIL"),method = "init")
    private void init(CallbackInfo info){
        if (Config.info!=null){
            if (Config.info.replaceRealmsBtn){
                replaceRealmsBtn();
            }else {
                createNewUpdateBtn();
            }
        }else {
            AutoUpdate.LOGGER.warn("Config is null!");
        }
    }
    @Unique
    private void createNewUpdateBtn(){
        addDrawableChild(ButtonWidget.builder(Text.of("U"),
                button -> openModUpdateScreen(modUpdateScreenTitle)).dimensions(this.width / 2 + 104, (this.height / 4 + 48 + 72 + 12)-22, 20, 20).build());
    }
    @Unique
    private void replaceRealmsBtn(){
        List<ButtonWidget> buttons=
                this.children().stream().filter(element -> element instanceof ButtonWidget).map(element -> (ButtonWidget) element).toList();
        for (ButtonWidget button:buttons){
            if (button.getMessage().getString().contains("Realms")){
                var width=button.getWidth();
                var height=button.getHeight();
                var x=button.getX();
                var y=button.getY();
                this.remove(button);
                this.addDrawableChild(ButtonWidget.builder(Text.translatable(AutoUpdate.MOD_ID+".title.mainUpdateBtn"),
                        btn -> openModUpdateScreen(modUpdateScreenTitle)).dimensions(x,y,width,height).build());
                replaced=true;
                break;
            }
        }
        if (!replaced){
            AutoUpdate.LOGGER.error("Failed to replace Realms button");
        }
    }
    @Unique
    private void openModUpdateScreen(Text title){
        System.out.println("check");
        if (this.client != null) {
            this.client.setScreen(new ModUpdateScreen(title,this));
        }else {
            throw new NullPointerException("Client shouldn't be null!");
        }
    }
}