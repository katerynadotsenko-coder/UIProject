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

public class NewTestsReview extends BaseTest {

    private ProductListingPage page;
    private static final Logger log = LoggerFactory.getLogger(NewTestsReview.class);

    // Known categories on the challenge page
    private static final List<String> KNOWN_CATEGORIES = List.of("Books", "Sports", "Home", "Clothing", "Electronics");

    @BeforeMethod(alwaysRun = true)
    public void openChallengePage() {
        page = new ProductListingPage(driver);
        page.openPage();
    }

    @Test(description = "PLP_003")
    @Story("Category Filter")
    @Description("For each category, collect all cards across all pages, parse ratings, "
            + "find the highest-rated product per category, and assert rating > 0.")
    public void findHighestRatedProductPerCategory() {
        Map<String, ProductDetails> highestRatedProducts = page.getHighestRatedProductPerCategory(KNOWN_CATEGORIES);
        highestRatedProducts.forEach((category, product) -> {
            log.info("[PLP_003] Category: {} | Highest rated: {} ({} stars)", category, product.getName(), product.getRating());
            Assert.assertTrue(product.getRating() > 0, "No valid rating found for category: " + category);
        });
        log.info("[PLP_003] All categories processed successfully.");
    }
}
