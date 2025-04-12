package com.g.media.uploader.component.video;

import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.DownloadHelper;
import lombok.extern.slf4j.Slf4j;
import com.g.media.uploader.utils.BrowserHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.uploader.model.AccountInfo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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

//    @Override
//    public void switchDriver(WebDriver driver, String phone) {
//        if (driver == null) {
//            throw new UploaderException(String.format("用户%s浏览器未初始化", phone));
//        }
//        Map<String, String> urlHandlerMap = new HashMap<>();
//        this.driver = driver;
//        wait = new WebDriverWait(driver,  20);
//
//        Set<String> windowHandles = driver.getWindowHandles();
//        for (String handle: windowHandles) {
//            driver.switchTo().window(handle);
//            if (StringUtils.startsWith(url(), driver.getCurrentUrl())) {
//                urlHandlerMap.put(url(), handle);
//                break;
//            }
//        }
//        if (!StringUtils.equalsIgnoreCase(url(), driver.getCurrentUrl())) {
//            String windowsHandle = urlHandlerMap.get(url());
//            if (StringUtils.isBlank(windowsHandle)) {
//                log.warn("对应页面没有打开, 尝试重新打开页面 {}", url());
//                initSinglePage(url(), driver);
//                return;
//            }
//            driver.switchTo().window(windowsHandle);
//            driver.get(url());
//        }
//    }


    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/WeiShi.png", this.getClass());
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
        // todo
    }

    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {
        WebElement input = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By.xpath("//textarea[@placeholder='这一刻的想法']")));
        input.sendKeys(videoContentTopic);
    }

    @Override
    public void submit(WebDriver driver) {
        WebElement button = getWait(driver).until(ExpectedConditions.elementToBeClickable(By
                .xpath("//button[@class='ant-btn ant-btn-primary']")));
        button.click();
        try {
            TimeUnit.SECONDS.sleep(10);
            driver.navigate().refresh();
            TimeUnit.SECONDS.sleep(5);
            BrowserHelper.enter(driver);
        } catch (InterruptedException ex) {
            log.error("", ex);
        }

    }
}
