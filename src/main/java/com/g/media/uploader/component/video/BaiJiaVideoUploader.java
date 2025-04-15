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
public class BaiJiaVideoUploader extends AbstractVideoUploader {

    @Override
    public String url() {
        return "https://baijiahao.baidu.com/builder/rc/edit?type=videoV2&is_from_cms=1";
    }


    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        if (!driver.getCurrentUrl().contains("accountSet")) {
            driver.get(homeUrl());
            SleepUtils.sleepSecond(5);
        }
        WebElement nameElement = driver.findElement(By.xpath("//*[@id=\"personCenterBaseInfo\"]/div[2]/div[2]/div[1]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By
                .xpath("//*[@id=\"personCenterBaseInfo\"]/div[2]/div[1]/div/img[1]"));
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
        return FileUtils.image2Base64("logo/BAI_JIA_VIDEO.png", this.getClass());
    }

    @Override
    public String name() {
        return PlatformEnum.BAI_JIA_VIDEO.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.BAI_JIA_VIDEO.getCode();
    }

    @Override
    public void uploadFile(String videoPath, WebDriver driver) {
        getWait(driver).until(ExpectedConditions
                        .presenceOfElementLocated(By.xpath("//input[@type=\"file\"]")))
                .sendKeys(videoPath);
        Long count = FileUtils.getUploadFileSleepTime(videoPath);
        SleepUtils.sleepSecond(count.intValue());
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {

    }

    @Override
    public void setVideoTitle(String title, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//input[@placeholder=\"添加标题获得更多推荐\"]")));
        input.sendKeys(title);
    }

    @Override
    public void setVideoCover(String imagePath, WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//div[contains(text(), '选择封面')]")));

        button.click();

        try {
            getWait(driver).until(ExpectedConditions
                            .presenceOfElementLocated(By.xpath("//input[@type=\"file\"]")))
                    .sendKeys(imagePath);
            TimeUnit.SECONDS.sleep(5);
            getWait(driver).until(ExpectedConditions
                            .presenceOfElementLocated(By.xpath("//span[contains(text(), '确定')]")))
                    .click();
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
    }

    @Override
    public void submit(WebDriver driver) {
        getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//div[contains(text(), '发布')]"))).click();
        checkClickResult("//button[text()=\"发布\"]", driver);
    }
}
