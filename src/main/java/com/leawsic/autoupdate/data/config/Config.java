package com.leawsic.autoupdate.data.config;

import com.google.gson.GsonBuilder;
import com.leawsic.autoupdate.AutoUpdate;
import net.minecraft.client.MinecraftClient;

import java.io.*;

public class Config {
    public static ConfigInfo info=null;
    private static File autoupdateDir=null;
    public static final String CONFIG_FILE_NAME="config.json";
    private static Config INSTANCE;
    private Config(){}
    public static Config getInstance(){
        if (INSTANCE==null) INSTANCE=new Config();
        return INSTANCE;
    }

    public void initializeModDir(){
        MinecraftClient client=MinecraftClient.getInstance();
        if (client!=null){
            File auDir=getOrCreateDir(new File(client.runDirectory, AutoUpdate.MOD_ID));
            if (auDir!=null){
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

    public File getAutoupdateDir() {
        return autoupdateDir;
    }

    private boolean checkOrCreateDefaultFiles() {
        File configFile = new File(autoupdateDir, CONFIG_FILE_NAME);
        try {
            if (!configFile.exists()){
                if (writeConfigInfoToFile(ConfigInfo.DEFAULT, configFile)){
                    AutoUpdate.LOGGER.info("Default files created successfully!");
                }
                info=ConfigInfo.DEFAULT;
            }else {
                info=getConfigInfoFromFile(configFile);
            }
            return true;
        } catch (Exception e) {
            AutoUpdate.LOGGER.error(e.getMessage());
            return false;
        }
    }
    public ConfigInfo getConfigInfoFromFile(File configFile) {
        if (configFile==null){
            configFile=new File(autoupdateDir,CONFIG_FILE_NAME);
        }
        try {
            ConfigInfo configInfo=new GsonBuilder().setPrettyPrinting().create().fromJson(new FileReader(configFile),ConfigInfo.class);
            writeConfigInfoToFile(configInfo,configFile);
            return configInfo;
        } catch (FileNotFoundException e) {
            AutoUpdate.LOGGER.error("Error when serializing config file");
            return ConfigInfo.DEFAULT;
        }
    }
    public boolean writeConfigInfoToFile(ConfigInfo content,File configFile) {
        try {
            FileWriter fileWriter=new FileWriter(configFile);
            fileWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(content.getJson()));
            fileWriter.close();
            return true;
        } catch (IOException e) {
            AutoUpdate.LOGGER.error("Error writing config info to file");
            return false;
        }
    }
    private File getOrCreateDir(File file){
        if (!file.exists()){
            if (file.mkdir()){
                AutoUpdate.LOGGER.info("Successfully create dir {}",file);
                return file;
            }else {
                AutoUpdate.LOGGER.error("Can't create dir {}",file);
                return null;
            }
        }else {
            AutoUpdate.LOGGER.warn("Dir already exists!");
            return file;
        }
    }
}
