package com.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object for the cart page at {@code <base.url>/cart.html} on Swag Labs (see
 * {@code https://www.saucedemo.com/cart.html}).
 *
 * <p>An empty cart simply renders no {@code .cart_item} rows at all (there's no dedicated
 * "your cart is empty" message on this site, confirmed live) - {@link #isEmpty()} reflects
 * that directly rather than looking for text that doesn't exist.
 */
public class CartPage extends BasePage {

    private static final By LINE_ITEMS = By.cssSelector(".cart_item");
    private static final By ITEM_QUANTITY = By.cssSelector(".cart_quantity");
    private static final By REMOVE_BUTTON = By.cssSelector("button");
    private static final By CONTINUE_SHOPPING_BUTTON = By.cssSelector("[data-test='continue-shopping']");

    private final HeaderComponent header = new HeaderComponent();

    public boolean isEmpty() {
        return $$(LINE_ITEMS).size() == 0;
    }

    public int getLineItemCount() {
        return $$(LINE_ITEMS).size();
    }

    public boolean hasProduct(String productName) {
        return $$(LINE_ITEMS).findBy(Condition.text(productName)).is(Condition.visible);
    }

    public String getLineItemQuantity(String productName) {
        return itemFor(productName).find(ITEM_QUANTITY).getText();
    }

    public CartPage removeProduct(String productName) {
        itemFor(productName).find(REMOVE_BUTTON).shouldBe(Condition.enabled).click();
        return this;
    }

    public int getCartItemCount() {
        return header.getCartItemCount();
    }

    public InventoryPage continueShopping() {
        click(CONTINUE_SHOPPING_BUTTON);
        return new InventoryPage();
    }

    private SelenideElement itemFor(String productName) {
        return $$(LINE_ITEMS).findBy(Condition.text(productName)).shouldBe(Condition.visible);
    }
}
