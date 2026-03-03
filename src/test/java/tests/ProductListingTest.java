package tests;

import base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.ProductListingPage;

import java.util.*;
import java.util.regex.Pattern;


@Feature("Product Listing & Pagination")
public class ProductListingTest extends BaseTest {

    private ProductListingPage page;

    // Known categories on the challenge page
    private static final List<String> KNOWN_CATEGORIES = List.of("Books", "Sports", "Home", "Clothing", "Electronics");

    @BeforeMethod(alwaysRun = true)
    public void openChallengePage() {
        page = new ProductListingPage(driver);
        page.openPage();
    }

    /* ================================================================== */
    /* PLP_001 — Count products per category */
    /* ================================================================== */

    @Test(description = "PLP_001")
    @Story("Category Filter")
    @Description("Iterate each category filter, click it, count the visible product cards, "
            + "and assert the count is greater than zero. Store and log results per category.")
    public void findProductByTargetName() throws InterruptedException {
        Map<String, Integer> results = new LinkedHashMap<>();

        for (String category : KNOWN_CATEGORIES) {
            // Re-open to reset any previous filter
            page.openPage();
            page.clickCategoryFilter(category);
            int count = page.getProductCards().size();
            results.put(category, count);

            System.out.printf("[PLP_001] Category: %-15s → %d product(s) on page 1%n",
                    category, count);

        }

        System.out.println("\n[PLP_001] Summary:");
        results.forEach((cat, cnt) -> System.out.printf("  %-15s : %d%n", cat, cnt));
    }

    /* ================================================================== */
    /* PLP_002 — Find a specific product and report its page */
    /* ================================================================== */

    @Test(description = "PLP_002")
    @Story("Pagination Navigation")
    @Description("Search all pages for a hard-coded product name. Navigate page-by-page "
            + "until the product is found. Assert the product exists and log the page number.")
    public void findSpecificProductPage() {
        final String targetProduct = "The Pragmatic Programmer";

        int totalPages = page.getTotalPages();
        int foundOnPage = -1;

        outer: for (int p = 1; p <= totalPages; p++) {
            if (p > 1) {
                page.clickPageNumber(p);
            }

            List<WebElement> cards = page.getProductCards();
            for (WebElement card : cards) {
                String name = page.getProductName(card);
                if (name.equalsIgnoreCase(targetProduct)) {
                    foundOnPage = p;
                    System.out.printf("[PLP_002] Product \"%s\" found on page %d%n",
                            targetProduct, foundOnPage);
                    break outer;
                }
            }
        }

        Assert.assertNotEquals(foundOnPage, -1,
                "Product \"" + targetProduct + "\" was not found on any page.");
    }

    /* ================================================================== */
    /* PLP_003 — Highest-rated product per category */
    /* ================================================================== */

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

    /* ================================================================== */
    /* PLP_004 — Most expensive product per category */
    /* ================================================================== */

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

    /* ================================================================== */
    /* PLP_005 — Validate pagination controls behaviour */
    /* ================================================================== */

    @Test(description = "PLP_005")
    @Story("Pagination Controls")
    @Description("On page 1: assert Previous is disabled and Next is enabled. "
            + "Navigate to the last page: assert Next is disabled and Previous is enabled. "
            + "Assert clicking a page number correctly changes the active page indicator.")
    public void validatePaginationControls() {
        // --- Verify state on Page 1 ---
        Assert.assertEquals(page.getCurrentPageNumber(), 1,
                "Should start on page 1");
        Assert.assertFalse(page.isPreviousEnabled(),
                "Previous button should be DISABLED on page 1");
        Assert.assertTrue(page.isNextEnabled(),
                "Next button should be ENABLED on page 1");

        // --- Navigate mid-way via a page-number button ---
        int totalPages = page.getTotalPages();
        System.out.printf("[PLP_005] Total pages detected: %d%n", totalPages);
        Assert.assertTrue(totalPages > 1,
                "There should be more than 1 page");

        page.clickPageNumber(2);
        Assert.assertEquals(page.getCurrentPageNumber(), 2,
                "Active page should be 2 after clicking page 2");

        // --- Navigate to last page ---
        page.clickPageNumber(totalPages);
        Assert.assertEquals(page.getCurrentPageNumber(), totalPages,
                "Active page should be " + totalPages + " after clicking last page button");
        Assert.assertFalse(page.isNextEnabled(),
                "Next button should be DISABLED on the last page");
        Assert.assertTrue(page.isPreviousEnabled(),
                "Previous button should be ENABLED on the last page");

        // --- Navigate back using Previous ---
        page.clickPrevious();
        Assert.assertEquals(page.getCurrentPageNumber(), totalPages - 1,
                "Previous click should land on page " + (totalPages - 1));

        System.out.println("[PLP_005] All pagination control assertions passed.");
    }

    /* ================================================================== */
    /* PLP_006 — Verify product card data format (includes negative check) */
    /* ================================================================== */

    @Test(description = "PLP_006")
    @Story("Data Validation")
    @Description("For every card on page 1, assert price matches regex ^\\$\\d+\\.\\d{2}$, "
            + "rating is between 0.0 and 5.0, and name is not blank. "
            + "Also verify (negative test) that an invalid price format (no '$') does NOT match the regex, "
            + "confirming the format guard would catch bad data.")
    public void verifyProductCardDetailsFormat() {
        // Valid price format: starts with '$', digits, dot, exactly 2 decimal digits
        Pattern pricePattern = Pattern.compile("^\\$\\d+\\.\\d{2}$");

        // --- Positive assertions for every card on page 1 ---
        List<WebElement> cards = page.getProductCards();
        Assert.assertFalse(cards.isEmpty(), "Page 1 must have at least one product card");

        for (WebElement card : cards) {
            String name = page.getProductName(card);
            String price = page.getProductPrice(card);
            double rating = page.getProductRating(card);
            String cat = page.getProductCategoryName(card);

            System.out.printf("[PLP_006] Name: %-40s | Price: %-10s | Rating: %.1f | Category: %s%n",
                    name, price, rating, cat);

            // Name must not be blank
            Assert.assertFalse(name.isBlank(),
                    "Product name must not be blank. Card content: " + card.getText());

            // Price must match expected format
            Assert.assertTrue(pricePattern.matcher(price).matches(),
                    "Price \"" + price + "\" does not match expected format $XX.XX for: " + name);

            // Rating must be between 0.0 and 5.0 (inclusive)
            Assert.assertTrue(rating >= 0.0 && rating <= 5.0,
                    "Rating " + rating + " is out of range [0, 5] for: " + name);

            // Category must not be blank
            Assert.assertFalse(cat.isBlank(),
                    "Category must not be blank for: " + name);
        }

        // --- Negative assertion: invalid price format (no '$') should NOT match ---
        String invalidPrice = "29.99"; // intentionally missing the dollar sign
        boolean invalidMatches = pricePattern.matcher(invalidPrice).matches();
        Assert.assertFalse(invalidMatches,
                "Price without '$' should NOT match the format regex — format guard is broken.");

        String anotherInvalid = "$29.9"; // only 1 decimal digit
        Assert.assertFalse(pricePattern.matcher(anotherInvalid).matches(),
                "Price with only 1 decimal digit should NOT match — format guard is broken.");

        System.out.println("[PLP_006] Positive and negative format assertions passed for "
                + cards.size() + " product(s).");
    }
}
