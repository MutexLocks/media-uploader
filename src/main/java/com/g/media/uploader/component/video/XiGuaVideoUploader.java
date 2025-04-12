package com.g.media.uploader.component.video;

import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.g.media.uploader.exception.UploaderException;
import com.g.uploader.model.AccountInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class XiGuaVideoUploader extends AbstractVideoUploader {

    @Override
    public String url() {
        return "https://studio.ixigua.com/upload?from=post_article";
    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        return null;
    }

    @Override
    public String name() {
        return PlatformEnum.XiGua.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.XiGua.getCode();
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/XiGua.png", this.getClass());
    }

    @Override
    public void uploadFile(String videoPath, WebDriver driver) {

    }

    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("//input[@placeholder=\"输入合适的话题\"]")));
        WebElement baseTitle = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("//div[@class=\"video-from-base-title\"]")));
        String[] topics = videoContentTopic.split("#");
        for (String topic : topics) {
            if (StringUtils.isBlank(topic)) {
                continue;
            }
            input.click();
            input.sendKeys(topic.trim());
            try {
                TimeUnit.SECONDS.sleep(1);
                baseTitle.click();
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//div[@data-editor=\"abstract\"]")));
        input.sendKeys(description);
    }

    @Override
    public void setVideoTitle(String title, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//div[@data-editor=\"title\"]")));
        input.sendKeys(title);
    }

    @Override
    public void setVideoCover(String imagePath, WebDriver driver) {
        WebDriverWait wait = getWait(driver);
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By
                .xpath("//div[@class=\"m-xigua-upload\"]")));

        button.click();

        try {
            TimeUnit.SECONDS.sleep(3);
            WebElement next = wait.until(ExpectedConditions.elementToBeClickable(By
                    .xpath("//div[@class=\"m-button red  \"]")));
            next.click();
            TimeUnit.SECONDS.sleep(10);
            // 确定
            next = wait.until(ExpectedConditions.elementToBeClickable(By
                    .xpath("//button[@class=\"btn-l btn-sure ml16 \"]")));
            next.click();
            TimeUnit.SECONDS.sleep(10);


            next = wait.until(ExpectedConditions.elementToBeClickable(By
                    .xpath("/html/body/div[6]/div/div[2]/div/div[2]/button[2]")));
            next.click();
            TimeUnit.SECONDS.sleep(10);

        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }

        try {
            // 原创
            WebElement originalButton = wait.until(ExpectedConditions.elementToBeClickable(By
                    .xpath("//span[text()=\"原创\"]")));
            originalButton.click();
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception ex) {
            log.warn("无法点击原创");
        }

    }

    @Override
    public void submit(WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//button[@class=\"action-footer-btn submit m-button red  \"]")));
        try {
            TimeUnit.SECONDS.sleep(3);
            button.click();
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
    }
}
