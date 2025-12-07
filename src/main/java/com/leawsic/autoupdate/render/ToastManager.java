package com.leawsic.autoupdate.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.text.Text;

public class ToastManager {
    public static Toast getToast(MinecraftClient client, String translationKey){
        return SystemToast.create(client, SystemToast.Type.NARRATOR_TOGGLE, Text.translatable(translationKey+".title"),
                Text.translatable(translationKey+".content"));
    }
    public static Toast getToastWithArgs(MinecraftClient client,String translationKey,Object... args){
        return SystemToast.create(client,SystemToast.Type.NARRATOR_TOGGLE,Text.translatable(translationKey+".title"),
                Text.translatable(translationKey+".content",args));
    }
}
