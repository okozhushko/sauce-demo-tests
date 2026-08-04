package com.example.pages;

import com.codeborne.selenide.Condition;
import com.example.config.Config;
import com.example.utils.WaitUtils;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

/**
 * Shared header region present on every authenticated page of Swag Labs
 * (see {@code https://www.saucedemo.com/inventory.html}) - the cart link and its item-count
 * badge. Composed into page objects rather than duplicating these locators on each one.
 *
 * <p>The badge ({@code .shopping_cart_badge}) is only rendered in the DOM when the cart is
 * non-empty (confirmed live: it's entirely absent, not just hidden, for an empty cart) - see
 * {@link #getCartItemCount()}.
 */
public class HeaderComponent extends BasePage {

    private static final By CART_LINK = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By CART_BADGE = By.cssSelector(".shopping_cart_badge");

    public int getCartItemCount() {
        var badge = $(CART_BADGE);
        return badge.exists() ? Integer.parseInt(badge.getText().trim()) : 0;
    }

    /**
     * Waits for the cart badge to reflect the expected count. The badge disappears entirely
     * (rather than showing "0") once the cart is empty, so {@code expected == 0} waits for
     * it to be gone instead of waiting for text - see {@link WaitUtils} for the bounded,
     * condition-based wait this delegates to for the non-zero case.
     */
    public void waitForCartItemCount(int expected) {
        if (expected == 0) {
            $(CART_BADGE).shouldBe(Condition.disappear, Duration.ofMillis(Config.getTimeout()));
        } else {
            WaitUtils.waitForText(CART_BADGE, String.valueOf(expected));
        }
    }

    public CartPage goToCart() {
        click(CART_LINK);
        return new CartPage();
    }
}
