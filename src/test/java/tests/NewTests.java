package tests;

import base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ProductListingPage;
import pages.models.ProductDetails;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NewTests extends BaseTest {

    private ProductListingPage page;

    // Known categories on the challenge page
    private static final List<String> KNOWN_CATEGORIES = List.of("Books", "Sports", "Home", "Clothing", "Electronics");
    private static final Logger log = LoggerFactory.getLogger(NewTests.class);

    @BeforeMethod(alwaysRun = true)
    public void openChallengePage() {
        page = new ProductListingPage(driver);
        page.openPage();
    }

    @Test(description = "PLP_004")
    @Story("Category Filter")
    @Description("For each category, collect all cards across all pages, parse prices, "
            + "find the most expensive product per category, and assert price > 0.")
    public void findMostExpensiveProductPerCategory() {
        for (String category : KNOWN_CATEGORIES) {

            ProductDetails productInfo = findMostExpensiveProductIn(category);
            log.info("[PLP_004] Category: {} | Most expensive: {} | Name: {}", productInfo.getCategory(), productInfo.getPrice(), productInfo.getName());
            Assert.assertTrue(productInfo.getPrice() > 0,
                    "No valid price found for category: " + category);
        }
        log.info("[PLP_004] All categories processed successfully.");
    }


    public ProductDetails findMostExpensiveProductIn(String category) {
        double maxPrice = 0;
        String maxProductName = "";

        int totalPages = page.getTotalPages();

        for (int p = 1; p <= totalPages; p++) {
            if (p >= 1) {
                page.clickPageNumber(p);
            }
            for (WebElement card : page.getProductCards()) {
                double price = page.getProductPriceAsDouble(card);
                String cardCategory = page.getProductCategory(card);

                if (price > maxPrice && cardCategory.contains(category)) {
                    maxPrice = price;
                    maxProductName = page.getProductName(card);
                }
            }
        }

        return new ProductDetails(maxProductName, 0, maxPrice, category);
    }
}

