package com.g.media.uploader.component.video;


import com.g.media.uploader.utils.SleepUtils;
import com.g.uploader.VideoUploader;
import lombok.extern.slf4j.Slf4j;
import com.g.media.uploader.exception.UploaderException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public abstract class AbstractVideoUploader implements VideoUploader {

    @Override
    public String homeUrl() {
        return url();
    }

//    @Override
//    public void uploadFile(String videoPath) {
//        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By
//                .xpath(uploadVideoXpath())));
//        button.click();
//        try {
//            TimeUnit.SECONDS.sleep(5);
//            BrowserHelper.paste(videoPath, driver);
//            TimeUnit.SECONDS.sleep(getUploadFileSleepTime(videoPath));
//        } catch (InterruptedException ex) {
//            throw new UploaderException(ex);
//        }
//    }

//    @Override
//    public void closTabs() {
//        try {
//            Set<String> windowHandles = driver.getWindowHandles();
//            for (String handle : windowHandles) {
//                driver.switchTo().window(handle);
//                driver.close();
//            }
//        } catch (Exception ex) {
//            log.error("关闭页面失败", ex);
//        }
//    }


//    public void waitPage() {
//        int count = 1;
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        while (!js.executeScript("return document.readyState").equals("complete")) {
//            // 等待页面加载完成
//            log.info("等待页面加载完成...");
//            SleepUtils.sleepSecond(10);  // 也可以使用更好的等待机制
//            if (count > 100) {
//                break;
//            }
//            log.info("已等待时间：{}秒", 10 * count);
//            count--;
//        }
//    }

    public WebDriverWait getWait(WebDriver driver) {
        return new WebDriverWait(driver, 20);
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
