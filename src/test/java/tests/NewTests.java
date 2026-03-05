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
}
