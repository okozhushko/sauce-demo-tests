package com.example.ui;

import com.example.base.BaseWebTest;
import com.example.pages.InventoryPage;
import com.example.pages.LoginPage;
import com.example.retry.RetryAnalyzer;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Login coverage against Swag Labs (see {@code https://www.saucedemo.com/}) - a
 * purpose-built Selenium/Playwright QA training demo site, not a real store. It has no
 * captcha and ships well-known public test accounts (all with password
 * {@code secret_sauce}), so both the valid- and invalid-login cases run for real on every
 * execution - see {@link SauceDemoUsers}.
 *
 * <p>{@code retryAnalyzer} is attached here (not as a blanket default) because every test
 * in this class depends on a real network round-trip to a public, third-party site -
 * exactly the kind of environment flakiness retries exist to absorb.
 */
@Story("Authentication")
public class LoginTests extends BaseWebTest {

    @Test(groups = {"smoke", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @Description("A standard user's valid credentials should sign in and land on the inventory page")
    public void testValidLogin() {
        InventoryPage inventoryPage = new LoginPage()
                .open()
                .login(SauceDemoUsers.STANDARD_USER, SauceDemoUsers.PASSWORD);

        Assert.assertTrue(inventoryPage.isProductDisplayed("Sauce Labs Backpack"),
                "A successful login should land on the inventory page showing the product catalog");
    }

    @Test(groups = {"regression"}, retryAnalyzer = RetryAnalyzer.class)
    @Description("A locked-out user's credentials should be rejected with the site's lockout error")
    public void testInvalidLogin() {
        LoginPage loginPage = new LoginPage()
                .open()
                .attemptLogin(SauceDemoUsers.LOCKED_OUT_USER, SauceDemoUsers.PASSWORD);

        Assert.assertTrue(loginPage.isOnLoginPage(),
                "A locked-out login attempt should not navigate away from the login page");
        Assert.assertTrue(loginPage.isErrorDisplayed(), "A locked-out login attempt should show an error banner");
        Assert.assertTrue(loginPage.getErrorMessage().contains("locked out"),
                "Error banner should explain the account is locked out, was: '%s'"
                        .formatted(loginPage.getErrorMessage()));
    }
}
