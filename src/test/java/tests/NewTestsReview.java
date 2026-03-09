package tests;

import base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ProductListingPage;
import pages.models.ProductDetails;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class NewTestsReview extends BaseTest {

    private ProductListingPage page;
    private static final Logger log = LoggerFactory.getLogger(NewTestsReview.class);

    // Known categories on the challenge page
    private static final List<String> KNOWN_CATEGORIES = List.of("Books", "Sports", "Home", "Clothing", "Electronics", "KATE_CATEGORY");

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

        List<ProductDetails> allCategoriesProducts = page.collectProductDetailsForAllCategories();
        Map<String, ProductDetails> highestRatedProducts = page.getHighestRatedProductPerCategory(KNOWN_CATEGORIES, allCategoriesProducts);
        assertHighestRatedProductsHaveValidRatings(highestRatedProducts);
        log.info("[PLP_003] All categories processed successfully.");
    }

    @Test(description = "PLP_003_NPE_Sabotage")
    public void findHighestRatedProductWithNullSabotage() {
        // 1. Get real products
        List<ProductDetails> products = page.collectProductDetailsForAllCategories();

        // 2. INJECT SABOTAGE: Add a product with a null rating
        products.add(ProductDetails.builder()
                .name("Sabotage Product")
                .category(KNOWN_CATEGORIES.get(0)) // Ensure it matches a searched category
                        .rating(-50.0)
                .price(BigDecimal.valueOf(100))
                .build());
        Assert.assertTrue(
                products.stream().allMatch(product -> product.getRating() > 0),
                "One or more products have an invalid rating (0 or less) in category: "
        );
        // 3. This call will now throw a NullPointerException inside the Stream
        Map<String, ProductDetails> highestRated =
                page.getHighestRatedProductPerCategory(KNOWN_CATEGORIES, products);

        assertHighestRatedProductsHaveValidRatings(highestRated);
    }
    @Step("Assert the product with the highest rating in each category")
    private void assertHighestRatedProductsHaveValidRatings(Map<String, ProductDetails> highestRatedProducts) {
        highestRatedProducts.forEach((category, product) -> {
            log.info("[PLP_003] Category: {} | Highest rated: {} ({} stars)", category, product.getName(), product.getRating());
            Assert.assertTrue(product.getRating() > 0, "No valid rating found for category: " + category);
        });
    }

}