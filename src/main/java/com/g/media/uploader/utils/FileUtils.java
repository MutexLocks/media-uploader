package com.g.media.uploader.utils;

import com.g.media.uploader.exception.UploaderException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
public class FileUtils {
    public static Long getFileSizeInMB(String filePath) {
        int mb = 1024 * 1024;//定义MB的计算常量
        try {
            // 加载文件
            File f = new File(filePath);
            long size = f.length();
            Float fileSize = size / (float) mb;
            // 格式化小数
            return fileSize.longValue();
        } catch (Exception e) {
            throw new UploaderException("failed to get files size");
        }
    }


    public static Long getUploadFileSleepTime(String filePath) {
        long fileSizeInMB = FileUtils.getFileSizeInMB(filePath);
        if (fileSizeInMB == 0) {
            return 30L;
        }
        // 按1m/s
        Long sleepTime = fileSizeInMB;
        log.info("上传文件大小：{} MB", fileSizeInMB);
        log.info("等待时间：{} 秒", sleepTime);
        return sleepTime;
    }

    public static String image2Base64(String imagePath, Class<?> contextClass) {
        ClassLoader classLoader = contextClass.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(imagePath)) {
            if (inputStream == null) {
                throw new UploaderException("资源未找到: " + imagePath);
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            buffer.flush();
            byte[] imageBytes = buffer.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (IOException e) {
            throw new UploaderException("读取图片失败: " + imagePath, e);
        }
    }

}
