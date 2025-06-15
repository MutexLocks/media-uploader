package com.g.media.uploader.component.video;


import com.g.media.uploader.exception.UploaderException;
import com.g.media.uploader.utils.SleepUtils;
import com.g.uploader.VideoUploader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
public abstract class AbstractVideoUploader implements VideoUploader {

    @Override
    public String homeUrl() {
        return url();
    }

    public WebDriverWait getWait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void checkClickResult(Supplier<WebElement> supplier, WebDriver driver) {
        int submitCount = 0;
        while (true) {
            try {
                SleepUtils.sleepSecond(10);
                // 判断是否点击成功，点击成功，页面会跳转，不会找到 发布 按钮
                WebElement button = supplier.get();
                button.click();
                SleepUtils.sleepSecond(30);
            } catch (Exception ex) {
                break;
            }
            submitCount++;
            if (submitCount > 3) {
                break;
            }
        }
        try {
            supplier.get();
            // 找到按钮，说明上传失败
            log.error("上传视频失败");
            throw new UploaderException("上传视频失败");
        } catch (NoSuchElementException ex) {
            log.error("上传视频成功");
        }
    }

    public void checkClickResult(String xpath, WebDriver driver) {
        int submitCount = 0;
        while (true) {
            try {
                SleepUtils.sleepSecond(10);
                // 判断是否点击成功，点击成功，页面会跳转，不会找到 发布 按钮
                WebElement button = driver.findElement(By
                        .xpath(xpath));
                button.click();
                SleepUtils.sleepSecond(30);
            } catch (Exception ex) {
                break;
            }
            submitCount++;
            if (submitCount > 3) {
                break;
            }
        }
        try {
            driver.findElement(By
                    .xpath(xpath));
            // 找到按钮，说明上传失败
            log.error("上传视频失败");
            throw new UploaderException("上传视频失败");
        } catch (NoSuchElementException ex) {
            log.error("上传视频成功");
        }
    }
}
