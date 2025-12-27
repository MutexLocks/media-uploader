package com.g.media.uploader.component.article;

import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.SleepUtils;
import lombok.extern.slf4j.Slf4j;
import com.g.media.uploader.utils.BrowserHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.uploader.model.AccountInfo;

import com.g.media.uploader.model.PlatformEnum;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ZhiHuArticleUploader extends AbstractArticleUploader {
    @Override
    public String url() {
        return "https://zhuanlan.zhihu.com/write";
    }

    @Override
    public String homeUrl() {
        return "https://www.zhihu.com/creator";
    }

    @Override
    public String name() {
        return PlatformEnum.ZHI_HU_ARTICLE.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.ZHI_HU_ARTICLE.getCode();
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/ZHI_HU.png", this.getClass());
    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//div[contains(@class, 'LevelInfoV2-creatorInfo')]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//img[contains(@class, 'Avatar AppHeader-profileAvatar')]"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);
        
        return accountInfo;
    }

    @Override
    public void uploadFile(String filePath, WebDriver driver) {
        uploadArticlePreAction(driver);
        List<WebElement> elementList = getWait(driver).until(ExpectedConditions
                .presenceOfAllElementsLocatedBy(
                        By.xpath("//input[@type='file']")));
        elementList.get(elementList.size() - 1).sendKeys(filePath);
        SleepUtils.sleepSecond(30);
    }

    @Override
    void uploadArticlePreAction(WebDriver driver) {
        WebDriverWait wait = getWait(driver);
        WebElement importFileButton = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//span[text()=\"导入\"]")));
        importFileButton.click();
        SleepUtils.sleepSecond(1);
        importFileButton = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//button[@aria-label='导入文档']")));
        importFileButton.click();
        SleepUtils.sleepSecond(10);
    }


    @Override
    public void setArticleCover(String imagePath, WebDriver driver) {
        WebElement cover = driver.findElement(By
                .xpath("//input[@class=\"UploadPicture-input\"]"));
        cover.sendKeys(imagePath);
        SleepUtils.sleepSecond(10);
    }

    @Override
    public void setTitle(String title, WebDriver driver) {
        WebElement webElement = driver.findElement(By.xpath("//textarea[@placeholder=\"请输入标题（最多 100 个字）\"]"));
        webElement.clear();
        webElement.sendKeys(title);
        SleepUtils.sleepSecond(1);
    }

    @Override
    public void submit(WebDriver driver) {
        int count = 0;
        while (count < 10) {
            BrowserHelper.scrollDownV1(driver);
            count = count + 1;
            SleepUtils.sleepSecond(2);
        }
        SleepUtils.sleepSecond(10);

//        optimizeArticle();
        WebElement submit = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//button[text()=\"发布\"]")));
        submit.click();
        SleepUtils.sleepSecond(5);
    }
}
