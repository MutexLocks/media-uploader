package com.g.media.uploader.component.video;


import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.media.uploader.utils.SleepUtils;
import lombok.extern.slf4j.Slf4j;
import com.g.uploader.model.AccountInfo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class XiaoHongShuVideoUploader extends AbstractVideoUploader {

    @Override
    public String url() {
        return "https://creator.xiaohongshu.com/publish/publish?source=official";
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/XiaoHongShu.png", this.getClass());
    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//span[@class=\"name-box\"]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//img[@class=\"user_avatar\"]"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);
        
        return accountInfo;
    }

    @Override
    public String name() {
        return PlatformEnum.XiaoHongShu.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.XiaoHongShu.getCode();
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
    public void setVideoCover(String imagePath, WebDriver driver) {
        WebElement cover = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("//div[contains(text(), '设置封面')]")));
        cover.click();

        cover = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("/html/body/div[12]/div/div[2]/div/div[1]/div/div/div[2]/div/div[1]/h6")));
        cover.click();

        getWait(driver).until(ExpectedConditions
                        .presenceOfElementLocated(By.xpath("/html/body/div[12]/div/div[2]/div/div[2]/div[2]/input")))
                .sendKeys(imagePath);
        SleepUtils.sleepSecond(2);
        WebElement submitButton = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("/html/body/div[12]/div/div[3]/div/button[2]/div/span")));
        submitButton.click();
        SleepUtils.sleepSecond(10);
    }

    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {
//
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//div[contains(@data-placeholder, '描述')]")));
        input.sendKeys(description);
    }

    @Override
    public void setVideoTitle(String title, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//input[contains(@placeholder, '标题')]")));
        input.sendKeys(title);
    }

    @Override
    public void submit(WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//span[contains(text(), '发布')]")));
        button.click();
        checkClickResult("//span[contains(text(), '发布')]", driver);
    }

//    private Boolean alert() {
//        try {
//            driver.switchTo().alert();  // 尝试切换到弹窗
//            return true;  // 成功切换到弹窗，表示弹窗存在
//        } catch (NoAlertPresentException e) {
//            return false; // 捕获异常，表示没有弹窗
//        }
//    }

}
