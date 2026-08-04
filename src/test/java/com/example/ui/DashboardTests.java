package com.example.ui;

import com.example.base.BaseWebTest;
import com.example.pages.DashboardPage;
import com.example.pages.LoginPage;
import com.example.retry.RetryAnalyzer;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Smoke coverage for the secure area reached after a successful login. Each test logs in
 * independently (no shared/@BeforeMethod session) so tests stay order-agnostic and safe
 * to run in parallel across workers.
 */
@Story("Secure area dashboard")
public class DashboardTests extends BaseWebTest {

    private static final String VALID_USERNAME = "tomsmith";
    private static final String VALID_PASSWORD = "SuperSecretPassword!";

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("Secure area should show the logged-in heading and welcome message")
    public void testSecureAreaIsDisplayedAfterLogin() {
        DashboardPage dashboardPage = loginAsValidUser();

        Assert.assertTrue(dashboardPage.isUserLoggedIn(), "Secure area heading should be visible after login");
        Assert.assertTrue(dashboardPage.getWelcomeMessage().contains("Welcome to the Secure Area"),
                "Welcome message should mention the secure area");
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("Logging out from the secure area returns the user to the login page")
    public void testLogoutReturnsToLoginPage() {
        DashboardPage dashboardPage = loginAsValidUser();

        LoginPage loginPage = dashboardPage.logout();

        Assert.assertTrue(loginPage.isErrorMessageVisible(), "A flash message should confirm the logout");
        Assert.assertTrue(loginPage.getErrorMessage().contains("You logged out"),
                "Flash message should confirm the logout");
    }

    private DashboardPage loginAsValidUser() {
        return new LoginPage()
                .open()
                .enterUsername(VALID_USERNAME)
                .enterPassword(VALID_PASSWORD)
                .clickLogin();
    }
}
