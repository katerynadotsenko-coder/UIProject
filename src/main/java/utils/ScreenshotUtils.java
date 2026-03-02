package utils;

import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for capturing screenshots on test failure.
 * Screenshots are saved to target/screenshots/ and attached to the Allure report.
 */
public class ScreenshotUtils {

    private static final String SCREENSHOT_DIR = "target/screenshots/";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotUtils() {
        // Utility class — no instantiation
    }

    /**
     * Captures a screenshot, saves it as a PNG file, and attaches it to Allure.
     *
     * @param driver   the active WebDriver instance
     * @param testName the name of the failing test (used in the filename)
     */
    public static void capture(WebDriver driver, String testName) {
        byte[] screenshotBytes = captureAndAttach(driver);
        if (screenshotBytes == null || screenshotBytes.length == 0) {
            return;
        }
        try {
            Path dir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            String fileName = testName + "_" + timestamp + ".png";
            Path filePath = dir.resolve(fileName);

            Files.write(filePath, screenshotBytes);
            System.out.printf("[ScreenshotUtils] Screenshot saved: %s%n", filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("[ScreenshotUtils] Failed to save screenshot: " + e.getMessage());
        }
    }

    /**
     * Allure attachment — invoked internally so the PNG byte array
     * is embedded directly in the Allure report.
     */
    @Attachment(value = "Failure Screenshot", type = "image/png")
    private static byte[] captureAndAttach(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            System.err.println("[ScreenshotUtils] Could not capture screenshot: " + e.getMessage());
            return new byte[0];
        }
    }
}
