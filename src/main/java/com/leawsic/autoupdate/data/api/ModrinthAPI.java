package com.leawsic.autoupdate.data.api;

import com.google.gson.*;
import com.leawsic.autoupdate.AutoUpdate;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class ModrinthAPI {
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String baseUrl = "https://api.modrinth.com/v2/";
    private final String userAgent = "AutoUpdate/" + AutoUpdate.MOD_VER + " (3775717540@qq.com)";

    public ModrinthAPI() {
        // 添加超时设置：连接超时10秒，读取超时30秒，写入超时30秒
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * 搜索模组
     */
    public JsonObject searchMods(String query, String gameVersion, String loader, int limit) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "search").newBuilder()
                .addQueryParameter("query", query)
                .addQueryParameter("limit", String.valueOf(limit));

        // 构建facets过滤条件 - 根据API文档使用正确的格式
        JsonArray facetsArray = getJsonElements(gameVersion, loader);

        // 不需要URL编码，直接使用JSON字符串
        urlBuilder.addQueryParameter("facets", facetsArray.toString());

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("User-Agent", userAgent)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                AutoUpdate.LOGGER.error("Failed to search mods: HTTP {} - URL: {}", 
                        response.code(), response.request().url());
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }

    private static @NotNull JsonArray getJsonElements(String gameVersion, String loader) {
        JsonArray facetsArray = new JsonArray();

        // 项目类型过滤
        JsonArray projectTypeFacet = new JsonArray();
        projectTypeFacet.add("project_type:mod");
        facetsArray.add(projectTypeFacet);

        if (gameVersion != null && !gameVersion.isEmpty()) {
            JsonArray gameVersionFacet = new JsonArray();
            gameVersionFacet.add("versions:" + gameVersion);
            facetsArray.add(gameVersionFacet);
        }

        if (loader != null && !loader.isEmpty()) {
            JsonArray loaderFacet = new JsonArray();
            loaderFacet.add("categories:" + loader);
            facetsArray.add(loaderFacet);
        }
        return facetsArray;
    }

    /**
     * 获取模组详细信息
     */
    public JsonObject getModInfo(String projectIdOrSlug) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "project/" + projectIdOrSlug)
                .addHeader("User-Agent", userAgent)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                AutoUpdate.LOGGER.error("Failed to get mod info for {}: HTTP {} - URL: {}", 
                        projectIdOrSlug, response.code(), response.request().url());
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonObject();
        }
    }

    /**
     * 获取模组版本列表
     */
    public JsonArray getModVersions(String projectIdOrSlug, String gameVersion, String loader) throws IOException {
        // 使用正确的/version端点
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "project/" + projectIdOrSlug + "/version").newBuilder();
        
        if (gameVersion != null && !gameVersion.isEmpty()) {
            // 使用正确的JSON数组格式，不需要URL编码
            urlBuilder.addQueryParameter("game_versions", "[\"" + gameVersion + "\"]");
        }
        
        if (loader != null && !loader.isEmpty()) {
            // 使用正确的JSON数组格式，不需要URL编码
            urlBuilder.addQueryParameter("loaders", "[\"" + loader + "\"]");
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("User-Agent", userAgent)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                AutoUpdate.LOGGER.error("Failed to get versions for {}: HTTP {} - URL: {}", 
                        projectIdOrSlug, response.code(), response.request().url());
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            return JsonParser.parseString(responseBody).getAsJsonArray();
        }
    }

    /**
     * 下载模组文件
     */
    public boolean downloadModFile(String fileUrl, File localFile) throws IOException {
        Request request = new Request.Builder()
                .url(fileUrl)
                .addHeader("User-Agent", userAgent)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                AutoUpdate.LOGGER.error("Failed to download file from {}: HTTP {} - {}", 
                        fileUrl, response.code(), response.message());
                throw new IOException("Unexpected code " + response);
            }
            
            // 创建目录
            File parentDir = localFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
            
            // 下载文件
            try (InputStream inputStream = response.body().byteStream();
                 FileOutputStream outputStream = new FileOutputStream(localFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return true;
        }
    }

    /**
     * 根据模组ID和指定版本下载模组
     */
    public boolean downloadModByIdAndVersion(String modId, String targetVersion, String gameVersion, String loader, File modsDir) throws IOException {
        try {
            // 获取模组信息
            JsonObject modInfo = getModInfo(modId);
            if (modInfo == null) {
                AutoUpdate.LOGGER.warn("Failed to get mod info for: {}", modId);
                return false;
            }

            // 获取版本列表
            JsonArray versions = getModVersions(modId, gameVersion, loader);
            if (versions == null || versions.isEmpty()) {
                AutoUpdate.LOGGER.warn("No compatible versions found for mod: {}", modId);
                return false;
            }

            // 查找指定版本
            JsonObject targetVersionObj = null;
            for (int i = 0; i < versions.size(); i++) {
                JsonObject versionObj = versions.get(i).getAsJsonObject();
                String versionNumber = versionObj.get("version_number").getAsString();
                if (versionNumber.equals(targetVersion)) {
                    targetVersionObj = versionObj;
                    break;
                }
            }

            if (targetVersionObj == null) {
                AutoUpdate.LOGGER.warn("Target version {} not found for mod: {}, using latest version", targetVersion, modId);
                // 如果找不到指定版本，回退到最新版本
                targetVersionObj = versions.get(0).getAsJsonObject();
            }

            JsonArray files = targetVersionObj.getAsJsonArray("files");
            if (files == null || files.isEmpty()) {
                AutoUpdate.LOGGER.warn("No files found for mod version: {} (mod: {})", targetVersion, modId);
                return false;
            }

            // 获取主文件
            JsonObject mainFile = files.get(0).getAsJsonObject();
            String downloadUrl = mainFile.get("url").getAsString();
            String filename = mainFile.get("filename").getAsString();

            // 下载文件
            File outputFile = new File(modsDir, filename);
            AutoUpdate.LOGGER.info("Downloading mod {} (version: {}) to {}", filename, targetVersion, outputFile.getAbsolutePath());
            
            return downloadModFile(downloadUrl, outputFile);

        } catch (Exception e) {
            AutoUpdate.LOGGER.error("Error downloading mod {} (version: {}): {}", modId, targetVersion, e.getMessage());
            return false;
        }
    }

    /**
     * 根据模组ID搜索并下载最新版本
     */
    public boolean downloadModById(String modId, String gameVersion, String loader, File modsDir) throws IOException {
        try {
            // 获取模组信息
            JsonObject modInfo = getModInfo(modId);
            if (modInfo == null) {
                AutoUpdate.LOGGER.warn("Failed to get mod info for: {}", modId);
                return false;
            }

            // 获取版本列表
            JsonArray versions = getModVersions(modId, gameVersion, loader);
            if (versions == null || versions.isEmpty()) {
                AutoUpdate.LOGGER.warn("No compatible versions found for mod: {}", modId);
                return false;
            }

            // 获取最新版本（第一个通常是最新的）
            JsonObject latestVersion = versions.get(0).getAsJsonObject();
            JsonArray files = latestVersion.getAsJsonArray("files");
            if (files == null || files.isEmpty()) {
                AutoUpdate.LOGGER.warn("No files found for mod versions: {}", modId);
                return false;
            }

            // 获取主文件
            JsonObject mainFile = files.get(0).getAsJsonObject();
            String downloadUrl = mainFile.get("url").getAsString();
            String filename = mainFile.get("filename").getAsString();

            // 下载文件
            File outputFile = new File(modsDir, filename);
            AutoUpdate.LOGGER.info("Downloading mod {} (latest version) to {}", filename, outputFile.getAbsolutePath());
            
            return downloadModFile(downloadUrl, outputFile);

        } catch (Exception e) {
            AutoUpdate.LOGGER.error("Error downloading mod {}: {}", modId, e.getMessage());
            return false;
        }
    }

    /**
     * 根据模组名称搜索并下载
     */
    public boolean downloadModByName(String modName, String gameVersion, String loader, File modsDir) throws IOException {
        try {
            // 搜索模组
            JsonObject searchResult = searchMods(modName, gameVersion, loader, 5);
            if (searchResult == null) {
                AutoUpdate.LOGGER.warn("No search results for: {}", modName);
                return false;
            }

            JsonArray hits = searchResult.getAsJsonArray("hits");
            if (hits == null || hits.isEmpty()) {
                AutoUpdate.LOGGER.warn("No hits found for search: {}", modName);
                return false;
            }

            // 获取第一个匹配的模组
            JsonObject firstHit = hits.get(0).getAsJsonObject();
            String modId = firstHit.get("project_id").getAsString();
            String slug = firstHit.get("slug").getAsString();
            
            AutoUpdate.LOGGER.info("Found mod: {} (ID: {})", slug, modId);

            // 下载模组
            return downloadModById(modId, gameVersion, loader, modsDir);

        } catch (Exception e) {
            AutoUpdate.LOGGER.error("Error searching and downloading mod {}: {}", modName, e.getMessage());
            return false;
        }
    }
}