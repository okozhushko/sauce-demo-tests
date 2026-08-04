package com.example.listeners;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Attachment;

public final class AllureAttachments {

    private AllureAttachments() {
    }

    @Attachment(value = "Screenshot", type = "image/png")
    public static byte[] attachScreenshot() {
        return Selenide.screenshot(org.openqa.selenium.OutputType.BYTES);
    }

    @Attachment(value = "Page source", type = "text/plain")
    public static String attachPageSource() {
        return WebDriverRunner.getWebDriver().getPageSource();
    }

    @Attachment(value = "{name}", type = "text/plain")
    public static String attachText(String name, String content) {
        return content;
    }
}
