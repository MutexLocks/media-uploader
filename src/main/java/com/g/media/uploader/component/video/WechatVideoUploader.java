package com.g.media.uploader.component.video;

import com.g.media.uploader.exception.UploaderException;
import com.g.media.uploader.model.PlatformEnum;
import com.g.media.uploader.utils.DownloadHelper;
import com.g.media.uploader.utils.FileUtils;
import com.g.media.uploader.utils.SleepUtils;
import com.g.uploader.model.AccountInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Component;

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
        Object shadowRoot = getShadowRoot(driver, "wujie-app");
        WebElement input = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].querySelector(\"input[placeholder*='概括']\")",
                shadowRoot
        );
        title = title
                .replaceAll("，", " ")
                .replaceAll(",", " ");
        //视频号标题至少要6个字
        title = addBlank(title);
        input.sendKeys(title);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
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
            Object shadowRoot = getShadowRoot(driver, "wujie-app");
            WebElement inputEditor = getWait(driver).until(driver1 ->
                    (WebElement) ((JavascriptExecutor) driver1).executeScript(
                            "return arguments[0].querySelector('div.input-editor')",
                            shadowRoot
                    )
            );
            inputEditor.sendKeys(description);
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ex) {
            log.error("设置视频描述失败");
            throw new UploaderException(ex);
        }
    }

    @Override
    public void uploadFile(String videoPath, WebDriver driver) {
//        // 1. 找到 tag
//        WebElement shadowHost = getWait(driver).until(
//                ExpectedConditions.presenceOfElementLocated(
//                        By.cssSelector("wujie-app.wujie_iframe[data-wujie-id='content']")
//                )
//        );
//
//        // 通过JavaScript执行器穿透Shadow DOM
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//


        // 2. 定位Shadow Host元素（wujie-app）
        WebElement shadowHost = getWait(driver).until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("wujie-app.wujie_iframe[data-wujie-id='content']")
        ));

        // 3. 移除覆盖层（如果存在）
        removeCoverLayer(driver, shadowHost);

        // 4. 执行文件上传
        uploadVideoFile(driver, shadowHost, videoPath);

        // 3. 发送文件路径
//        inputFile.sendKeys(videoPath);

        // 4. 等待上传完成（自定义逻辑）
        int waitTime = FileUtils.getUploadFileSleepTime(videoPath).intValue();
        SleepUtils.sleepSecond(waitTime);
        log.info("等待时间：{}s", waitTime);
    }



    @Override
    public void setVideoContentTopic(String videoContentTopic, WebDriver driver) {
        Object shadowRoot = getShadowRoot(driver, "wujie-app");

        WebElement input = getWait(driver).until(driver1 -> {
            WebElement el = (WebElement) ((JavascriptExecutor) driver1)
                    .executeScript("return arguments[0].querySelector('.input-editor')", shadowRoot);
            return (el != null && el.isDisplayed() && el.isEnabled()) ? el : null;
        });
        input.sendKeys(videoContentTopic);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
    }

    @Override
    public void submit(WebDriver driver) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ex) {
            throw new UploaderException(ex);
        }
        Object shadowRoot = getShadowRoot(driver, "wujie-app");

        WebElement button = getWait(driver).until(driver1 -> {
            WebElement el = (WebElement) ((JavascriptExecutor) driver1)
                    .executeScript("return arguments[0].querySelector(\"button:textContent('发表')\")", shadowRoot);
            return (el != null && el.isDisplayed() && el.isEnabled()) ? el : null;
        });
        button.click();
        checkClickResult(() -> getWait(driver).until(driver1 -> {
            WebElement el = (WebElement) ((JavascriptExecutor) driver1)
                    .executeScript("return arguments[0].querySelector(\"button:textContent('发表')\")", shadowRoot);
            return (el != null && el.isDisplayed() && el.isEnabled()) ? el : null;
        }), driver);
    }

    private Object execJs(Integer timeOutInSecond,WebDriver driver, String script, Object arg) {
        if (timeOutInSecond <= 0) {
            throw new UploaderException("time out can not be zero");
        }
        int loopCount = timeOutInSecond;
        try {
            while (loopCount > 0) {
                JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                Object ret = jsExecutor.executeScript(script, arg);
                if (ret != null) {
                    if (ret instanceof List && ((List) ret).isEmpty()) {
                        TimeUnit.SECONDS.sleep(1);
                        loopCount--;
                        continue;
                    }
                    return ret;
                }
                TimeUnit.SECONDS.sleep(1);
                loopCount--;
            }
        } catch (Exception ex) {
            log.error("<UNK>", ex);
        }
        return null;
    }

    private Object getShadowRoot(WebDriver driver, String tagName) {
        Object shadowRoot = null;
        if (StringUtils.isBlank(tagName)) {
            throw new UploaderException("tag name can not be null");
        }
        WebElement tag = getWait(driver).until(
                ExpectedConditions.presenceOfElementLocated(
                        By.tagName(tagName)));
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        return jsExecutor.executeScript("return arguments[0].shadowRoot", tag);
    }

    private static void removeCoverLayer(WebDriver driver, WebElement shadowHost) {
        ((JavascriptExecutor) driver).executeScript(
                "try {" +
                        "    const shadowRoot = arguments[0].shadowRoot;" +
                        "    if (shadowRoot) {" +
                        "        const cover = shadowRoot.querySelector('div[style*=\"position: fixed\"][style*=\"z-index: 2147483647\"]');" +
                        "        if (cover) {" +
                        "            cover.remove();" +
                        "            console.log('覆盖层已移除');" +
                        "        }" +
                        "    }" +
                        "} catch (e) {" +
                        "    console.warn('移除覆盖层失败', e);" +
                        "}",
                shadowHost
        );
    }

    private static void uploadVideoFile(WebDriver driver, WebElement shadowHost, String filePath) {
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

        // 执行上传
        Object result = ((JavascriptExecutor) driver).executeScript(uploadScript, shadowHost, filePath);
        System.out.println("上传结果: " + result);
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


