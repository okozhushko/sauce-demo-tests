package com.example.listeners;

import com.codeborne.selenide.WebDriverRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        logger.info(">>> {} started", testName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("<<< {} passed", testName(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("<<< {} failed: {}", testName(result), result.getThrowable() != null
                ? result.getThrowable().getMessage() : "unknown reason");
        if (WebDriverRunner.hasWebDriverStarted()) {
            AllureAttachments.attachScreenshot();
            AllureAttachments.attachPageSource();
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("<<< {} skipped", testName(result));
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("=== Test suite started: {} ===", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("=== Test suite finished: {} (passed: {}, failed: {}, skipped: {}) ===",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    private String testName(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
}
