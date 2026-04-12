package com.leawsic.autoupdate.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leawsic.autoupdate.AutoUpdate;
import com.leawsic.autoupdate.data.config.Config;
import com.leawsic.autoupdate.data.mod.RemoteModList;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class PackVersionAPI {
    static final String baseUrl = Config.getInstance().getInfo().getModListUrl();
    static final String healthUrl = baseUrl + "health";
    static final String versionUrl = baseUrl + "version/";

    public static boolean checkStatus() {
        try {
            Request healthRequest = new Request.Builder().get().url(HttpUrl.get(healthUrl)).addHeader(Base.UA,
                    Base.userAgent).build();
            try (Response response = Base.httpClient.newCall(healthRequest).execute()) {
                return response.code() == HttpURLConnection.HTTP_OK;
            }
        } catch (IOException e) {
            AutoUpdate.LOGGER.warn("Cannot connect to url");
            return false;
        }
    }

    public static String getLatestVersion() {
        return Objects.requireNonNull(getLatestPack()).getPackVersion();
    }

    public static RemoteModList getLatestPack() {
        try {
            URL url = new URL(versionUrl + "latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            connection.setRequestProperty(Base.UA, Base.userAgent);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                Gson gson = new Gson();
                return gson.fromJson(reader, RemoteModList.class);
            } else {
                AutoUpdate.LOGGER.warn("Fail to get the latest pack version--Bad response {}",
                        connection.getResponseCode());
                return null;
            }
        } catch (IOException e) {
            AutoUpdate.LOGGER.warn("Error when fetching remote mods list!");
            return null;
        }
    }

    // 可能返回null
    public static RemoteModList getSpecificPack(String version) {
        try {
            Request request =
                    new Request.Builder().get().addHeader(Base.UA, Base.userAgent).url(versionUrl + version).build();
            try (Response response = Base.httpClient.newCall(request).execute()) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    return gson.fromJson(response.body().string(), RemoteModList.class);
                }
            }
            return null;
        } catch (IOException e) {
            AutoUpdate.LOGGER.warn("Cannot get {} pack list", version);
            return null;
        }
    }
}
