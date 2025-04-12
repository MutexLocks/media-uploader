package com.g.media.uploader.component.article;

import com.g.uploader.ArticleUploader;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public abstract class AbstractArticleUploader implements ArticleUploader {

    void uploadArticlePreAction(WebDriver driver) {

    };

    @Override
    public void optimizeArticle(WebDriver webDriver) {

    }

    public WebDriverWait getWait(WebDriver driver) {
        return new WebDriverWait(driver, 20);
    }
}
