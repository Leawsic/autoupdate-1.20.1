package com.leawsic.autoupdate.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.leawsic.autoupdate.AutoUpdate;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.io.IOException;

public class ModrinthAPI {
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String baseUrl="https://api.modrinth.com/v2/";
    private final String userAgent="AutoUpdate/"+ AutoUpdate.MOD_VER;

    public ModrinthAPI(){
        this.httpClient=new OkHttpClient();
        this.gson=new GsonBuilder().setPrettyPrinting().create();
    }

    public String searchMods(String query,String limit) throws IOException {
        HttpUrl.Builder urlBuilder=
                HttpUrl.parse(baseUrl+"search").newBuilder().addQueryParameter("query",query).addQueryParameter(
                        "limit",limit);

        JsonArray facetsArray=new JsonArray();
        return "";
    }
}
