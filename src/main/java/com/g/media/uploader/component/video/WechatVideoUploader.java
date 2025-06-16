package com.g.media.uploader.component.video;

import com.g.media.uploader.exception.UploaderException;
import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.media.uploader.utils.SleepUtils;
import com.g.uploader.model.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WechatVideoUploader extends AbstractVideoUploader {

    @Override
    public String name() {
        return PlatformEnum.Wechat.getName();
    }

    @Override
    public String code() {
        return PlatformEnum.Wechat.getCode();
    }

    @Override
    public String logoBase64() {
        return FileUtils.image2Base64("logo/WeChat.png", this.getClass());
    }

    @Override
    public void setVideoCover(String imagePath, WebDriver driver) {
    }

    @Override
    public String url() {
        return "https://channels.weixin.qq.com/platform/post/create";
    }

    @Override
    public AccountInfo collectAccountInfo(WebDriver driver) {
        WebElement nameElement = driver.findElement(By.xpath("//h2[@class=\"finder-nickname\"]"));
        String userName = nameElement.getText();
        log.info("获取到用户名：" + userName);
        WebElement avatarElement = driver.findElement(By.xpath("//img[@class=\"avatar\"]"));
        String imgSrc = avatarElement.getAttribute("src");
        String imgBase64 = DownloadHelper.downloadImageToBase64(imgSrc);

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(userName);
        accountInfo.setAvatar(imgBase64);

        return accountInfo;
    }

    @Override
    public String homeUrl() {
        return "https://channels.weixin.qq.com/platform";
    }

    @Override
    public void setVideoTitle(String title, WebDriver driver) {
        title = title
                .replaceAll("，", " ")
                .replaceAll(",", " ");
        //视频号标题至少要6个字
        title = addBlank(title);
        if (title.length() > 16) {
            title = title.substring(0, 16);
        }
        List<WebElement> elements = getShadowRoot(driver)
                .findElements(By.cssSelector("input[type=\"text\"]"));
        for (WebElement element : elements) {
            if (StringUtils.contains(element.getAttribute("placeholder"), "概括")) {
                element.sendKeys(title);
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ex) {
                    throw new UploaderException(ex);
                }
                return;
            }
        }
        throw new UploaderException("设置标题失败，没有找到element");
    }

    private String addBlank(String title) {
        if (title.length() >= 6) {
            return title;
        }
        int blankCount = 6 - title.length();
        StringBuilder titleBuilder = new StringBuilder(title);
        for (int i = 0; i < blankCount; i++) {
            titleBuilder.append(" ");
        }
        return titleBuilder.toString();
    }

    @Override
    public void setVideoDescription(String description, WebDriver driver) {
        try {
            SearchContext shadowRoot = getShadowRoot(driver);
            shadowRoot.findElement(By.cssSelector("div.input-editor"))
                    .sendKeys(description);
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ex) {
            log.error("设置视频描述失败");
            throw new UploaderException(ex);
        }
    }

    @Override
    public void uploadFile(String videoPath, WebDriver driver) {


        WebElement inputFile = waitForElementInContext(driver,
                getShadowRoot(driver),
                By.cssSelector("input[type='file']"),
                20);
        inputFile.sendKeys(videoPath);

        // 4. 等待上传完成（自定义逻辑）
        int waitTime = FileUtils.getUploadFileSleepTime(videoPath).intValue();
        SleepUtils.sleepSecond(waitTime);
        log.info("等待时间：{}s", waitTime);
    }


    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {
        if (StringUtils.isBlank(videoContentTopic)) {
            return;
        }
        setVideoDescription(videoContentTopic, driver);
    }

    @Override
    public void submit(WebDriver driver) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
        List<WebElement> elements = getShadowRoot(driver)
                .findElements(By.cssSelector("button[type=\"button\"]"));
        for (WebElement element : elements) {
            if (StringUtils.contains( element.getText(), "发表")) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
        }


        checkClickResult(() -> {
            List<WebElement> thisElements = getShadowRoot(driver)
                    .findElements(By.cssSelector("button[type=\"button\"]"));
            for (WebElement element : thisElements) {
                if (StringUtils.contains( element.getText(), "发表")) {
                    return element;
                }
            }
            return null;
        });
    }


    private SearchContext getShadowRoot(WebDriver driver) {
        WebElement shadowHost = getWait(driver).until(ExpectedConditions
                .presenceOfElementLocated(By.cssSelector("wujie-app")));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String script =
                "Object.defineProperty(" +
                        "  window.document.querySelector('wujie-app').shadowRoot.firstElementChild," +
                        "  'parentNode'," +
                        "  {" +
                        "    enumerable: true," +
                        "    configurable: true," +
                        "    get: () => window.document" +
                        "  }" +
                        ");";

        js.executeScript(script);
        return shadowHost.getShadowRoot();
    }

    private static WebElement waitForElementInContext(WebDriver driver, SearchContext context, By by, long timeoutSecs) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSecs));
        return wait.until(new ExpectedCondition<WebElement>() {
            @Override
            public WebElement apply(WebDriver d) {
                try {
                    return context.findElement(by);
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    return null;
                }
            }

            @Override
            public String toString() {
                return String.format("element located by %s in shadow context", by);
            }
        });
    }

    public static void main(String[] args) {
        String uploadScript =
                "try {\n" +
                        "    const shadowRoot = arguments[0].shadowRoot;\n" +
                        "    if (!shadowRoot) return { success: false, reason: 'shadowRoot not found' };\n" +

                        "    // 按层级查找文件输入框\n" +
                        "    let fileInput = shadowRoot.querySelector('.post-edit-wrap .material .upload .post-upload-wrap .upload-wrap .ant-upload input[type=\"file\"]');\n" +

                        "    // 备选选择器\n" +
                        "    if (!fileInput) {\n" +
                        "        fileInput = shadowRoot.querySelector('.ant-upload-drag input[type=\"file\"]');\n" +
                        "    }\n" +

                        "    if (!fileInput) return { success: false, reason: 'file input not found' };\n" +

                        "    // 确保元素可见\n" +
                        "    fileInput.style.display = 'block';\n" +
                        "    fileInput.style.opacity = '1';\n" +
                        "    fileInput.style.position = 'relative';\n" +
                        "    fileInput.style.zIndex = '2147483646';\n" +

                        "    // 创建模拟文件\n" +
                        "    const dataTransfer = new DataTransfer();\n" +
                        "    dataTransfer.items.add(new File([''], arguments[1], { type: 'video/mp4' }));\n" +
                        "    fileInput.files = dataTransfer.files;\n" +

                        "    // 触发变更事件\n" +
                        "    const changeEvent = new Event('change', { bubbles: true, cancelable: true });\n" +
                        "    fileInput.dispatchEvent(changeEvent);\n" +

                        "    // 检查上传状态\n" +
                        "    const uploadStatus = shadowRoot.querySelector('.ant-upload-list-item');\n" +
                        "    return { \n" +
                        "        success: true, \n" +
                        "        filePath: arguments[1], \n" +
                        "        uploadElementVisible: window.getComputedStyle(fileInput).display !== 'none',\n" +
                        "        uploadListVisible: !!uploadStatus\n" +
                        "    };\n" +
                        "} catch (e) {\n" +
                        "    return { success: false, reason: e.message };\n" +
                        "}\n";
        System.out.println(uploadScript);
    }

}


