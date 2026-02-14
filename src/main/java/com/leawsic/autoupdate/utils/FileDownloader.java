package com.leawsic.autoupdate.utils;

import com.leawsic.autoupdate.AutoUpdate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileDownloader {
    public static boolean downloadFile(String fileUrl, File localFile, String userAgent, OkHttpClient httpClient) throws IOException {
        Request request = new Request.Builder()
                .url(fileUrl)
                .addHeader("User-Agent", userAgent)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            assert response.body()!=null;
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
}
