package com.company.qa.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Screenshots;
import com.codeborne.selenide.Selenide;

import static com.codeborne.selenide.Selenide.open;
import io.qameta.allure.Allure;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Base class for all test classes.
 * Handles WebDriver lifecycle: setup before each test and teardown after.
 */

@SpringBootTest(classes = BaseTest.class)
@ComponentScan("com.company.qa")

public class BaseTest extends AbstractTestNGSpringContextTests {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        Configuration.browser = "chrome";
        Configuration.headless = false;
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;

        open("https://www.cnarios.com/challenges/product-listing-pagination#challenge");
        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            try {
                File screenshot = Screenshots.takeScreenShotAsFile();
                if (screenshot != null) {
                    Allure.addAttachment("Failure Screenshot", "image/png",
                            new ByteArrayInputStream(Files.readAllBytes(screenshot.toPath())), "png");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
