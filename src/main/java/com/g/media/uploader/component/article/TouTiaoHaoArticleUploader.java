package com.g.media.uploader.component.article;

import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.BrowserHelper;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.media.uploader.utils.SleepUtils;
import com.g.uploader.model.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class TouTiaoHaoArticleUploader extends AbstractArticleUploader {
    @Override
    public String url() {
        return "https://mp.toutiao.com/profile_v4/graphic/publish";
    }

    @Override
    public String homeUrl() {
        return "https://mp.toutiao.com/profile_v4/index";
    }

    @Override
    public String name() {
        return PlatformEnum.TOU_TIAO_ARTICLE.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.TOU_TIAO_ARTICLE.getCode();
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/TOU_TIAO_ARTICLE.png", this.getClass());
    }

    @Override
    public void setArticleCover(String s, WebDriver webDriver) {

    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//div[@class=\"auth-avator-name\"]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//img[@class=\"auth-avator-img\"]"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);
        
        return accountInfo;
    }

    @Override
    void uploadArticlePreAction(WebDriver driver) {
        WebElement importFileButton = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//div[@class=\"syl-toolbar-tool doc-import static\"]")));
        SleepUtils.sleepSecond(1);
        importFileButton.click();
        SleepUtils.sleepSecond(1);
        importFileButton.click();
        SleepUtils.sleepSecond(3);
    }

    @Override
    public void uploadFile(String videoPath, WebDriver driver) {
        uploadArticlePreAction(driver);
        driver.findElement(By.xpath("//input[@type=\"file\"]"))
                .sendKeys(videoPath);
        SleepUtils.sleepSecond(10);
    }

    @Override
    public void setTitle(String title, WebDriver driver) {
        SleepUtils.sleepSecond(1);
        driver.findElement(By.xpath("//textarea[@autocomplete=\"off\"]"))
                .sendKeys(title);
        SleepUtils.sleepSecond(1);
    }

    @Override
    public void submit(WebDriver driver) {
        optimizeArticle(driver);
        WebDriverWait wait = getWait(driver);
        try {
            WebElement submit = wait.until(ExpectedConditions
                    .elementToBeClickable(By
                            .xpath("//span[text()=\"预览并发布\"]")));
            submit.click();
            SleepUtils.sleepSecond(3);
        } catch (Exception ex) {
            //
        }
        ;

        int count = 0;
        while (count < 3) {
            try {
                WebElement submit = wait.until(ExpectedConditions
                        .elementToBeClickable(By
                                .xpath("//span[text()=\"确认发布\"]")));
                submit.click();
                break;
            } catch (Exception ex) {
                SleepUtils.sleepSecond(1);
                count = count + 1;
                BrowserHelper.scrollDown(driver);
            }
        }

        SleepUtils.sleepSecond(10);
    }

    @Override
    public void optimizeArticle(WebDriver driver) {
        WebDriverWait wait = getWait(driver);
        // 打开助手
        try {
            WebElement optimize = wait.until(ExpectedConditions
                    .elementToBeClickable(By
                            .xpath("//span[@class=\"text-hint\"]")));
            optimize.click();
            SleepUtils.sleepSecond(3);
        } catch (Exception ex) {
            log.warn("无法打开助手");
        }

        // 一键修改
        try {
            WebElement optimize = wait.until(ExpectedConditions
                    .elementToBeClickable(By
                            .xpath("//span[@class=\"btn btn-modify-all text-gray-mid\"]")));
            optimize.click();
            SleepUtils.sleepSecond(2);
        } catch (Exception ex) {
            log.warn("没有一键修改");
        }

        // 修改单处
        try {
            WebElement optimize = wait.until(ExpectedConditions
                    .elementToBeClickable(By
                            .xpath("//a[@class=\"btn text-small\"]")));
            optimize.click();
            SleepUtils.sleepSecond(2);
        } catch (Exception ex) {
            log.warn("没有修改");
        }
        try {
            WebElement optimize = wait.until(ExpectedConditions
                    .elementToBeClickable(By
                            .xpath("//span[text()=\"单标题\"]")));
            BrowserHelper.scrollToClick(optimize, driver);
            SleepUtils.sleepSecond(2);
        } catch (Exception ex) {
            log.warn("点击单标题失败");
        }

    }


}
