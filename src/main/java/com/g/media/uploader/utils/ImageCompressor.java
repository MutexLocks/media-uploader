package com.g.media.uploader.utils;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;

import java.io.*;

public class ImageCompressor {

    // 默认压缩参数
    private static final float MIN_QUALITY = 0.1f;
    private static final float MAX_QUALITY = 0.7f;
    private static final float QUALITY_STEP = 0.1f;
    private static final float SCALE_STEP = 0.1f;
    private static final int MAX_ATTEMPTS = 5;

    /**
     * 压缩图片字节数组
     * @param imageBytes 原始图片字节
     * @param maxSize 最大允许大小（字节）
     * @return 压缩后的图片字节数组
     */
    public static byte[] compressImage(byte[] imageBytes, int maxSize) throws IOException {
        // 1. 检查原始大小
        if (imageBytes.length <= maxSize) {
            return imageBytes;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

        // 2. 初始化压缩参数
        float currentQuality = MAX_QUALITY;
        float currentScale = 1.0f;

        // 3. 多阶段压缩尝试
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            outputStream.reset();  // 重置输出流

            try {
                Thumbnails.of(inputStream)
                        .scalingMode(ScalingMode.BILINEAR)  // 双线性插值
                        .scale(currentScale)
                        .outputQuality(currentQuality)
                        .outputFormat("jpg")  // 强制使用JPEG格式
                        .toOutputStream(outputStream);

                // 4. 检查压缩后大小
                byte[] compressedBytes = outputStream.toByteArray();
                if (compressedBytes.length <= maxSize) {
                    return compressedBytes;
                }

                // 5. 动态调整压缩参数
                if (currentQuality > MIN_QUALITY) {
                    currentQuality = Math.max(MIN_QUALITY, currentQuality - QUALITY_STEP);
                } else {
                    currentScale = Math.max(0.3f, currentScale - SCALE_STEP);
                    currentQuality = MAX_QUALITY;  // 重置质量
                }

                // 为下次迭代重置输入流
                inputStream.reset();

            } catch (IOException e) {
                if (i == MAX_ATTEMPTS - 1) throw e;  // 最后一次尝试仍然失败
            }
        }

        // 最后一次尝试结果
        return outputStream.toByteArray();
    }
}
