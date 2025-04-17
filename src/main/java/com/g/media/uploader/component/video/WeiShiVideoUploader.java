package com.g.media.uploader.component.video;

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

@Component
@Slf4j
public class WeiShiVideoUploader extends AbstractVideoUploader {

    @Override
    public String url() {
        return "https://media.weishi.qq.com/";
    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//div[contains(@class, 'user-name')]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//div[contains(@class, 'container')]/img"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);

        return accountInfo;
    }


    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/WeiShi.png", this.getClass());
    }

    @Override
    public void setVideoTitle(String title, WebDriver driver) {

    }

    @Override
    public void setVideoCover(String imagePath, WebDriver driver) {
        // no cover setting
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.presenceOfElementLocated(By
                .xpath("//textarea[@placeholder=\"这一刻的想法\"]")));
        input.sendKeys(description);
        SleepUtils.sleepSecond(3);

    }

    @Override
    public String name() {
        return PlatformEnum.WeiShi.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.WeiShi.getCode();
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
    public void submit(WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//button[@class='ant-btn ant-btn-primary']")));
        button.click();
        checkClickResult("//button[@class='ant-btn ant-btn-primary']", driver);
    }
}
