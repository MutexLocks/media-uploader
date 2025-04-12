package com.g.media.uploader.component.article;

import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.SleepUtils;
import lombok.extern.slf4j.Slf4j;
import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.BrowserHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.uploader.model.AccountInfo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DaYuArticleUploader extends AbstractArticleUploader {
    @Override
    public String url() {
        return "https://mp.dayu.com/dashboard/article/write";
    }

    @Override
    public String homeUrl() {
        return "https://mp.dayu.com/dashboard/index";
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/DA_YU_ARTICLE.png", getClass());
    }

    @Override
    public String name() {
        return PlatformEnum.DA_YU_ARTICLE.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.DA_YU_ARTICLE.getCode();
    }


    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//div[@class=\"name\"]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//div[@class=\"header-user\"]/img"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);
        
        return accountInfo;
    }

    @Override
    void uploadArticlePreAction(WebDriver driver) {
        WebDriverWait wait = getWait(driver);
        WebElement importFileButton = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//a[@class=\"import_doc_link\"]")));
        importFileButton.click();
        SleepUtils.sleepSecond(3);
        importFileButton = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//button[text()=\"选择文档\"]")));
        importFileButton.click();
        SleepUtils.sleepSecond(2);
    }


    @Override
    public void uploadFile(String filePath, WebDriver driver) {
        uploadArticlePreAction(driver);

        WebElement fileInput = getWait(driver).until(ExpectedConditions
                .presenceOfElementLocated(
                        By.xpath("//input[@type='file']")));
        fileInput.sendKeys(filePath);
        SleepUtils.sleepSecond(30);
    }

    @Override
    public void setArticleCover(String imagePath, WebDriver driver) {
        try {
            SleepUtils.sleepSecond(5);
            WebElement element = driver.findElement(By
                    .xpath("/html/body"));
            element.click();
            BrowserHelper.scrollToClick(
                    getWait(driver).until(ExpectedConditions
                            .elementToBeClickable(By
                                    .xpath("//span[text()='单封面']"))), driver);
        } catch (Exception ex) {
            log.error("点击正文报错");
        }
        int count = 0;
        while (count < 300) {
            try {
                driver.findElement(By
                        .xpath("//div[@class=\"article-write-article-cover-cover-item\"]/img"));
                log.info("封面自动生成完成");
                break;
            } catch (Exception ex) {
                count = count + 1;
                SleepUtils.sleepSecond(1);
                log.info("等待封面自动生成中...");
            }
        }
    }

    @Override
    public void setTitle(String title, WebDriver driver) {
        try {
            driver.navigate().refresh();
            SleepUtils.sleepSecond(2);
            BrowserHelper.enter(driver);
            WebElement delete = getWait(driver).until(ExpectedConditions
                    .elementToBeClickable(By
                            .xpath("//a[text()=\"撤销\"]")));
            delete.click();
            SleepUtils.sleepSecond(1);
        } catch (Exception ex) {
            //ignore
        }

        WebElement webElement = driver.findElement(By.xpath("//input[@id=\"title\"]"));
        webElement.click();
        webElement.sendKeys(title);

        SleepUtils.sleepSecond(10);
    }

    @Override
    public void submit(WebDriver driver) {
        int count = 0;
        while (count < 20) {
            try {
                driver.findElements(By.xpath("//label[@class=\"ant-radio-wrapper\"]")).get(0).click();
                break;
            } catch (Exception ex) {
                BrowserHelper.scrollDown(driver);
                count = count + 1;
            }
        }
        WebDriverWait wait = getWait(driver);
        WebElement submit = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//button[text()=\"发表\"]")));
        submit.click();
        SleepUtils.sleepSecond(10);
        submit = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//button[text()=\"确认发表\"]")));
        submit.click();
        SleepUtils.sleepSecond(5);
    }


}
