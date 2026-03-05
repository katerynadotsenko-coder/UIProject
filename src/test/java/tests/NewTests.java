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

    @Test(description = "PLP_003")
    @Story("Category Filter")
    @Description("For each category, collect all cards across all pages, parse ratings, "
            + "find the highest-rated product per category, and assert rating > 0.")
    public void findHighestRatedProductPerCategory() {
        Map<String, double[]> results = new LinkedHashMap<>(); // category → [rating, page]

        for (String category : KNOWN_CATEGORIES) {
            page.openPage();
            page.clickCategoryFilter(category);

            double maxRating = -1;
            String maxProductName = "";

            int totalPages = page.getTotalPages();
            for (int p = 1; p <= totalPages; p++) {
                if (p > 1)
                    page.clickPageNumber(p);

                for (WebElement card : page.getProductCards()) {
                    double rating = page.getProductRating(card);
                    if (rating > maxRating) {
                        maxRating = rating;
                        maxProductName = page.getProductName(card);
                    }
                }
            }

            System.out.printf("[PLP_003] Category: %-15s | Highest rated: %-40s (%.1f stars)%n",
                    category, maxProductName, maxRating);

            Assert.assertTrue(maxRating > 0,
                    "No valid rating found for category: " + category);

            results.put(category, new double[] { maxRating });
        }

        System.out.println("[PLP_003] All categories processed successfully.");
    }
}
