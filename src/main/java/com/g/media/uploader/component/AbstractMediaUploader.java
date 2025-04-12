//package org.g.media.uploader.component;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.g.media.uploader.exception.UploaderException;
//import org.g.media.uploader.utils.BrowserHelper;
//import org.g.media.uploader.utils.FileUtils;
//import org.g.media.uploader.utils.SleepUtils;
//import com.g.uploader.VideoUploader;
//import org.openqa.selenium.Alert;
//import org.openqa.selenium.Cookie;
//import org.openqa.selenium.JavascriptExecutor;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//public abstract class AbstractMediaUploader {
//    public WebDriverWait wait;
//    public WebDriver driver;
//
//
//    @Override
//    public void initPage(String phone, WebDriver driver) {
//        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
//
//        /* Javascript that will create new Firefox window. */
//        String jsOpenNewWindow = "window.open('" + url() + "');";
//        jsExecutor.executeScript(jsOpenNewWindow);
//
//    }
//
//
//    public static String downloadImageToBase64(String imageUrl, Set<Cookie> cookies) {
//        try {
//
//            URL url = new URL(imageUrl);
//            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
//            httpConn.setRequestMethod("GET");
//            httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
//            httpConn.setRequestProperty("Accept", "image/webp,image/apng,image/vnd.wap.png,image/png,image/svg+xml,image/gif,image/jpeg,image/webp");
//            // 确保响应码为 200 表示成功
//            int responseCode = httpConn.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                InputStream inputStream = httpConn.getInputStream();
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//                byte[] buffer = new byte[4096];
//                int bytesRead = -1;
//
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, bytesRead);
//                }
//
//                byte[] imageBytes = outputStream.toByteArray();
//                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
//
//                inputStream.close();
//                outputStream.close();
//
//                return base64Image;
//            } else {
//                log.error("No image found at the given URL.");
//            }
//        } catch (Exception ex) {
//            log.error("failed to download image: {}", imageUrl, ex);
//        }
//        return  null;
//    }
//
//    public void initSinglePage(String url, WebDriver driver) {
//        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
//
//        /* Javascript that will create new Firefox window. */
//        String jsOpenNewWindow = "window.open('" + url + "');";
//        jsExecutor.executeScript(jsOpenNewWindow);
//
//    }
//
//    public Boolean needAuth() {
//        return false;
//    }
//
//    @Override
//    public void getWebPage(WebDriver driver, String phone) {
//        if (driver == null) {
//            throw new UploaderException(String.format("用户%s浏览器未初始化", phone));
//        }
//        initPage(phone, driver);
//        switchDriver(driver, phone);
//    }
//
//    private void waitPage() {
//        int count = 1;
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        while (!js.executeScript("return document.readyState").equals("complete")) {
//            // 等待页面加载完成
//            log.info("等待页面加载完成...");
//            SleepUtils.sleepSecond(10);  // 也可以使用更好的等待机制
//            if (count > 100) {
//                break;
//            }
//            log.info("已等待时间：{}秒", 10 * count);
//            count--;
//        }
//    }
//
//    public int waitSecond() {
//        return 600;
//    }
//    @Override
//    public void switchDriver(WebDriver driver, String phone) {
//        if (driver == null) {
//            throw new UploaderException(String.format("用户%s浏览器未初始化", phone));
//        }
//        Map<String, String> urlHandlerMap = new HashMap<>();
//        this.driver = driver;
//        wait = new WebDriverWait(driver, waitSecond());
//
//        Set<String> windowHandles = driver.getWindowHandles();
//        for (String handle : windowHandles) {
//            driver.switchTo().window(handle);
//            if (StringUtils.startsWith(url(), driver.getCurrentUrl())) {
//                urlHandlerMap.put(url(), handle);
//                break;
//            }
//            if (StringUtils.startsWith(driver.getCurrentUrl(), url())) {
//                urlHandlerMap.put(url(), handle);
//                break;
//            }
//        }
//
//        if (!StringUtils.startsWith(url(), driver.getCurrentUrl())) {
//            String windowsHandle = urlHandlerMap.get(url());
//            if (StringUtils.isBlank(windowsHandle)) {
//                log.warn("对应页面没有打开, 尝试重新打开页面 {}", url());
//                initSinglePage(url(), driver);
//                windowHandles = driver.getWindowHandles();
//                for (String handle : windowHandles) {
//                    driver.switchTo().window(handle);
//                    if (StringUtils.startsWith(url(), driver.getCurrentUrl())) {
//                        urlHandlerMap.put(url(), handle);
//                        break;
//                    }
//                }
//                return;
//            }
//            driver.switchTo().window(windowsHandle);
//            driver.get(url());
//        }
//    }
//
//
//    public void driverSleep(Integer sleepSecond) {
//        try {
//            while (sleepSecond > 0) {
//                ((JavascriptExecutor) driver).executeScript("return 1+1;");
//                TimeUnit.SECONDS.sleep(1);
//                sleepSecond--;
//                dealWithAlert();
//            }
//        } catch (Exception ex) {
//            log.warn("failed to sleep", ex);
//        }
//    }
//
//    private void dealWithAlert() {
//        try {
//            Alert alert = driver.switchTo().alert();
//            alert.accept();  // 点击“确定”按钮
//            log.info("出现弹窗，点击确定");
//        } catch (Exception ex) {
//            // ignore
//        }
//    }
//
//    public Long getUploadFileSleepTime(String filePath) {
//        long fileSizeInMB = FileUtils.getFileSizeInMB(filePath);
//        if (fileSizeInMB == 0) {
//            return 30L;
//        }
//        // 按1m/s
//        Long sleepTime = fileSizeInMB;
//        log.info("上传文件大小：{} MB", fileSizeInMB);
//        log.info("等待时间：{} 秒", sleepTime);
//        return sleepTime;
//    }
//
//    @Override
//    public void refresh(WebDriver webDriver) {
//        log.info("刷新页面...");
//        webDriver.navigate().refresh();
//        try {
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//            int count = 1;
//            while (!js.executeScript("return document.readyState").equals("complete")) {
//                // 等待页面加载完成
//                log.info("等待页面加载完成...");
//                TimeUnit.SECONDS.sleep(10);  // 也可以使用更好的等待机制
//                if (count > 100) {
//                    break;
//                }
//                log.info("已等待时间：{}秒", 10 * count);
//                count--;
//            }
//            BrowserHelper.enter(webDriver);
//            TimeUnit.SECONDS.sleep(8);
//        } catch (Exception ex) {
//            //ignore
//        }
//    }
//}
