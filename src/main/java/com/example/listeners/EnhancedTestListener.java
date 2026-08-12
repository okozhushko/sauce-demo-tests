package com.example.listeners;

import com.codeborne.selenide.WebDriverRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test listener with practical failure reporting. Shows:
 * - Direct link to test file (class + line number)
 * - Failure reason (locator, timeout, assertion, etc.)
 * - Full stack trace
 * - Actual vs Expected values
 */
public class EnhancedTestListener implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedTestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("START: {}.{}",
                result.getTestClass().getName(),
                result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        logger.info("✅ PASS: {}.{} [{}ms]",
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                duration);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable throwable = result.getThrowable();
        long duration = result.getEndMillis() - result.getStartMillis();

        logger.error("\n" +
                "═══════════════════════════════════════════════════════════════════════\n" +
                "❌ TEST FAILED\n" +
                "═══════════════════════════════════════════════════════════════════════");

        // Test link
        String testLink = String.format("%s.%s",
                result.getTestClass().getName(),
                result.getMethod().getMethodName());
        logger.error("TEST: {}", testLink);

        // File location with line number
        StackTraceElement failurePoint = extractFailurePoint(throwable);
        if (failurePoint != null) {
            logger.error("FILE: {}", String.format("%s:%d",
                    failurePoint.getFileName(),
                    failurePoint.getLineNumber()));
        }

        logger.error("TIME: {}ms\n", duration);

        if (throwable != null) {
            // Failure reason
            String reason = extractFailureReason(throwable);
            logger.error("REASON: {}\n", reason);

            // Error type and message
            logger.error("ERROR: {}", throwable.getClass().getSimpleName());
            if (throwable.getMessage() != null) {
                logger.error("MESSAGE:\n{}\n", throwable.getMessage());
            }

            // Actual vs Expected
            AssertionDetails details = extractAssertionDetails(throwable);
            if (details.hasValues()) {
                logger.error("EXPECTED: {}", details.expected);
                logger.error("ACTUAL:   {}\n", details.actual);
            }

            // Full stack trace
            logger.error("STACK TRACE:");
            logFullStackTrace(throwable);
        }

        logger.error("═══════════════════════════════════════════════════════════════════════\n");

        // Attach screenshots for UI tests
        if (WebDriverRunner.hasWebDriverStarted()) {
            AllureAttachments.attachScreenshot();
            AllureAttachments.attachPageSource();
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("⏭️  SKIP: {}.{}",
                result.getTestClass().getName(),
                result.getMethod().getMethodName());
        if (result.getThrowable() != null && result.getThrowable().getMessage() != null) {
            logger.warn("   Reason: {}", result.getThrowable().getMessage());
        }
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("═══════════════════════════════════════════════════════════════════════");
        logger.info("SUITE START: {} ({} tests)", context.getName(), context.getAllTestMethods().length);
        logger.info("═══════════════════════════════════════════════════════════════════════\n");
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        int total = passed + failed + skipped;

        logger.info("\n" +
                "═══════════════════════════════════════════════════════════════════════\n" +
                "SUITE FINISHED: {}\n" +
                "TOTAL: {} | PASSED: {} | FAILED: {} | SKIPPED: {}\n" +
                "SUCCESS RATE: {}\n" +
                "═══════════════════════════════════════════════════════════════════════\n",
                context.getName(),
                total, passed, failed, skipped,
                formatSuccessRate(passed, total));
    }

    private StackTraceElement extractFailurePoint(Throwable throwable) {
        if (throwable == null) return null;

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.startsWith("com.example.ui.") || className.startsWith("com.example.api.")) {
                return element;
            }
        }

        if (stackTrace.length > 0) {
            return stackTrace[0];
        }
        return null;
    }

    private String extractFailureReason(Throwable throwable) {
        String message = throwable.getMessage() != null ? throwable.getMessage() : "";
        String className = throwable.getClass().getSimpleName();

        // Selenide locator not found
        if (message.contains("Element not found")) {
            Pattern locatorPattern = Pattern.compile("\\{([^}]+)\\}");
            Matcher matcher = locatorPattern.matcher(message);
            if (matcher.find()) {
                return String.format("Locator not found: %s", matcher.group(1));
            }
            return "Element not found on page";
        }

        // Timeout
        if (message.contains("Timeout") || message.contains("timeout")) {
            return "Timeout waiting for condition";
        }

        // Assertion failure
        if (className.contains("AssertionError")) {
            if (message.contains("expected") && message.contains("but found")) {
                return "Value mismatch in assertion";
            }
            return "Assertion failed";
        }

        // NoSuchElement
        if (message.contains("Cannot locate element")) {
            return "Element locator failed";
        }

        // StaleElement
        if (className.contains("StaleElement")) {
            return "Element became stale (DOM changed)";
        }

        // Default
        return className;
    }

    private AssertionDetails extractAssertionDetails(Throwable throwable) {
        String message = throwable.getMessage() != null ? throwable.getMessage() : "";

        // TestNG format: "expected [X] but found [Y]"
        Pattern testNgPattern = Pattern.compile(
                "expected \\[([^\\]]*)]\\s+but found\\s+\\[([^\\]]*)\\]",
                Pattern.DOTALL);
        Matcher matcher = testNgPattern.matcher(message);
        if (matcher.find()) {
            return new AssertionDetails(matcher.group(1).trim(), matcher.group(2).trim());
        }

        // Selenide Element not found with locator details
        if (message.contains("expected:") && message.contains("actual:")) {
            Pattern pattern = Pattern.compile(
                    "expected:\\s*([^\\n]+)[\\n\\s]*actual:\\s*([^\\n]+)",
                    Pattern.DOTALL);
            matcher = pattern.matcher(message);
            if (matcher.find()) {
                return new AssertionDetails(matcher.group(1).trim(), matcher.group(2).trim());
            }
        }

        return new AssertionDetails("", "");
    }

    private void logFullStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        String[] lines = stackTrace.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                logger.error("  {}", line);
            }
        }
    }

    private String formatSuccessRate(int passed, int total) {
        if (total == 0) return "N/A";
        double rate = (double) passed / total * 100;
        return String.format("%.1f%% (%d/%d)", rate, passed, total);
    }

    private static class AssertionDetails {
        final String expected;
        final String actual;

        AssertionDetails(String expected, String actual) {
            this.expected = expected;
            this.actual = actual;
        }

        boolean hasValues() {
            return !expected.isEmpty() || !actual.isEmpty();
        }
    }
}
