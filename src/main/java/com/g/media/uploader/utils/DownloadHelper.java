package com.g.media.uploader.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@Slf4j
public class DownloadHelper {
    public static String downloadImageToBase64(String imageUrl) {
        try {

            URL url = new URL(imageUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            httpConn.setRequestProperty("Accept", "image/webp,image/apng,image/vnd.wap.png,image/png,image/svg+xml,image/gif,image/jpeg,image/webp");
            // 确保响应码为 200 表示成功
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[4096];
                int bytesRead = -1;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                byte[] imageBytes = outputStream.toByteArray();
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);

                inputStream.close();
                outputStream.close();

                return base64Image;
            } else {
                log.error("No image found at the given URL.");
            }
        } catch (Exception ex) {
            log.error("failed to download image: {}", imageUrl, ex);
        }
        return  null;
    }
}
