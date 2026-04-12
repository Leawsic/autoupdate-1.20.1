package com.leawsic.autoupdate.data.api;

import com.leawsic.autoupdate.AutoUpdate;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class Base {
    public static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    static final String UA = "User-Agent";
    static final String userAgent = "AutoUpdate/" + AutoUpdate.MOD_VER;
}
