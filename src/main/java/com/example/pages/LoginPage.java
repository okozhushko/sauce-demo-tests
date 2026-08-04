package com.example.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.example.config.Config;
import org.openqa.selenium.By;

/**
 * Page Object for the Swag Labs login page at {@code <base.url>/} (see
 * {@code https://www.saucedemo.com/}) - the standard Selenium/Playwright QA training demo
 * site. This is a purpose-built practice target: no captcha, no real payment/PII, and it
 * ships well-known public test accounts (all with password {@code secret_sauce}), so there
 * is nothing to redact or supply later here.
 *
 * <p>All locators use the site's {@code data-test} attributes (confirmed live against the
 * rendered markup) - the standard, stable testing hook this site is built to expose, taking
 * priority over its incidental {@code id}s.
 */
public class LoginPage extends BasePage {

    private static final By USERNAME_FIELD = By.cssSelector("[data-test='username']");
    private static final By PASSWORD_FIELD = By.cssSelector("[data-test='password']");
    private static final By LOGIN_BUTTON = By.cssSelector("[data-test='login-button']");
    private static final By ERROR_BANNER = By.cssSelector("[data-test='error']");

    public LoginPage open() {
        Selenide.open(Config.getBaseUrl() + "/");
        return this;
    }

    /**
     * Fills the credentials and submits, then returns the {@link InventoryPage} - use this
     * for the expected/normal case where the credentials are known to succeed (e.g.
     * {@code standard_user}).
     */
    public InventoryPage login(String username, String password) {
        attemptLogin(username, password);
        return new InventoryPage();
    }

    /**
     * Fills the credentials and submits, but stays on {@code LoginPage} - use this for
     * negative-path assertions (e.g. {@code locked_out_user}) where navigation away from the
     * login page is not expected.
     */
    public LoginPage attemptLogin(String username, String password) {
        fillField(USERNAME_FIELD, username);
        fillField(PASSWORD_FIELD, password);
        click(LOGIN_BUTTON);
        return this;
    }

    public boolean isOnLoginPage() {
        return !WebDriverRunner.url().contains("/inventory.html");
    }

    public boolean isErrorDisplayed() {
        return isVisible(ERROR_BANNER);
    }

    public String getErrorMessage() {
        return getText(ERROR_BANNER);
    }
}
