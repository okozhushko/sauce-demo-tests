package com.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import com.example.config.Config;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

/**
 * Shared Selenide element helpers for Page Objects. Concrete Page Objects extend this
 * class, keep their locators {@code private static final By}, and add page-specific
 * fluent action methods on top.
 *
 * <p>No assertions live here - only interaction and state-reading helpers. Assertions
 * belong in the test class. Every helper relies on Selenide's built-in auto-wait
 * ({@code shouldBe}/{@code is}) rather than a fixed sleep.
 */
public class BasePage {

    protected SelenideElement element(By locator) {
        return $(locator);
    }

    protected void click(By locator) {
        element(locator).shouldBe(Condition.enabled).click();
    }

    protected void fillField(By locator, String text) {
        element(locator).shouldBe(Condition.visible).setValue(text);
    }

    protected String getText(By locator) {
        return element(locator).shouldBe(Condition.visible).getText();
    }

    /**
     * Non-throwing visibility check: waits up to the configured timeout for the element
     * to become visible and returns {@code false} instead of throwing if it never does.
     * Use this (not {@code shouldBe}) when visibility itself is the thing under test,
     * e.g. checking whether an error message appeared.
     */
    protected boolean isVisible(By locator) {
        return element(locator).is(Condition.visible, Duration.ofMillis(Config.getTimeout()));
    }

    /**
     * Clicks {@code element}, then confirms {@code confirmation} within a fraction of the
     * configured timeout, re-clicking if it doesn't hold - bounded overall by the same
     * configured timeout, not beyond it. This is not a longer wait for one click's effect
     * to eventually show up; it's a retry of the click itself.
     *
     * <p>Guards a reproduced failure mode (see {@code ShoppingCartTests} flakiness
     * investigation, captured screenshot + page source on a real timeout): under load, a
     * single WebDriver click can be silently swallowed - it throws nothing, {@code click()}
     * returns normally, but the page's state never updates (e.g. a coordinate-based native
     * click racing a layout reflow, or the tab's JS thread being starved right when the
     * event needed handling). A plain click has no built-in verification of its own effect,
     * so on a real signal it didn't land, this re-issues it rather than only waiting longer
     * for a click that may never have registered in the first place.
     */
    protected void clickUntil(SelenideElement element, WebElementCondition confirmation) {
        long deadline = System.currentTimeMillis() + Config.getTimeout();
        Duration attemptWindow = Duration.ofMillis(Math.max(1000, Config.getTimeout() / 5));
        do {
            element.shouldBe(Condition.enabled).click();
        } while (!element.is(confirmation, attemptWindow) && System.currentTimeMillis() < deadline);
    }
}
