package com.example.utils;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;

/**
 * On-demand screenshot capture for UI tests that want to document an intermediate state
 * mid-flow (e.g. before a risky step, or alongside a soft assertion).
 *
 * <p>This is deliberately separate from failure reporting: {@code TestListener} already
 * attaches a screenshot (and page source) to Allure automatically whenever a UI test
 * fails, via {@code com.example.listeners.AllureAttachments}. Don't call this from a
 * {@code catch}/failure branch to replicate that - call it only for planned,
 * non-failure checkpoints.
 */
public final class ScreenshotUtils {

    private ScreenshotUtils() {
    }

    /**
     * Captures the current browser viewport and attaches it to the Allure report under
     * the given name.
     */
    public static void capture(String name) {
        attach(name, Selenide.screenshot(OutputType.BYTES));
    }

    @Attachment(value = "{name}", type = "image/png")
    private static byte[] attach(String name, byte[] screenshot) {
        return screenshot;
    }
}
