package com.example.ui;

import com.example.base.BaseWebTest;
import com.example.pages.DashboardPage;
import com.example.pages.LoginPage;
import com.example.retry.RetryAnalyzer;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * Login coverage against the public demo target configured via {@code base.url}
 * (see {@code the-internet.herokuapp.com/login}).
 *
 * <p>{@code retryAnalyzer} is attached here (not as a blanket default) because every test
 * in this class depends on a real network round-trip to a public, third-party site -
 * exactly the kind of environment flakiness retries exist to absorb.
 */
@Story("Authentication")
public class LoginTests extends BaseWebTest {

    // The only account the public demo target actually accepts. Fixed and not part of
    // the loginCredentials DataProvider, since that dataset's "success" row is a
    // fictitious account that doesn't exist on this real target.
    private static final String VALID_USERNAME = "tomsmith";
    private static final String VALID_PASSWORD = "SuperSecretPassword!";

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("Valid user should be able to log in and reach the secure area")
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage();
        DashboardPage dashboardPage = loginPage
                .open()
                .enterUsername(VALID_USERNAME)
                .enterPassword(VALID_PASSWORD)
                .clickLogin();

        Assert.assertTrue(dashboardPage.isUserLoggedIn(), "User should land on the secure area after login");
        Assert.assertTrue(dashboardPage.getFlashMessage().contains("You logged into a secure area"),
                "Success flash message should confirm the login");
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,
            dataProvider = "loginCredentials", dataProviderClass = com.example.utils.DataProvider.class)
    @Description("Invalid, locked-out or blank credentials should be rejected with an error message")
    public void testInvalidLogin(String username, String password, String expectedResult) {
        if ("success".equals(expectedResult)) {
            // Not applicable to this target: its "success" row is a fictitious account.
            // Valid-login coverage is exercised separately by testValidLogin() with the
            // one account this real demo site actually accepts.
            throw new SkipException("Success case is covered by testValidLogin() against the real demo account");
        }

        LoginPage loginPage = new LoginPage();
        loginPage
                .open()
                .enterUsername(username)
                .enterPassword(password)
                .clickLogin();

        Assert.assertTrue(loginPage.isErrorMessageVisible(),
                "Error message should be shown for username='%s'".formatted(username));
    }
}
