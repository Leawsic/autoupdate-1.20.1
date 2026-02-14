package com.leawsic.autoupdate.data.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.utils.FileDownloader;
import com.leawsic.autoupdate.utils.ModDownloadManager;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ModrinthAPI {
    static final OkHttpClient httpClient=new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    static final String baseUrl = "https://api.modrinth.com/v2/";
    static final String UA="User-Agent";
    static final String searchUrl=baseUrl+"search";
    static final String userAgent = "AutoUpdate/" + AutoUpdate.MOD_VER;
    static final String getProjectUrl=baseUrl+"project/";
    static final String getProjectVersionsUrlFormatted=getProjectUrl+"%s/version";
    /**
     * 搜索模组并获取文件链接
     */
    private static JsonObject searchForModFileUrl(String modName,String gameVersion,String loader,int limit,String hash) throws IOException {
        Request searchModRequest = new Request.Builder().get().url(HttpUrl.parse(searchUrl).newBuilder()
                .addQueryParameter("query",modName)
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("facets", getFacetsString(gameVersion,loader)).build())
                .addHeader(UA,userAgent).build();
        Response response=httpClient.newCall(searchModRequest).execute();
        JsonArray hits=JsonParser.parseString(response.body().string()).getAsJsonObject().get("hits").getAsJsonArray();
        if (!hits.isEmpty()){
            JsonObject hitModInfo=hits.get(0).getAsJsonObject();
            Request getVersionRequest=
                    new Request.Builder().get().url(HttpUrl.parse(getProjectVersionsUrlFormatted.formatted(hitModInfo.get("project_id").getAsString())).newBuilder()
                            .addQueryParameter("loaders",getSingleParameterJsonArray(loader).toString())
                            .addQueryParameter("game_versions",getSingleParameterJsonArray(gameVersion).toString())
                            .build()).addHeader(UA,userAgent).build();
            Response getVersionResponse=httpClient.newCall(getVersionRequest).execute();
            if (getVersionResponse.code()== HttpURLConnection.HTTP_OK){
                JsonArray modVersionsArray=JsonParser.parseString(getVersionResponse.body().string()).getAsJsonArray();
                for (JsonElement modVersion:modVersionsArray){
                    JsonObject modVersionObject=modVersion.getAsJsonObject();
                    JsonObject fileObject=modVersionObject.get("files").getAsJsonArray().get(0).getAsJsonObject();

                    if (Objects.equals(hash, fileObject.get("hashes").getAsJsonObject().get("sha1").getAsString())){
                        JsonObject target=new JsonObject();
                        target.addProperty("url",fileObject.get("url").getAsString());
                        target.addProperty("filename", fileObject.get("filename").getAsString());
                        return target;
                    }
                }
            }
        }
        return null;
    }
    public static boolean downloadFileThroughAPI(String modName,String gameVer,String loader,String hash){
        try {
            JsonObject searchResult=searchForModFileUrl(modName,gameVer,loader,1,hash);
            File modFile=new File(Objects.requireNonNull(ModDownloadManager.getModsDirectory()),
                    Objects.requireNonNull(searchResult).get("filename").getAsString());
            return FileDownloader.downloadFile(Objects.requireNonNull(searchResult).get("url").getAsString(), modFile, userAgent, httpClient);
        } catch (IOException e) {
            AutoUpdate.LOGGER.error("Error occurred when downloading mod!\n{}\n{}", e.getMessage(),e.getCause());
            return false;
        }
    }

    private static String getFacetsString(String gameVersion,String LOADER){
        JsonArray facetsArray=new JsonArray();

        facetsArray.add(getSingleParameterJsonArray("project_type:mod"));
        facetsArray.add(getSingleParameterJsonArray("versions:%s".formatted(gameVersion)));
        facetsArray.add(getSingleParameterJsonArray("categories:%s".formatted(LOADER)));

        return facetsArray.toString();
    }
    private static JsonArray getSingleParameterJsonArray(String value){
        JsonArray target=new JsonArray();
        target.add(value);
        return target;
    }
}