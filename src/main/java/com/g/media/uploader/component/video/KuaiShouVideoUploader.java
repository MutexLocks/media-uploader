package com.g.media.uploader.component.video;

import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.uploader.model.AccountInfo;

import lombok.extern.slf4j.Slf4j;
import com.g.media.uploader.utils.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.g.media.uploader.utils.FileUtils.getUploadFileSleepTime;

@Component
@Slf4j
public class KuaiShouVideoUploader extends AbstractVideoUploader {

    @Override
    public String url() {
        return "https://cp.kuaishou.com/article/publish/video";
    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//div[@class=\"user-info-name\"]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//div[@class=\"user-info-avatar\"]/img"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);
        
        return accountInfo;
    }

    @Override
    public String homeUrl() {
        return "https://cp.kuaishou.com/profile";
    }

    @Override
    public String name() {
        return PlatformEnum.Kuaishou.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.Kuaishou.getCode();
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/Kuaishou.png", this.getClass());
    }

    @Override
    public void setVideoTitle(String title, WebDriver driver) {
        
    }

    @Override
    public void setVideoCover(String imagePath, WebDriver driver) {
        
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {
        
    }

//    @Override
//    public Boolean needAuth() {
//        try {
//            wait.until(ExpectedConditions.elementToBeClickable(By
//                    .xpath(uploadVideoXpath())));
//        } catch (Exception ex) {
//            log.info("can not find upload button, need auth");
//            return true;
//        }
//        return false;
//    }


    @Override
    public void uploadFile(String videoPath, WebDriver driver) {
        getWait(driver).until(ExpectedConditions
                        .presenceOfElementLocated(By.xpath("//input[@type=\"file\"]")))
                .sendKeys(videoPath);
        try {
            TimeUnit.SECONDS.sleep(getUploadFileSleepTime(videoPath));
        } catch (Exception ex) {
            //
        }

    }

    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("//div[@placeholder=\"添加合适的话题和描述，作品能获得更多推荐～\"]")));
        input.sendKeys(videoContentTopic);
    }

    @Override
    public void submit(WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//button/span[text()=\"发布\"]")));
        button.click();
    }
}
