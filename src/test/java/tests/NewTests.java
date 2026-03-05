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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.IntStream;

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
        List<ProductDetails> productsInCategory = page.collectProductDetailsForAllCategories();
        for (String category : KNOWN_CATEGORIES) {
            ProductDetails productInfo = findMostExpensiveProductIn(category, productsInCategory);
            log.info("[PLP_004] Category: {} | Most expensive: {} | Name: {}", productInfo.getCategory(), productInfo.getPrice(), productInfo.getName());
            Assert.assertTrue(
                    productInfo.getPrice().compareTo(BigDecimal.ZERO) > 0,
                    "No valid price found for category: " + category
            );
        }
        log.info("[PLP_004] All categories processed successfully.");
    }

    public Optional<ProductDetails> findMostExpensiveProductFromList(List<ProductDetails> products) {
        return products.stream()
                .max(Comparator.comparing(ProductDetails::getPrice));
    }


    public ProductDetails findMostExpensiveProductIn(String category, List<ProductDetails> products) {

        List<ProductDetails> filtered=products.stream().filter(product->product.getCategory().contains(category)).toList();
        return findMostExpensiveProductFromList(filtered)
                .orElseThrow(() -> new IllegalStateException("No products found in category: " + category));
    }
}

