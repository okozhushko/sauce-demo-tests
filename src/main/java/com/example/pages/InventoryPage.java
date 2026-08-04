package com.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object for the products/inventory page at {@code <base.url>/inventory.html} on
 * Swag Labs (see {@code https://www.saucedemo.com/inventory.html}) - reached after a
 * successful login via {@link LoginPage#login}. Shows the product grid with an
 * "Add to cart"/"Remove" button per item.
 *
 * <p>Each product card ({@code .inventory_item}) has no per-product {@code id}/data-test of
 * its own (only its button does, e.g. {@code data-test="add-to-cart-sauce-labs-backpack"}),
 * so cards are addressed by filtering on their product-name text rather than reconstructing
 * that slug - the Selenide equivalent of Playwright's {@code .filter({ hasText })}.
 */
public class InventoryPage extends BasePage {

    private static final By PRODUCT_CARDS = By.cssSelector(".inventory_item");
    private static final By PRODUCT_LINK_IN_CARD = By.cssSelector("a");
    private static final By CART_BUTTON_IN_CARD = By.cssSelector("button");

    private final HeaderComponent header = new HeaderComponent();

    public boolean isProductDisplayed(String productName) {
        return cardFor(productName).is(Condition.visible);
    }

    public InventoryPage addToCart(String productName) {
        cardFor(productName).find(CART_BUTTON_IN_CARD).shouldBe(Condition.enabled).click();
        return this;
    }

    public InventoryPage removeFromCart(String productName) {
        cardFor(productName).find(CART_BUTTON_IN_CARD).shouldBe(Condition.enabled).click();
        return this;
    }

    public boolean isInCart(String productName) {
        return cardFor(productName).find(CART_BUTTON_IN_CARD).getText().equalsIgnoreCase("Remove");
    }

    public ProductPage openProduct(String productName) {
        cardFor(productName).find(PRODUCT_LINK_IN_CARD).shouldBe(Condition.visible).click();
        return new ProductPage();
    }

    public int getCartItemCount() {
        return header.getCartItemCount();
    }

    public InventoryPage waitForCartItemCount(int expected) {
        header.waitForCartItemCount(expected);
        return this;
    }

    public CartPage goToCart() {
        return header.goToCart();
    }

    private SelenideElement cardFor(String productName) {
        return $$(PRODUCT_CARDS).findBy(Condition.text(productName)).shouldBe(Condition.visible);
    }
}
