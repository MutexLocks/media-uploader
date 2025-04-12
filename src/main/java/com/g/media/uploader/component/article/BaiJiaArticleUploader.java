package com.g.media.uploader.component.article;//package com.gfree.auto.upload.video.component.article;
//
//import com.gfree.auto.upload.video.utils.BrowserHelper;
//import com.gfree.auto.upload.video.utils.SleepUtils;
//import com.gfree.video.common.model.type.PlatformEnum;
//import lombok.extern.slf4j.Slf4j;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class BaiJiaArticleUploader extends AbstractArticleUploader {
//    @Override
//    public String url() {
//        return "https://baijiahao.baidu.com/builder/rc/edit?type=news";
//    }
//
//
//    @Override
//    public String name() {
//        return PlatformEnum.BAI_JIA_ARTICLE.getName();
//    }
//
//    @Override
//    public String code() {
//        return PlatformEnum.BAI_JIA_ARTICLE.getCode();
//    }
//
//    @Override
//    String uploadArticleXpath() {
//        return "//span[text()=\"选择文档\"]";
//    }
//
//    @Override
//    void uploadArticlePreAction() {
//        try {
//            WebElement body = wait.until(ExpectedConditions
//                    .elementToBeClickable(By.xpath("/html/body")));
//            body.click();
//            SleepUtils.sleepSecond(1);
//            BrowserHelper.backspace(driver);
//        } catch (Exception ex) {
//            //
//        }
//        WebElement importFileButton = wait.until(ExpectedConditions
//                .elementToBeClickable(By
//                        .xpath("//div[@class=\"edui-box edui-button edui-for-importdoc edui-default\"]")));
//        importFileButton.click();
//        SleepUtils.sleepSecond(5);
//    }
//
//
//    @Override
//    public void setArticleCover(String imagePath) {
//        int count = 0;
//        while (count < 20) {
//            try {
//                WebElement auto = wait.until(ExpectedConditions
//                        .elementToBeClickable(By
//                                .xpath("//span[text()=\"单图\"]")));
//                auto.click();
//                SleepUtils.sleepSecond(2);
//                break;
//            } catch (Exception ex) {
//                BrowserHelper.scrollDown(driver);
//                count = count + 1;
//            }
//
//            try {
//                driver.findElement(By.xpath("//span[@class=\"placehold\"]")).click();
//                SleepUtils.sleepSecond(1);
//                driver.findElement(By.xpath("//div[@class=\"image cheetah-ui-pro-base-image-aspect-fit\"]")).click();
//                SleepUtils.sleepSecond(2);
//                driver.findElement(By.xpath("//span[text()=\"确 认\"]")).click();
//                SleepUtils.sleepSecond(2);
//            } catch (Exception ex) {
//                log.warn("手动设置封面失败");
//            }
//        }
//
//    }
//
//    @Override
//    public void setTitle(String title) {
//        WebElement element = driver.findElement(By.xpath("//textarea[@placeholder=\"请输入标题（8 - 30字）\"]"));
//        element.clear();
//        SleepUtils.sleepSecond(1);
//        element.sendKeys(title);
//        SleepUtils.sleepSecond(1);
//    }
//
//    @Override
//    public void submit() {
//        optimizeArticle();
//        driver.findElements(By.xpath("//div[@class=\"op-btn-outter-content\"]")).get(1).click();
//        SleepUtils.sleepSecond(5);
//    }
//
//    @Override
//    public void optimizeArticle() {
//        // 打开助手
//        try {
//            SleepUtils.sleepSecond(10);
//            WebElement optimize = wait.until(ExpectedConditions
//                    .elementToBeClickable(By
//                            .xpath("//div[@class=\"assistent-title semibold\"]")));
//            optimize.click();
//            SleepUtils.sleepSecond(5);
//        } catch (Exception ex) {
//            log.warn("无法打开助手");
//        }
//
//        // 一键修改
//        try {
//            WebElement optimize = wait.until(ExpectedConditions
//                    .elementToBeClickable(By
//                            .xpath("//div[@class=\"adopt-all\"]")));
//            optimize.click();
//            SleepUtils.sleepSecond(2);
//        } catch (Exception ex) {
//            log.warn("没有一键修改");
//        }
//    }
//
//
//}
