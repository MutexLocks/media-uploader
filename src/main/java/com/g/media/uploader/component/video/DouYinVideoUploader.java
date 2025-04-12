package com.g.media.uploader.component.video;

import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.BrowserHelper;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.media.uploader.utils.SleepUtils;
import com.g.uploader.model.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DouYinVideoUploader extends AbstractVideoUploader {

    @Override
    public String url() {
        return "https://creator.douyin.com/creator-micro/content/upload";
    }

    @Override
    public String homeUrl() {
        return "https://creator.douyin.com/creator-micro/home";
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/DouYin.png", this.getClass());
    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//div[contains(@class, 'name-')]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//div[contains(@class, 'avatar-')]/img"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);

        return accountInfo;
    }

    @Override
    public String name() {
        return PlatformEnum.DouYin.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.DouYin.getCode();
    }


    @Override
    public void uploadFile(String videoPath, WebDriver driver) {
        WebDriverWait wait = getWait(driver);
        wait.until(ExpectedConditions
                        .presenceOfElementLocated(By.xpath("//input[@type=\"file\"]")))
                .sendKeys(videoPath);
        Long count = FileUtils.getUploadFileSleepTime(videoPath);
        SleepUtils.sleepSecond(count.intValue());
        log.info("出现烦人弹窗，打开新窗口，重新上传");

        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        String jsOpenNewWindow = "window.open('" + url() + "');";
        jsExecutor.executeScript(jsOpenNewWindow);

        wait.until(ExpectedConditions
                        .presenceOfElementLocated(By.xpath("//input[@type=\"file\"]")))
                .sendKeys(videoPath);
        SleepUtils.sleepSecond(count.intValue());
    }

    @Override
    public void setVideoCover(String imagePath, WebDriver driver) {
        WebDriverWait wait = getWait(driver);
        WebElement cover = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//div[text()=\"选择封面\"]")));
        cover.click();
        wait.until(ExpectedConditions
                        .presenceOfElementLocated(By.xpath("//input[@type=\"file\"]")))
                .sendKeys(imagePath);
        SleepUtils.sleepSecond(2);
        WebElement submitButton = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//span[text()=\"完成\"]")));
        submitButton.click();
        SleepUtils.sleepSecond(10);
    }

    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("//div[text()=\"#添加话题\"]")));
        String[] topics = videoContentTopic.split("#");
        for (String topic : topics) {
            if (StringUtils.isBlank(topic)) {
                continue;
            }
            input.click();
            try {
                SleepUtils.sleepSecond(1);
                BrowserHelper.paste(topic.trim(), driver);
                SleepUtils.sleepSecond(2);
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//div[@data-placeholder='添加作品简介']")));
        input.sendKeys(description);
    }

    @Override
    public void setVideoTitle(String title, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//input[@placeholder='填写作品标题，为作品获得更多流量']")));
        input.sendKeys(title);
    }

    @Override
    public void submit(WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//button[text()=\"发布\"]")));
        button.click();
        checkClickResult("//button[text()=\"发布\"]", driver);
    }

}
