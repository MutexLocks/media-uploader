package com.g.media.uploader.utils;

import cn.hutool.core.swing.clipboard.ImageSelection;
import lombok.extern.slf4j.Slf4j;
import com.g.media.uploader.exception.UploaderException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BrowserHelper {

    private static void clearClipboard() {
        StringSelection stringSelection = new StringSelection("");
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
    }

    public static void pasteImage(byte[] pictureBytes, WebDriver webDriver) {

        InputStream input = new ByteArrayInputStream(pictureBytes);
        try {
            BufferedImage image = ImageIO.read(input);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); //得到系统剪贴板
            Transferable selection = new ImageSelection(image);  //图像通道
            clipboard.setContents(selection, null);

            doPaste(webDriver);
        } catch (IOException ex) {
            throw new UploaderException("failed to copy image");
        }
    }


    public static void paste(String content, WebDriver webDriver) {
        if (content == null || "".equals(content)) {
            throw new UploaderException("content can not be null");
        }
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        String script = "navigator.clipboard.writeText(arguments[0]).then(() => { console.log('Text copied!'); }).catch(err => { console.error('Failed to copy text: ', err); });";

        // 执行 JavaScript，将文本写入剪贴板
        js.executeScript(script, content);
        doPaste(webDriver);
    }

    public static void scrollToWebElement(WebDriver driver, WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView();", element);
    }
    public static void scrollDownV1(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollBy(0, 100);");
        } catch (Exception ex) {
            log.error("向下滚动失败");
        }
    }
    public static void scrollDown(WebDriver webDriver) {
        try {
            log.info("滚动鼠标");
            next(webDriver);
        } catch (Exception ex) {
            log.error("", ex);
            throw new UploaderException("复制内容失败");
        }
    }

    public static void scrollToClick(WebElement webElement, WebDriver webDriver) {
        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("arguments[0].scrollIntoView();",webElement);
        SleepUtils.sleepSecond(2);
        webElement.click();
    }


    private static void doPaste(WebDriver webDriver) {
        try {
            Actions actions = new Actions(webDriver);
            // 模拟按下 Ctrl + V 组合键
            actions.keyDown(Keys.CONTROL) // 按下 Ctrl 键
                    .sendKeys("v") // 发送 'V' 键
                    .keyUp(Keys.CONTROL) // 抬起 Ctrl 键
                    .perform(); // 执行动作
            TimeUnit.SECONDS.sleep(1);
            enter(webDriver);
        } catch (InterruptedException ex) {
            log.error("", ex);
            throw new UploaderException("复制内容失败");
        }
    }

    public static void backspace(WebDriver webDriver) {
        try {
            Actions actions = new Actions(webDriver);
            log.info("删除");
            actions.keyDown(Keys.CONTROL) // 按下 Ctrl 键
                    .sendKeys("a") // 发送 'A' 键
                    .keyUp(Keys.CONTROL) // 抬起 Ctrl 键
                    .perform(); // 执行动作

            TimeUnit.SECONDS.sleep(1);

            // 模拟按下 Backspace 键
            actions.sendKeys(Keys.BACK_SPACE).perform();

        } catch (InterruptedException ex) {
            log.error("", ex);
            throw new UploaderException("删除内容失败");
        }
    }

    public static void enter(WebDriver webDriver) {
        try {
            Actions actions = new Actions(webDriver);
            actions.sendKeys(Keys.ENTER).perform();
            TimeUnit.MILLISECONDS.sleep(100);

        } catch (InterruptedException ex) {
            log.error("", ex);
            throw new UploaderException("failed to enter");
        }
    }

    public static void next(WebDriver webDriver) {
        try {
            // 创建 Actions 对象
            Actions actions = new Actions(webDriver);

// 模拟向下滚动（按下向下箭头）
            actions.sendKeys(Keys.ARROW_DOWN).perform();
            TimeUnit.SECONDS.sleep(1);
            actions.sendKeys(Keys.ARROW_DOWN).perform();
            TimeUnit.SECONDS.sleep(1);

        } catch (InterruptedException ex) {
            log.error("", ex);
            throw new UploaderException("向下按钮失败");
        }
    }

}
