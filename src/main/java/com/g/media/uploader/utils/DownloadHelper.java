package com.g.media.uploader.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

@Slf4j
public class DownloadHelper {
    public static String downloadImageToBase64(String imageUrl) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        final int MAX_REDIRECTS = 5; // 最大重定向次数，防止无限循环
        int redirectCount = 0;

        try {
            String currentUrl = imageUrl;
            while (redirectCount < MAX_REDIRECTS) {
                URL url = new URL(currentUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setInstanceFollowRedirects(false); // 禁用自动重定向，手动处理
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                connection.setRequestProperty("Accept", "image/*"); // 简化 Accept 头
                connection.setConnectTimeout(10000); // 10秒连接超时
                connection.setReadTimeout(30000);    // 30秒读取超时

                int responseCode = connection.getResponseCode();

                // 处理重定向 (301, 302, 307 等)
                if (responseCode >= HttpURLConnection.HTTP_MOVED_PERM && responseCode <= HttpURLConnection.HTTP_MOVED_TEMP) {
                    String location = connection.getHeaderField("Location");
                    if (location == null || location.isEmpty()) {
                        log.error("Redirect location is missing for URL: {}", currentUrl);
                        return null;
                    }
                    currentUrl = location;
                    redirectCount++;
                    // 关闭当前连接，准备下一次重定向
                    connection.disconnect();
                    continue;
                }

                // 仅处理成功响应 (HTTP 200)
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.getInputStream();
                    outputStream = new ByteArrayOutputStream();

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    byte[] imageBytes = outputStream.toByteArray();
                    String mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes));
                    String base64Prefix = "data:" + (mimeType != null ? mimeType : "image/jpeg") + ";base64,";
                    return base64Prefix + Base64.getEncoder().encodeToString(imageBytes);
                } else {
                    log.error("Unexpected HTTP status code: {} for URL: {}", responseCode, currentUrl);
                    return null;
                }
            }
            log.error("Too many redirects (>{}) for URL: {}", MAX_REDIRECTS, imageUrl);
            return null;
        } catch (Exception ex) {
            log.error("Failed to download image: {}", imageUrl, ex);
            return null;
        } finally {
            // 确保资源关闭
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (connection != null) connection.disconnect();
            } catch (IOException ex) {
                log.warn("Error closing resources", ex);
            }
        }
    }

}
