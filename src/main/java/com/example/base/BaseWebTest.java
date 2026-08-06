package com.example.base;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.example.config.BrowserConfig;
import com.example.config.Config;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.util.Map;

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

        boolean headless = BrowserConfig.isHeadless(resolvedBrowser);

        Configuration.browser = resolvedBrowser;
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = Config.getTimeout();
        Configuration.pageLoadTimeout = Config.getPageLoadTimeout();
        Configuration.headless = headless;
        Configuration.browserCapabilities = buildCapabilities(resolvedBrowser, headless);

        logger.info("Opening browser: {} (headless={})", resolvedBrowser, headless);
    }

    /**
     * {@code Configuration.headless} alone was observed NOT to reach the actual launched
     * browser process in CI (confirmed via the driver's own "Command:" log: no
     * {@code --headless}/{@code -headless} argument was present even with
     * {@code Configuration.headless = true}), so headless is applied explicitly here as a
     * CLI argument instead of trusting that flag to propagate on its own.
     *
     * <p>Chromium-based browsers (chrome, edge) also fail to start under GitHub Actions'
     * default runner user with {@code SessionNotCreatedException: Chrome instance exited}
     * unless the OS sandbox is disabled - Chromium's sandbox needs privileges the runner
     * doesn't grant. {@code --no-sandbox} is the standard, widely-documented CI workaround;
     * it's a no-op risk here since every browser instance is a disposable, freshly-launched
     * test session, not a long-lived user browser.
     *
     * <p>Chromium-based browsers also need Chrome's own "Change your password" / Safe
     * Browsing password-leak-detection popup suppressed - it fires natively (outside the
     * page DOM) whenever a credential pair widely known to be public gets submitted through
     * a real login form, which is exactly what happens logging into a public QA demo site
     * with its documented test credentials. That native dialog steals focus and can stall
     * whatever the test does immediately after login, producing flaky "element not found"
     * failures downstream that look unrelated to login. {@code PASSWORD_LEAK_PREFS} disables
     * the specific preference driving it (confirmed live on saucedemo.com, screenshot from
     * a real run) - {@code safebrowsing.enabled} is deliberately overridden to {@code false}
     * here even though Selenide's own base options default it to {@code true}.
     */
    private static final Map<String, Object> PASSWORD_LEAK_PREFS = Map.of(
            "credentials_enable_service", false,
            "profile.password_manager_enabled", false,
            "profile.password_manager_leak_detection", false,
            "safebrowsing.enabled", false
    );

    private MutableCapabilities buildCapabilities(String browser, boolean headless) {
        return switch (browser) {
            case "chrome" -> {
                ChromeOptions options = new ChromeOptions()
                        .addArguments("--no-sandbox", "--disable-dev-shm-usage",
                                "--disable-features=PasswordLeakDetection,PasswordCheck")
                        .setExperimentalOption("prefs", PASSWORD_LEAK_PREFS);
                if (headless) {
                    options.addArguments("--headless=new");
                }
                yield options;
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions()
                        .addArguments("--no-sandbox", "--disable-dev-shm-usage",
                                "--disable-features=PasswordLeakDetection,PasswordCheck")
                        .setExperimentalOption("prefs", PASSWORD_LEAK_PREFS);
                if (headless) {
                    options.addArguments("--headless=new");
                }
                yield options;
            }
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) {
                    options.addArguments("-headless");
                }
                yield options;
            }
            default -> new MutableCapabilities();
        };
    }

    @AfterMethod
    public void tearDownBrowser() {
        Selenide.closeWebDriver();
        logger.info("Browser closed");
    }
}
