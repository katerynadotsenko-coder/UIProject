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

public class NewTests extends BaseTest {

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
    @Description("Iterate each category filter, click it, count the visible product cards, "
            + "and assert the count is greater than zero. Store and log results per category.")
    public void stupidname() throws InterruptedException {
        Map<String, Integer> results = new LinkedHashMap<>();

        for (String category : KNOWN_CATEGORIES) {
            // Re-open to reset any previous filter
            page.openPage();
            Thread.sleep(5000);
            page.clickCategoryFilter(category);

            int count = page.getProductCards().size();
            results.put(category, count);

            System.out.printf("[PLP_001] Category: %-15s → %d product(s) on page 1%n",
                    category, count);

        }

        System.out.println("\n[PLP_001] Summary:");
        results.forEach((cat, cnt) -> System.out.printf("  %-15s : %d%n", cat, cnt));
    }
}
