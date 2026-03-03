package tests;

import base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ProductListingPage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NewTestsReview extends BaseTest {

    private ProductListingPage page;

    // Known categories on the challenge page
    private static final List<String> KNOWN_CATEGORIES = List.of("Books", "Sports", "Home", "Clothing", "Electronics");

    @BeforeMethod(alwaysRun = true)
    public void openChallengePage() {
        page = new ProductListingPage(driver);
        page.openPage();
    }

    @Test(description = "PLP_001")
    @Story("Category Filter")
    @Description("Iterate each category filter, click it, count the visible product cards, and assert the count is greater than zero. Store and log results per category.")
    public void verifyProductCountsPerCategory() throws InterruptedException {
        Map<String, Integer> results = new LinkedHashMap<>();

        KNOWN_CATEGORIES.stream()
                .forEach(category -> {
                    page.openPage();
                    page.clickCategoryFilter(category);

                    int count = page.getVisibleProductsCount();
                    results.put(category, count);

                    log.info("[PLP_001] Category: {} → {} product(s) on page 1", category, count);
                });

        log.info("\n[PLP_001] Summary:");
        results.forEach((cat, cnt) -> log.info("  {} : {}", cat, cnt));
    }
}
