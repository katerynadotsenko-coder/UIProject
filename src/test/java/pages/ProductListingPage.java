package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.models.ProductDetails;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Page Object for the E-commerce Product Listing &amp; Pagination challenge.
 * URL: https://www.cnarios.com/challenges/product-listing-pagination#challenge
 *
 * <p>
 * DOM uses Material UI (MUI) components. Selectors verified against the live
 * page on 2026-03-02.
 * </p>
 */
public class ProductListingPage {

    /* ------------------------------------------------------------------ */
    /* Constants */
    /* ------------------------------------------------------------------ */
    private static final Logger log = LoggerFactory.getLogger(ProductListingPage.class);
    private static final String PAGE_URL = "https://www.cnarios.com/challenges/product-listing-pagination#challenge";

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15);

    /* ------------------------------------------------------------------ */
    /* Locators */
    /* ------------------------------------------------------------------ */

    // Product cards — each card is a MuiCard-root div
    private final By productCards = By.cssSelector("div.MuiCard-root");

    // Inside a card — name is the first h6 (MuiTypography-h6) child
    private final By productNameInCard = By.cssSelector("h6.MuiTypography-h6:first-of-type");

    // Inside a card — price is the last h6 (green, contains '$')
    private final By productPriceInCard = By.cssSelector("h6.MuiTypography-h6:last-of-type");

    // Inside a card — "Category: Books" paragraph
    private final By productCategoryInCard = By.cssSelector("p.MuiTypography-body2");

    // Inside a card — MUI Rating root; numeric value read from aria-label attribute
    private final By productRatingInCard = By.cssSelector("span.MuiRating-root");

    // Pagination
    private final By paginationItems = By.cssSelector("button.MuiPaginationItem-page");
    private final By paginationNext = By.cssSelector("button[aria-label='Go to next page']");
    private final By paginationPrev = By.cssSelector("button[aria-label='Go to previous page']");
    private final By activePage = By.cssSelector("button.MuiPaginationItem-root.Mui-selected");

    /* ------------------------------------------------------------------ */
    /* Fields */
    /* ------------------------------------------------------------------ */

    private final WebDriver driver;
    private final WebDriverWait wait;

    /* ------------------------------------------------------------------ */
    /* Constructor */
    /* ------------------------------------------------------------------ */

    public ProductListingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    /* ------------------------------------------------------------------ */
    /* Navigation */
    /* ------------------------------------------------------------------ */

    /**
     * Navigates to the challenge URL and waits for the first product card
     * to be visible. No iframe switch is needed — the challenge is rendered
     * directly on the main document.
     */
    public void openPage() {
        driver.get(PAGE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(productCards));
    }

    /* ------------------------------------------------------------------ */
    /* Product cards */
    /* ------------------------------------------------------------------ */

    /**
     * Returns all visible product card elements on the current page.
     */
    public List<WebElement> getProductCards() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(productCards));
        return driver.findElements(productCards);
    }

    // ---- Per-card accessors --------------------------------------------

    /**
     * Extracts the product name from a card element.
     */
    public String getProductName(WebElement card) {
        return card.findElement(productNameInCard).getText().trim();
    }

    /**
     * Extracts the product price string from a card element.
     * Expected format: "$XX.XX"
     */
    public String getProductPrice(WebElement card) {
        return card.findElement(productPriceInCard).getText().trim();
    }

    /**
     * Parses the numeric price (double) from a card element.
     * Strips the leading '$' before parsing.
     */
    public double getProductPriceAsDouble(WebElement card) {
        String raw = getProductPrice(card).replace("$", "").trim();
        return Double.parseDouble(raw);
    }

    /**
     * Returns the raw category text from a card, e.g. "Category: Books".
     */
    public String getProductCategory(WebElement card) {
        return card.findElement(productCategoryInCard).getText().trim();
    }

    /**
     * Returns the category name only, stripping the "Category:" prefix.
     */
    public String getProductCategoryName(WebElement card) {
        return getProductCategory(card).replace("Category:", "").trim();
    }

    /**
     * Returns the star rating value (double) for a card.
     * Reads the aria-label of the MuiRating-root span,
     * e.g. "5 Stars" → 5.0.
     */
    public double getProductRating(WebElement card) {
        try {
            WebElement ratingEl = card.findElement(productRatingInCard);
            String ariaLabel = ratingEl.getAttribute("aria-label"); // e.g. "5 Stars"
            if (ariaLabel == null || ariaLabel.isBlank())
                return 0.0;
            return Double.parseDouble(ariaLabel.split("\\s+")[0]);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /* ------------------------------------------------------------------ */
    /* Category filter */
    /* ------------------------------------------------------------------ */

    /**
     * Builds a dynamic XPath that locates a category tile by matching the
     * uppercase h6 text inside a MuiPaper-root that is NOT inside a product card.
     *
     * <p>
     * Tile DOM structure (observed on live page):
     *
     * <pre>
     * &lt;div class="MuiPaper-root ..."&gt;
     *   &lt;h6 class="MuiTypography-h6"&gt;BOOKS&lt;/h6&gt;
     *   &lt;p  class="MuiTypography-body2"&gt;10&lt;/p&gt;
     * &lt;/div&gt;
     * </pre>
     * </p>
     */
    private By categoryTileLocator(String categoryName) {
        return By.xpath(
                "//div//p[contains(text(),'" + categoryName + "')]");
    }

    /**
     * Returns all category filter tile elements visible on the page.
     */
    public List<WebElement> getCategoryFilterBoxes() {
        // Broad selector: any MuiPaper-root NOT inside a product card
        return driver.findElements(By.xpath(
                "//div[contains(@class,'MuiPaper-root')" +
                        " and not(ancestor::div[contains(@class,'MuiCard-root')])" +
                        " and .//h6]"));
    }

    /**
     * Clicks the category filter tile for the given category name.
     * Matching is case-insensitive; the tile's h6 text is uppercase on the page.
     *
     * @param categoryName e.g. "Books", "SPORTS", "home"
     * @throws org.openqa.selenium.TimeoutException if the tile isn't found within
     *                                              the wait timeout
     */
    public void clickCategoryFilter(String categoryName) {
        By locator = categoryTileLocator(categoryName);
        WebElement tile = wait.until(ExpectedConditions.elementToBeClickable(locator));
        tile.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(productCards));
    }

    /* ------------------------------------------------------------------ */
    /* Pagination */
    /* ------------------------------------------------------------------ */

    /**
     * Returns the currently active page number.
     */
    public int getCurrentPageNumber() {
        WebElement active = wait.until(
                ExpectedConditions.visibilityOfElementLocated(activePage));
        return Integer.parseInt(active.getText().trim());
    }

    /**
     * Returns the total number of numbered page buttons in the pagination bar.
     */
    public int getTotalPages() {
        return driver.findElements(paginationItems).size();
    }

    /**
     * Clicks the page-number button for the given page.
     */
    public void clickPageNumber(int page) {
        List<WebElement> buttons = driver.findElements(paginationItems);
        for (WebElement btn : buttons) {
            if (btn.getText().trim().equals(String.valueOf(page))) {
                btn.click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(productCards));
                return;
            }
        }
        throw new IllegalArgumentException("Page button not found: " + page);
    }

    /**
     * Clicks the Next (→) pagination button.
     */
    public void clickNext() {
        driver.findElement(paginationNext).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(productCards));
    }

    /**
     * Clicks the Previous (←) pagination button.
     */
    public void clickPrevious() {
        driver.findElement(paginationPrev).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(productCards));
    }

    /**
     * Returns true if the Next button is enabled (not flagged as disabled by MUI).
     */
    public boolean isNextEnabled() {
        WebElement btn = driver.findElement(paginationNext);
        return btn.isEnabled() && btn.getAttribute("disabled") == null;
    }

    /**
     * Returns true if the Previous button is enabled (not flagged as disabled by
     * MUI).
     */
    public boolean isPreviousEnabled() {
        WebElement btn = driver.findElement(paginationPrev);
        return btn.isEnabled() && btn.getAttribute("disabled") == null;
    }

    public int getProductCardCount() {
        List<WebElement> cards = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(productCards));
        int count = cards.size();
        log.debug("Found {} product cards.", count);
        return count;
    }

    private int getProductCountForCategory(String category) {
        clickCategoryFilter(category);
        int count = getProductCardCount();
        log.info("[PLP_001] Category: {} → {} product(s) on page 1", category, count);
        return count;
    }

    public Map<String, Integer> collectProductCountsForCategories(List<String> categories) {
        openPage();
        Map<String, Integer> results = categories.stream().collect(
                Collectors.toMap(category -> category, this::getProductCountForCategory, (existing, replacement)
                        -> existing, LinkedHashMap::new));
        log.info("[PLP_001] Summary:");
        results.forEach((cat, cnt) -> log.info("  {}: {}", cat, cnt));
        return results;
    }

    public Map<String, ProductDetails> getHighestRatedProductPerCategory(List<String> categories) {
        openPage();
        return categories.stream().collect(Collectors.toMap(category -> category, this::getHighestRatedProductForCategory,
                (existing, replacement) -> existing, LinkedHashMap::new));
    }

    private ProductDetails getHighestRatedProductForCategory(String category) {
        return findHighestRatedProductInCurrentCategory();
    }

    private ProductDetails findHighestRatedProductInCurrentCategory() {
        int totalPages = getTotalPages();

        List<ProductDetails> allProductsInCurrentCategory = IntStream.rangeClosed(1, totalPages)
                .mapToObj(pageNumber -> {
                    clickPageNumber(pageNumber);
                    return getProductCardsDetails();
                })
                .flatMap(Collection::stream)
                .toList();

        return allProductsInCurrentCategory.stream()
                .max(Comparator.comparingDouble(ProductDetails::getRating))
                .orElse(ProductDetails.builder().name("No Product Found").price(BigDecimal.valueOf(0)).rating(0.0).category("N/A").build());
    }

    public List<ProductDetails> getProductCardsDetails() {
        return getProductCards().stream()
                .map(this::extractProductDetailsFromCard)
                .collect(Collectors.toList());
    }

    private ProductDetails extractProductDetailsFromCard(WebElement cardElement) {
// 1. Get the raw string from the UI (e.g., "$29.99")
        String rawPrice = getProductPrice(cardElement);

        // 2. Sanitize: Remove everything EXCEPT digits 0-9 and the decimal point
        String cleanPrice = rawPrice.replaceAll("[^0-9.]", "");
        return ProductDetails.builder()
                .name(getProductName(cardElement))
                .rating(getProductRating(cardElement))
                .category(getProductCategory(cardElement))
                .price(new BigDecimal(cleanPrice)).build();
    }

    public List<ProductDetails> collectProductDetailsForAllCategories() {
        List<ProductDetails> products = new java.util.ArrayList<>();
        int totalPages = getTotalPages();

        IntStream.rangeClosed(1, totalPages).forEach(pageNumber -> {
            clickPageNumber(pageNumber);
            // After filtering and navigating to the page, all cards should be of the selected category.
            // Use getProductCardsDetails() to get fully populated ProductDetails objects.
            products.addAll(getProductCardsDetails());
        });
        return products;
    }
}
