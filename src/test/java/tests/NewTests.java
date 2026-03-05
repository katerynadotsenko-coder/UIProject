package tests;

import base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ProductListingPage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NewTests extends BaseTest {

    private ProductListingPage page;

    // Known categories on the challenge page
    private static final List<String> KNOWN_CATEGORIES = List.of("Books", "Sports", "Home", "Clothing", "Electronics");

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
            page.openPage();
            page.clickCategoryFilter(category);

            double maxPrice = -1;
            String maxProductName = "";

            int totalPages = page.getTotalPages();
            for (int p = 1; p <= totalPages; p++) {
                if (p > 1)
                    page.clickPageNumber(p);

                for (WebElement card : page.getProductCards()) {
                    double price = page.getProductPriceAsDouble(card);
                    if (price > maxPrice) {
                        maxPrice = price;
                        maxProductName = page.getProductName(card);
                    }
                }
            }

            System.out.printf("[PLP_004] Category: %-15s | Most expensive: %-40s ($%.2f)%n",
                    category, maxProductName, maxPrice);

            Assert.assertTrue(maxPrice > 0,
                    "No valid price found for category: " + category);
        }

        System.out.println("[PLP_004] All categories processed successfully.");
    }

}
