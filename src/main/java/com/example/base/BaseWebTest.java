package com.example.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.example.config.BrowserConfig;
import com.example.config.Config;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base class for UI tests. Opens a Selenide-managed browser before each test method and
 * closes it afterwards.
 *
 * <p>The browser is driven by the {@code browser} TestNG suite {@code <parameter>} (see
 * {@code testng.xml}/{@code testng-parallel.xml}), falling back to the {@code browser}
 * property (via {@link BrowserConfig}) when a suite doesn't inject one - e.g. when a test
 * class is run directly outside a suite. Timeouts and headless mode are sourced from
 * {@link Config}/{@link BrowserConfig}, never hardcoded here.
 *
 * <p>Test start/finish logging is already handled by {@link BaseTest}; screenshot- and
 * page-source-on-failure is already handled automatically by
 * {@code com.example.listeners.TestListener} via {@code AllureAttachments}. This class
 * owns only the browser lifecycle - don't duplicate either concern here.
 */
public class BaseWebTest extends BaseTest {

    @BeforeMethod
    @Parameters("browser")
    public void setUpBrowser(@Optional String browser) {
        String resolvedBrowser = (browser != null && !browser.isBlank()) ? browser : BrowserConfig.getBrowser();

        Configuration.browser = resolvedBrowser;
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = Config.getTimeout();
        Configuration.pageLoadTimeout = Config.getPageLoadTimeout();
        Configuration.headless = BrowserConfig.isHeadless(resolvedBrowser);

        logger.info("Opening browser: {} (headless={})", resolvedBrowser, Configuration.headless);
    }

    @AfterMethod
    public void tearDownBrowser() {
        Selenide.closeWebDriver();
        logger.info("Browser closed");
    }
}
