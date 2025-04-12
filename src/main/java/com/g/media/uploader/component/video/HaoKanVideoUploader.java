package com.g.media.uploader.component.video;

import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.SleepUtils;
import com.g.media.uploader.exception.UploaderException;
import com.g.media.uploader.utils.FileUtils;
import com.g.uploader.model.AccountInfo;

import com.g.media.uploader.utils.BrowserHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class HaoKanVideoUploader extends AbstractVideoUploader {

    @Override
    public String url() {
        return "https://dream.haokan.com/author/upload";
    }


    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        driver.findElement(By
                .xpath("//img[@class='portrait']"));
        driver.get(homeUrl());
        SleepUtils.sleepSecond(3);

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
        return "https://baijiahao.baidu.com/builder/rc/settings/accountSet";
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/HaoKan.png", this.getClass());
    }

    @Override
    public String name() {
        return PlatformEnum.HaoKan.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.HaoKan.getCode();
    }

    @Override
    public void uploadFile(String videoPath, WebDriver driver) {
        // todo
    }

    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {

        WebElement input = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("//div[@class=\"ant-select-selection-overflow\"]")));
        String[] topics = videoContentTopic.split("#");
        for (String topic : topics) {
            if (StringUtils.isBlank(topic)) {
                continue;
            }
            input.click();
            BrowserHelper.paste(topic.trim(), driver);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {
        if (description.length() > 100) {
            description = description.substring(0, 98);
        }
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//input[@placeholder=\"请认真撰写视频简介，突出视频内容亮点和关键词，会增加更多曝光\"]")));
        input.sendKeys(description);
    }

    @Override
    public void setVideoTitle(String title, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//input[@placeholder=\"请输入作品标题，简要突出内容重点\"]")));
        input.sendKeys(title);
    }

    @Override
    public void setVideoCover(String imagePath, WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//p[@class=\"upload-text\"]")));

        button.click();

        try {
            TimeUnit.SECONDS.sleep(5);
            WebElement next = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                    .xpath("//p[@class=\"btn btn-next \"]")));
            next.click();
            TimeUnit.SECONDS.sleep(3);
            // 完成
            next = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                    .xpath("//p[@class=\"btn btn-submit\"]")));
            next.click();
            TimeUnit.SECONDS.sleep(10);

        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }


    }

    @Override
    public void submit(WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//button[@class=\"ant-btn ant-btn-primary ant-btn-lg btn-item\"]")));
        try {
            TimeUnit.SECONDS.sleep(2);
            button.click();
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
    }
}
