package com.g.media.uploader.utils;

import org.imgscalr.Scalr;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class ImageCompressor {

    private static final float MIN_QUALITY = 0.1f;
    private static final float MAX_QUALITY = 0.7f;
    private static final float QUALITY_STEP = 0.1f;
    private static final float SCALE_STEP = 0.1f;
    private static final int MAX_ATTEMPTS = 5;

    /**
     * 压缩图片字节数组（使用 imgscalr + ImageIO）
     * @param imageBytes 原始图片字节
     * @param maxSize 最大允许大小（字节）
     * @return 压缩后的图片字节数组
     * @throws IOException IO异常
     */
    public static byte[] compressImage(byte[] imageBytes, int maxSize) throws IOException {
        if (imageBytes.length <= maxSize) {
            return imageBytes;
        }

        float currentQuality = MAX_QUALITY;
        float currentScale = 1.0f;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // 解码为 BufferedImage
            BufferedImage originalImage;
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                originalImage = ImageIO.read(inputStream);
            }

            if (originalImage == null) {
                throw new IOException("无法解析输入图像");
            }

            // 缩放
            int scaledWidth = (int) (originalImage.getWidth() * currentScale);
            int scaledHeight = (int) (originalImage.getHeight() * currentScale);
            BufferedImage scaledImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, scaledWidth, scaledHeight);

            // 压缩并写入字节数组
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                writeJpeg(scaledImage, outputStream, currentQuality);
                byte[] compressedBytes = outputStream.toByteArray();

                if (compressedBytes.length <= maxSize) {
                    return compressedBytes;
                }

                // 动态调整压缩参数
                if (currentQuality > MIN_QUALITY) {
                    currentQuality = Math.max(MIN_QUALITY, currentQuality - QUALITY_STEP);
                } else {
                    currentScale = Math.max(0.3f, currentScale - SCALE_STEP);
                    currentQuality = MAX_QUALITY; // 重置质量
                }
            }
        }

        // 最后失败也返回最后一次尝试结果（不抛出异常）
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            writeJpeg(image, outputStream, MIN_QUALITY);
            return outputStream.toByteArray();
        }
    }

    // 使用指定质量写入 JPEG 格式图片
    private static void writeJpeg(BufferedImage image, OutputStream outputStream, float quality) throws IOException {
        // 检测透明度并转换为 RGB 格式（不透明）
        if (hasTransparency(image)) {
            image = convertToOpaque(image, Color.WHITE);  // 用白色背景替换透明区域
        }

        // 确保图像是 RGB 格式（兼容 JPEG）
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            image = convertToRGB(image);
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("未找到 JPEG ImageWriter");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality); // 范围：0.0f - 1.0f

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();  // 确保资源被释放
        }
    }

    // 检测图像是否有透明度
    private static boolean hasTransparency(BufferedImage image) {
        return image.getTransparency() != Transparency.OPAQUE;
    }

    // 将透明图像转换为不透明图像（带背景色）
    private static BufferedImage convertToOpaque(BufferedImage image, Color bgColor) {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = newImage.createGraphics();
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return newImage;
    }

    // 将图像转换为标准 RGB 格式
    private static BufferedImage convertToRGB(BufferedImage image) {
        // 如果已经是 TYPE_INT_RGB 类型，直接返回
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // 创建 RGB 类型的新图像
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 避免使用可能依赖 X11 的 drawImage，直接手动复制像素
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                rgbImage.setRGB(x, y, argb);
            }
        }

        return rgbImage;
    }

}
