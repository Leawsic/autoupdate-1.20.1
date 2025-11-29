package com.leawsic.autoupdate.data;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.leawsic.autoupdate.AutoUpdate;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    public static ConfigInfo info=null;
    private static File autoupdateDir=null;
    private static final File configFile=new File(autoupdateDir,"config.json");

    public static void initializeModDir(){
        MinecraftClient client=MinecraftClient.getInstance();
        if (client!=null){
            File auDir=new File(client.runDirectory, AutoUpdate.MOD_ID);
            if (!auDir.exists()){
                if (auDir.mkdir()){
                    AutoUpdate.LOGGER.info("Successfully create autoupdate dir {}",auDir);
                    autoupdateDir=auDir;
                }else {
                    AutoUpdate.LOGGER.error("Can't create autoupdate dir {}",auDir);
                }
            }else {
                AutoUpdate.LOGGER.warn("Autoupdate dir already exists!");
                autoupdateDir=auDir;
            }
        }
        if (autoupdateDir!=null){
            boolean t=checkOrCreateDefaultFiles();
            if (t){
                AutoUpdate.LOGGER.info("File check completed");
            }else {
                AutoUpdate.LOGGER.error("Error when initializing files");
            }
        }
    }

    public static File getAutoupdateDir() {
        return autoupdateDir;
    }

    private static boolean checkOrCreateDefaultFiles() {
        try {
            if (!configFile.exists()){
                FileWriter fileWriter=new FileWriter(configFile);
                fileWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(ConfigInfo.getDefaultJson()));
                fileWriter.close();
                AutoUpdate.LOGGER.info("Default files created successfully!");
            }else {
                info=new GsonBuilder().setPrettyPrinting().create().fromJson(new FileReader(configFile),
                        ConfigInfo.class);
            }
            return true;
        } catch (IOException e) {
            AutoUpdate.LOGGER.error(e.getMessage());
            return false;
        }
    }
}
