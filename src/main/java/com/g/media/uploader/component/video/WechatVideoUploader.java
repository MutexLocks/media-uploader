package com.g.media.uploader.component.video;

import com.g.media.uploader.exception.UploaderException;
import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.media.uploader.utils.SleepUtils;
import com.g.uploader.model.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WechatVideoUploader extends AbstractVideoUploader {

    @Override
    public String name() {
        return PlatformEnum.Wechat.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.Wechat.getCode();
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/WeChat.png", this.getClass());
    }

    @Override
    public void setVideoCover(String imagePath, WebDriver driver) {
    }

    @Override
    public String url() {
        return "https://channels.weixin.qq.com/platform/post/create";
    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//h2[@class=\"finder-nickname\"]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//img[@class=\"avatar\"]"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);
        
        return accountInfo;
    }

    @Override
    public String homeUrl() {
        return "https://channels.weixin.qq.com/platform";
    }

    @Override
    public void setVideoTitle(String title, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.presenceOfElementLocated(By
                .xpath("//input[contains(@placeholder, '概括')]")));
        title = title
                .replaceAll("，", " ")
                .replaceAll(",", " ");
        //视频号标题至少要6个字
        title = addBlank(title);
        input.sendKeys(title);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
    }

    private String addBlank(String title) {
        if (title.length() >= 6) {
            return title;
        }
        int blankCount = 6 - title.length();
        StringBuilder titleBuilder = new StringBuilder(title);
        for (int i = 0; i < blankCount; i++) {
            titleBuilder.append(" ");
        }
        return titleBuilder.toString();
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {
        try {
            WebElement input = getWait(driver).until(ExpectedConditions.presenceOfElementLocated(By
                    .xpath("//div[@class=\"input-editor\"]")));
            input.sendKeys(description);
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ex) {
            log.error("设置视频描述失败");
            throw new UploaderException(ex);
        }
    }

    @Override
    public void uploadFile(String videoPath, WebDriver driver) {
        WebElement file = getWait(driver).until(ExpectedConditions
                .presenceOfElementLocated(
                        By.cssSelector("input[type='file']")));
        file.sendKeys(videoPath);
        int waitTime = FileUtils.getUploadFileSleepTime(videoPath).intValue();
        SleepUtils.sleepSecond(waitTime);
        log.info("等待时间：{}s", waitTime);
    }


    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By.className("input-editor")));
        input.sendKeys(videoContentTopic);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
    }

    @Override
    public void submit(WebDriver driver) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//*[@id=\"container-wrap\"]/div[2]/div/div/div[1]/div[3]/div/div[2]/div[2]/div[9]/div[5]/span/div/button")));
        button.click();
        checkClickResult("//*[@id=\"container-wrap\"]/div[2]/div/div/div[1]/div[3]/div/div[2]/div[2]/div[9]/div[5]/span/div/button", driver);
    }

}


