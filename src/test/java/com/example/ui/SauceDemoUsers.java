package com.example.ui;

/**
 * Public test account credentials for Swag Labs (see {@code https://www.saucedemo.com/}).
 * These are not secrets - the site is a purpose-built Selenium/Playwright QA training demo
 * that ships these exact accounts for automation to use, documented on its own login page.
 * Ordinary test data, not something "supplied later" via config (unlike a real store, there
 * is no private credential-provisioning problem here to work around).
 */
final class SauceDemoUsers {

    static final String PASSWORD = "secret_sauce";

    /** Logs in normally. */
    static final String STANDARD_USER = "standard_user";

    /** Login is rejected with "Epic sadface: Sorry, this user has been locked out." - the
     * site's standard negative-login test case. */
    static final String LOCKED_OUT_USER = "locked_out_user";

    private SauceDemoUsers() {
    }
}
