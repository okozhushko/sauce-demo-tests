package com.example.pages;

import com.codeborne.selenide.Selenide;
import com.example.config.Config;
import org.openqa.selenium.By;

/**
 * Page Object for the login page at {@code <base.url>/login}
 * (see {@code the-internet.herokuapp.com/login}).
 *
 * <p>Locators are attribute-based ({@code id}) rather than style-based, since this is the
 * most stable option Selenide's {@code By} API offers on this markup (no accessible-role
 * based selectors, unlike Playwright).
 */
public class LoginPage extends BasePage {

    private static final By USERNAME_FIELD = By.id("username");
    private static final By PASSWORD_FIELD = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    // The login and secure-area pages share the same #flash element for both error and
    // success/logout confirmation messages.
    private static final By FLASH_MESSAGE = By.id("flash");

    public LoginPage open() {
        Selenide.open(Config.getBaseUrl() + "/login");
        return this;
    }

    public LoginPage enterUsername(String username) {
        fillField(USERNAME_FIELD, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        fillField(PASSWORD_FIELD, password);
        return this;
    }

    public LoginPage login(String username, String password) {
        return enterUsername(username).enterPassword(password);
    }

    public DashboardPage clickLogin() {
        click(LOGIN_BUTTON);
        return new DashboardPage();
    }

    public String getErrorMessage() {
        return getText(FLASH_MESSAGE);
    }

    public boolean isErrorMessageVisible() {
        return isVisible(FLASH_MESSAGE);
    }
}
