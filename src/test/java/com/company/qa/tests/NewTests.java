package com.company.qa.tests;

import com.company.qa.base.BaseTest;
import com.company.qa.pages.ProductListingPage;
import com.company.qa.pages.models.ProductDetails;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


public class NewTests extends BaseTest {

    @Autowired
    private ProductListingPage page;

    // Known categories on the challenge page
    private static final List<String> KNOWN_CATEGORIES = List.of("Books", "Sports", "Home", "Clothing", "Electronics");
    private static final Logger log = LoggerFactory.getLogger(NewTests.class);

    @BeforeMethod(alwaysRun = true)
    public void openChallengePage() {
        page.openPage();
    }

    @Test(description = "PLP_004")
    @Story("Category Filter")
    @Description("For each category, collect all cards across all pages, parse prices, "
            + "find the most expensive product per category, and assert price > 0.")
    public void findMostExpensiveProductPerCategory() throws InterruptedException {
        List<ProductDetails> allCategoriesProducts = page.collectProductDetailsForAllCategories();
        for (String category : KNOWN_CATEGORIES) {
            ProductDetails productInfo = findMostExpensiveProductIn(category, allCategoriesProducts);
            log.info("[PLP_004] Category: {} | Most expensive: {} | Name: {}", productInfo.getCategory(), productInfo.getPrice(), productInfo.getName());
            Assert.assertTrue(
                    productInfo.getPrice().compareTo(BigDecimal.ZERO) > 0,
                    "No valid price found for category: " + category
            );
        }
        Thread.sleep(5000);
        log.info("[PLP_004] All categories processed successfully.");
    }

    public Optional<ProductDetails> findMostExpensiveProductFromList(List<ProductDetails> products) {
        return products.stream()
                .max(Comparator.comparing(ProductDetails::getPrice));
    }

    @Step("Find the most expensive product in category {category}")
    public ProductDetails findMostExpensiveProductIn(String category, List<ProductDetails> products) throws InterruptedException {

        Thread.sleep(5000);
        List<ProductDetails> filtered = products.stream().filter(product -> product.getCategory().contains(category)).toList();
        return findMostExpensiveProductFromList(filtered)
                .orElseThrow(() -> new IllegalStateException("No products found in category: " + category));
    }
}

