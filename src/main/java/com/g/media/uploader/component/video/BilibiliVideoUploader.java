package com.g.media.uploader.component.video;


import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.media.uploader.utils.SleepUtils;
import com.g.uploader.model.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class BilibiliVideoUploader extends AbstractVideoUploader {

    @Override
    public String url() {
        return "https://member.bilibili.com/platform/upload/video/frame?page_from=creative_home_top_upload";
    }

    @Override
    public String logoBase64() {
        log.info("classloader: " + this.getClass().getClassLoader());
        log.info("resource: " + this.getClass().getResource("logo/Bilibili.png"));
        log.info("asStream: " + this.getClass().getResourceAsStream("logo/Bilibili.png"));

        return FileUtils.image2Base64("logo/Bilibili.png", this.getClass());
    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//span[@class=\"home-top-msg-name\"]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//div[@class=\"home-head\"]/img"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);
        return accountInfo;
    }

    @Override
    public String homeUrl() {
        return "https://account.bilibili.com/account/home";
    }

    @Override
    public String name() {
        return PlatformEnum.Bilibili.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.Bilibili.getCode();
    }


    @Override
    public void setVideoCover(String imagePath, WebDriver driver) {
        try {
            List<WebElement> covers = driver.findElements(By.xpath("//div[@class=\"cover-preview-item\"]"));
            WebElement webElement = covers.get(covers.size() - 1);
            webElement.click();
        } catch (Exception ex) {
            log.warn("选择封面失败");
        }
    }

    @Override
    public void uploadFile(String videoPath, WebDriver driver) {
        // 取消未上传的视频
        try {
            TimeUnit.SECONDS.sleep(3);
            WebElement cancel = driver.findElement(By.xpath("//div[text()=\"不用了\"]"));
            cancel.click();
        } catch (Exception ex) {
            log.info("没有<不用了>按钮，不做处理");
        }

//        File uploadFile = new File(videoPath);
//
//        ((RemoteWebDriver) driver).setFileDetector(new LocalFileDetector());
        WebElement file = getWait(driver).until(ExpectedConditions
                .presenceOfElementLocated(
                        By.cssSelector("input[type='file']")));
        file.sendKeys(videoPath);

        int count = 0;
        while (true) {
            try {
                driver.findElement(By.xpath("//span[text()=\"上传完成\"]"));
                log.info("上传完成");
                break;
            } catch (NoSuchElementException e) {
                SleepUtils.sleepSecond(1);
                count = count + 1;
                log.info("上传中...");
            }
            if (count > 300) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        int count = 0;
        System.out.println(count++);
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//div[@class=\"ql-editor ql-blank\"]")));
        input.click();
        input.sendKeys(description.trim());
    }

    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {
        WebElement hotTag = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("//div[@class=\"hot-tag-container\"]")));//        WebElement input = wait.until(ExpectedConditions
        hotTag.click();
    }


    @Override
    public void setVideoTitle(String title, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//input[@maxlength=\"80\"]")));
        input.clear();
        SleepUtils.sleepSecond(1);
        input.sendKeys(title);
    }


    @Override
    public void submit(WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//span[@class=\"submit-add\"]")));
        SleepUtils.sleepSecond(3);
        button.click();
        checkClickResult("//span[@class=\"submit-add\"]", driver);
    }
}
