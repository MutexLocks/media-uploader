package com.g.media.uploader;

import cn.hutool.json.JSONUtil;
import com.g.media.uploader.model.PublishVideoDTO;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PublishVideoTest {

    private static final String UPLOAD_URL = "http://192.168.100.14:80/api/v1/upload/video/chunk";
    private static final String MERGE_URL = "http://192.168.100.14:80/api/v1/chunk/merge";
    private static final String PUBLISH_URL = "http://192.168.100.14:80/api/v1/publish/video";

    private static final String API_KEY = "sk-1edd2c68579b28e8ee151a76cb172a4e"; // 替换为你的 sk
    private static final int CHUNK_SIZE = 5 * 1024 * 1024; // 5MB per chunk


    public static void main(String[] args) {
        String filePath = "/home/g/Desktop/api.mp4";
        String uuid = UUID.randomUUID().toString();
        String title = "使用API上传视频到抖音";
        String description = "使用UTMatrix的API上传视频到抖音等平台#自动化 #API";
        // 账号id
        List<Long> accountId = Collections.singletonList(1L);
        // 上传视频
        uploadAndMerge(filePath, uuid);
        // 发布视频
        publishVideo(title, description, accountId, null, uuid, null);
    }

    public static void uploadAndMerge(String filePath, String uuid) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.err.println("❌ File not found: " + filePath);
            return;
        }

        try {
            long fileSize = Files.size(path);
            int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);

            System.out.println("📁 File: " + filePath);
            System.out.println("📏 Size: " + fileSize + " bytes | Chunks: " + totalChunks);
            System.out.println("🆔 Upload UUID: " + uuid);

            // Step 1: 上传所有分片
            boolean allUploaded = true;
            try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
                byte[] buffer = new byte[CHUNK_SIZE];
                for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
                    int bytesRead = raf.read(buffer);
                    if (bytesRead <= 0) break;
                    byte[] chunkData = (bytesRead == CHUNK_SIZE) ? buffer : java.util.Arrays.copyOf(buffer, bytesRead);

                    if (!uploadChunk(uuid, chunkIndex, chunkData)) {
                        System.err.println("❌ Failed to upload chunk " + chunkIndex);
                        allUploaded = false;
                        break;
                    }
                    System.out.println("✅ Uploaded chunk " + chunkIndex + "/" + (totalChunks - 1));
                }
            }

            if (!allUploaded) {
                System.err.println("🛑 Upload failed. Skip merging.");
                return;
            }

            // Step 2: 调用合并接口
            System.out.println("🔗 Triggering merge...");
            String result = mergeChunks(uuid, "VIDEO", "PUBLISH");
            if (result != null) {
                System.out.println("🎉 Merge successful! Result: " + result);
            } else {
                System.err.println("💥 Merge failed!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean publishVideo(
            String title,
            String description,
            List<Long> accountIdList,
            String extraConfig,
            String uuid,
            String scheduledTime) {

        try {
            PublishVideoDTO publishVideoDTO = new PublishVideoDTO();
            publishVideoDTO.setUuid(uuid);
            publishVideoDTO.setDescription(description);
            publishVideoDTO.setTitle(title);
            publishVideoDTO.setAccountIdList(accountIdList);

            // 发送请求
            URL url = new URL(PUBLISH_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("X-API-KEY", API_KEY);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = JSONUtil.toJsonStr(publishVideoDTO).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code == 200 || code == 201) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String resp = reader.lines().reduce("", String::concat);
                    System.out.println("📌 Publish Response: " + resp);
                }
                return true;
            } else {
                System.err.println("HTTP Error: " + code);
                try (BufferedReader er = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    er.lines().forEach(System.err::println);
                }
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean uploadChunk(String uuid, int chunkIndex, byte[] chunkData) throws IOException {
        String boundary = "----ChunkBoundary" + System.currentTimeMillis();
        URL url = new URL(UPLOAD_URL + "?uuid=" + URLEncoder.encode(uuid, "UTF-8") +
                "&chunkIndex=" + chunkIndex);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("X-API-KEY", API_KEY);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

            // 写入 multipart 头部
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"chunk.bin\"").append("\r\n");
            writer.append("Content-Type: application/octet-stream").append("\r\n");
            writer.append("\r\n");
            writer.flush();

            // 写入二进制数据
            os.write(chunkData);
            os.flush();

            // 结束 boundary
            writer.append("\r\n--" + boundary + "--\r\n");
            writer.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200 || responseCode == 201) {
            // 读取响应（可选）
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // 可解析 IResponse<Void> JSON
                }
            }
            return true;
        } else {
            System.err.println("HTTP Error: " + responseCode);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                reader.lines().forEach(System.err::println);
            }
            return false;
        }
    }

    private static String mergeChunks(String uuid, String mediaType, String taskType) {
        try {
            String urlStr = MERGE_URL +
                    "?uuid=" + URLEncoder.encode(uuid, "UTF-8") +
                    "&mediaType=" + URLEncoder.encode(mediaType, "UTF-8") +
                    "&taskType=" + URLEncoder.encode(taskType, "UTF-8");

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-API-KEY", API_KEY);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    // 假设返回 JSON: {"code":200,"data":"final_video_id_123","msg":"success"}
                    // 这里直接返回原始字符串，你可按需解析
                    return response.toString();
                }
            } else {
                System.err.println("Merge HTTP Error: " + responseCode);
                try (BufferedReader er = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    er.lines().forEach(System.err::println);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}