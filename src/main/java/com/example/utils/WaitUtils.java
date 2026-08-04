package com.example.utils;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.example.config.Config;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

/**
 * Explicit Selenide {@link Condition}-based wait helpers, for the rare case a flow needs
 * a standalone wait step rather than relying on the auto-wait built into
 * {@code SelenideElement.shouldBe(...)} calls inside a Page Object action.
 *
 * <p>Every wait here is condition-based and bounded by a timeout sourced from
 * {@link Config} - there is deliberately no {@code Thread.sleep}/fixed-delay helper.
 * A test that needs a hard sleep to pass is masking a missing wait condition, not a
 * pattern to support.
 *
 * <p>{@code BasePage}'s own helpers ({@code click}/{@code fillField}/{@code getText})
 * already cover ordinary element interaction via {@code shouldBe}, so the current
 * {@code LoginPage}/{@code DashboardPage} don't need to call this directly - it's here
 * for flows that need a wait not tied to a single element/action (e.g. waiting on a URL
 * change, or a timeout different from the configured default). {@code BasePage.isVisible}
 * intentionally does NOT delegate here: it's a non-throwing check (returns {@code false}
 * on timeout), while every wait below throws - different contract, not a duplicate.
 */
public final class WaitUtils {

    private WaitUtils() {
    }

    public static SelenideElement waitForVisible(By locator) {
        return waitForVisible(locator, Config.getTimeout());
    }

    public static SelenideElement waitForVisible(By locator, int timeoutMs) {
        return $(locator).shouldBe(Condition.visible, Duration.ofMillis(timeoutMs));
    }

    public static SelenideElement waitForEnabled(By locator) {
        return waitForEnabled(locator, Config.getTimeout());
    }

    public static SelenideElement waitForEnabled(By locator, int timeoutMs) {
        return $(locator).shouldBe(Condition.enabled, Duration.ofMillis(timeoutMs));
    }

    public static SelenideElement waitForText(By locator, String text) {
        return waitForText(locator, text, Config.getTimeout());
    }

    public static SelenideElement waitForText(By locator, String text, int timeoutMs) {
        return $(locator).shouldHave(Condition.text(text), Duration.ofMillis(timeoutMs));
    }

    public static void waitForUrlContains(String fragment) {
        waitForUrlContains(fragment, Config.getTimeout());
    }

    public static void waitForUrlContains(String fragment, int timeoutMs) {
        Selenide.Wait()
                .withTimeout(Duration.ofMillis(timeoutMs))
                .until(driver -> WebDriverRunner.url().contains(fragment));
    }
}
