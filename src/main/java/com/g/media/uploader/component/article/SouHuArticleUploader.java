package com.g.media.uploader.component.article;

import com.g.media.uploader.exception.UploaderException;
import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.BrowserHelper;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.media.uploader.utils.SleepUtils;
import com.g.uploader.model.AccountInfo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class SouHuArticleUploader extends AbstractArticleUploader {


    @Override
    public String url() {
        return "https://mp.sohu.com/mpfe/v4/contentManagement/news/addarticle";
    }

    @Override
    public String homeUrl() {
        return "https://mp.sohu.com/mpfe/v4/contentManagement/first/page";
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/SOU_HU_ARTICLE.png", this.getClass());
    }


    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//span[@class=\"user-name\"]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//img[@class=\"user-pic\"]"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);
        
        return accountInfo;
    }

    @Override
    public String name() {
        return PlatformEnum.SOU_HU_ARTICLE.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.SOU_HU_ARTICLE.getCode();
    }



    @Override
    public void uploadFile(String filePath, WebDriver driver) {
        WebDriverWait wait = getWait(driver);
        WebElement importFileButton = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//div[@class=\"ql-editor ql-blank\"]")));
        importFileButton.click();
        SleepUtils.sleepSecond(1);
        WebElement textArea = wait.until(ExpectedConditions
                .presenceOfElementLocated(
                        By.xpath("//div[@class='ql-editor ql-blank']")));

        try (FileInputStream fis = new FileInputStream(filePath)) {
            XWPFDocument document = new XWPFDocument(fis);

            // 读取段落
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            List<XWPFPictureData> pictures = document.getAllPictures();
            for (int index = 0; index < paragraphs.size(); index++) {
                XWPFParagraph xwpfParagraph = paragraphs.get(index);
                String content = xwpfParagraph.getText().trim();
                if (StringUtils.isBlank(content)) {
                    continue;
                }
                log.info("复制段落");
                textArea.sendKeys(xwpfParagraph.getText());
                int picIndex = index;
                if (picIndex >= pictures.size()) {
                    continue;
                }
                if (pictures.size() > 6) {
                    if (index % 2 != 0) {
                        continue;
                    }
                    picIndex = picIndex / 2;
                }
                XWPFPictureData xwpfPictureData = pictures.get(picIndex);
                log.info("复制图片");
                addImage(xwpfPictureData, driver);

                SleepUtils.sleepSecond(3);

            }
        } catch (
                IOException e) {
            throw new UploaderException("复制word失败");
        }
    }

//    private void addText(String text) {
//        wait.until(ExpectedConditions
//                        .presenceOfElementLocated(
//                                By.xpath("//div[@class='ql-editor ql-blank']")))
//                .sendKeys(text);
//    }

    private void addImage(XWPFPictureData pictureData, WebDriver driver) {
        String imagePath = saveTempPic(pictureData);
        doAddImage(imagePath, driver);
    }

    private String saveTempPic(XWPFPictureData pictureData) {
        byte[] data = pictureData.getData();
        // 创建文件名
        String fileName = "image_" + System.currentTimeMillis() + ".png";
        String filePath = "/opt/g/media/images/" + fileName;
        File outputFile = new File(filePath);

        // 保存图片到文件
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(data);
            log.info("保存图片文件到：" + filePath);
        } catch (Exception ex) {
            throw new UploaderException("failed to save pic");
        }

        return filePath;
    }

    private void doAddImage(String imagePath, WebDriver driver) {
        WebElement imageButton = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//button[@class=\"ql-image\"]")));
        imageButton.click();

        getWait(driver).until(ExpectedConditions
                        .presenceOfElementLocated(By
                                .xpath("//input[@type=\"file\"]")))
                .sendKeys(imagePath);
        SleepUtils.sleepSecond(10);
        getWait(driver).until(ExpectedConditions
                        .elementToBeClickable(By
                                .xpath("//p[text()=\"确定\"]")))
                .click();
    }

    @Override
    public void setArticleCover(String imagePath, WebDriver driver) {
        WebDriverWait wait = getWait(driver);
        WebElement auto = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//div[@class=\"upload-file mp-upload\"]")));
        BrowserHelper.scrollToWebElement(driver, auto);
        SleepUtils.sleepSecond(1);
        auto.click();
        SleepUtils.sleepSecond(1);
        auto = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//img[@class=\"lazy-image\"]")));
        auto.click();
        SleepUtils.sleepSecond(1);
        auto = wait.until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//p[@class=\"button positive-button\"]")));
        BrowserHelper.scrollToWebElement(driver, auto);
        auto.click();
        SleepUtils.sleepSecond(2);
    }

    @Override
    public void setTitle(String title, WebDriver driver) {
        driver.navigate().refresh();
        SleepUtils.sleepSecond(10);
        try {
            WebElement delete = driver.findElement(By
                    .xpath("//p[text()=\"撤销\"]"));
            delete.click();
        } catch (Exception ex) {
            //ignore
        }
        SleepUtils.sleepSecond(2);
        driver.findElement(By.xpath("//input[contains(@placeholder, '输入标题')]"))
                .sendKeys(title);
        SleepUtils.sleepSecond(1);
    }

    @Override
    public void submit(WebDriver driver) {
        optimizeArticle(driver);
        WebElement submit = getWait(driver).until(ExpectedConditions
                .elementToBeClickable(By
                        .xpath("//li[@class=\"publish-report-btn active positive-button\"]")));
        submit.click();
        SleepUtils.sleepSecond(5);
    }

    @Override
    public void optimizeArticle(WebDriver driver) {
        // 打开助手
        try {
            SleepUtils.sleepSecond(10);
            WebElement optimize = getWait(driver).until(ExpectedConditions
                    .elementToBeClickable(By
                            .xpath("//button[text()=\"生成摘要\"]")));
            optimize.click();
            SleepUtils.sleepSecond(5);
        } catch (Exception ex) {
            log.warn("无法打开助手");
        }
    }
}

