package com.example.pages;

import org.openqa.selenium.By;

/**
 * Page Object for the post-login secure area at {@code <base.url>/secure}
 * (see {@code the-internet.herokuapp.com/secure}).
 */
public class DashboardPage extends BasePage {

    private static final By SECURE_AREA_HEADING = By.cssSelector("#content h2");
    private static final By WELCOME_MESSAGE = By.cssSelector("#content h4.subheader");
    // href-based rather than class-based: the class list is a styling concern, the
    // destination is the functional identity of this control.
    private static final By LOGOUT_BUTTON = By.cssSelector("a[href='/logout']");
    private static final By FLASH_MESSAGE = By.id("flash");

    public boolean isUserLoggedIn() {
        return isVisible(SECURE_AREA_HEADING) && "Secure Area".equals(getText(SECURE_AREA_HEADING));
    }

    public String getWelcomeMessage() {
        return getText(WELCOME_MESSAGE);
    }

    public String getFlashMessage() {
        return getText(FLASH_MESSAGE);
    }

    public LoginPage logout() {
        click(LOGOUT_BUTTON);
        return new LoginPage();
    }
}
