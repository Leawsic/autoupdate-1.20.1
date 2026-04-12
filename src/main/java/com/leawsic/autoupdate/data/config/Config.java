package com.leawsic.autoupdate.data.config;

import com.google.gson.GsonBuilder;
import com.leawsic.autoupdate.AutoUpdate;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.util.Objects;

public class Config {
    private ConfigInfo info=null;
    private File autoupdateDir=null;
    public static final String CONFIG_FILE_NAME="config.json";
    private static final Config INSTANCE=new Config();
    private Config(){}
    public static Config getInstance(){
        if (INSTANCE==null) {
            throw new NullPointerException("Config INSTANCE not initialized yet");
        }
        return INSTANCE;
    }

    public ConfigInfo getInfo() {
        return info;
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
            if (!configFile.exists() || new GsonBuilder().create().fromJson(new FileReader(configFile),
                    ConfigInfo.class)==null){
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
    private ConfigInfo getConfigInfoFromFile(File configFile) {
        if (configFile==null){
            configFile=new File(autoupdateDir,CONFIG_FILE_NAME);
        }
        try {
            // 直接使用Gson解析文件，如果文件中没有出现ConfigInfo中定义的argument，则会自动以Java的默认值补上 如 null false
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
    public void refreshConfigInfoToFile(ConfigInfo content){
        Objects.requireNonNull(content);
        info=content;
        writeConfigInfoToFile(content,new File(autoupdateDir,CONFIG_FILE_NAME));
    }
}
