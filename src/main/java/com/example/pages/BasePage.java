package com.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
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
}
