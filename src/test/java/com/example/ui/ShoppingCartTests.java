package com.example.ui;

import com.example.base.BaseWebTest;
import com.example.pages.CartPage;
import com.example.pages.InventoryPage;
import com.example.pages.LoginPage;
import com.example.retry.RetryAnalyzer;
import com.example.utils.ScreenshotUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Coverage for browsing the product catalog and the cart on Swag Labs (see
 * {@code https://www.saucedemo.com/}) as {@code standard_user} - see {@link SauceDemoUsers}.
 * Each test starts from a fresh browser (see {@code BaseWebTest}) and logs in independently,
 * so the cart is always empty at the start of a test - no shared cart/session state across
 * tests or workers.
 *
 * <p>{@code retryAnalyzer} is attached here (not as a blanket default) because every test
 * in this class depends on a real network round-trip to a public, third-party site -
 * exactly the kind of environment flakiness retries exist to absorb.
 */
@Story("Catalog and cart")
public class ShoppingCartTests extends BaseWebTest {

    private static final String PRODUCT_NAME = "Sauce Labs Backpack";
    private static final String PRODUCT_PRICE = "$29.990";

    @Test(groups = {"smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @Description("The inventory page should display the known catalog products after login")
    public void testInventoryPageDisplaysProducts() {
        InventoryPage inventoryPage = loginAsStandardUser();

        Assert.assertTrue(inventoryPage.isProductDisplayed(PRODUCT_NAME),
                "Inventory page should display the '%s' product card".formatted(PRODUCT_NAME));
    }

    @Test(groups = {"regression"}, retryAnalyzer = RetryAnalyzer.class)
    @Description("Adding a product from its detail page should update the header cart count mmm")
    public void testAddingProductToCartUpdatesCartCount() {
        InventoryPage inventoryPage = loginAsStandardUser();
        Assert.assertEquals(inventoryPage.getCartItemCount(), 0, "Cart should start empty");

        var productPage = inventoryPage.openProduct(PRODUCT_NAME);
        Assert.assertEquals(productPage.getProductName(), PRODUCT_NAME,
                "Product page should show the selected product's name");
        Assert.assertEquals(productPage.getProductPrice(), PRODUCT_PRICE,
                "Product page should show the selected product's price");

        productPage.addToCart().waitForCartItemCount(1);
        // Planned checkpoint, not failure reporting (TestListener already covers that) -
        // documents the cart state at the moment of a passing assertion.
        ScreenshotUtils.capture("product-added-to-cart");

        Assert.assertEquals(productPage.getCartItemCount(), 1,
                "Header cart count should be 1 after adding a product");
    }

    @Test(groups = {"regression"}, retryAnalyzer = RetryAnalyzer.class)
    @Description("A product added to the cart should appear as a line item on the cart page")
    public void testCartPageReflectsAddedProduct() {
        InventoryPage inventoryPage = loginAsStandardUser();
        inventoryPage.addToCart(PRODUCT_NAME).waitForCartItemCount(1);

        CartPage cartPage = inventoryPage.goToCart();

        Assert.assertFalse(cartPage.isEmpty(), "Cart page should no longer be empty");
        Assert.assertEquals(cartPage.getLineItemCount(), 1, "Cart should contain exactly one line item");
        Assert.assertTrue(cartPage.hasProduct(PRODUCT_NAME),
                "Cart line item should be for the product that was added");
        Assert.assertEquals(cartPage.getLineItemQuantity(PRODUCT_NAME), "1",
                "Cart line item quantity should be 1");
    }

    @Test(groups = {"regression"}, retryAnalyzer = RetryAnalyzer.class)
    @Description("Removing a product from the cart page should empty the cart again")
    public void testRemovingProductFromCartEmptiesCart() {
        InventoryPage inventoryPage = loginAsStandardUser();
        CartPage cartPage = inventoryPage.addToCart(PRODUCT_NAME)
                .waitForCartItemCount(1)
                .goToCart();

        cartPage.removeProduct(PRODUCT_NAME);

        Assert.assertTrue(cartPage.isEmpty(), "Cart page should be empty after removing its only item");
        Assert.assertEquals(cartPage.getCartItemCount(), 0, "Header cart count should be 0 after removal");
    }

    @Test(groups = {"smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @Description("A freshly logged-in session's cart page should show the empty-cart state")
    public void testEmptyCartShowsEmptyState() {
        InventoryPage inventoryPage = loginAsStandardUser();

        CartPage cartPage = inventoryPage.goToCart();

        Assert.assertTrue(cartPage.isEmpty(), "A fresh session's cart should be empty");
        Assert.assertEquals(cartPage.getCartItemCount(), 0, "Header cart count should be 0 for an empty cart");
    }

    private InventoryPage loginAsStandardUser() {
        return new LoginPage().open().login(SauceDemoUsers.STANDARD_USER, SauceDemoUsers.PASSWORD);
    }
}
