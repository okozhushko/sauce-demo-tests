package com.example.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

/**
 * Enhanced retry analyzer for Safari tests. Safari WebDriver is less stable than
 * Chrome/Firefox/Edge, so tests running on Safari get 3 retry attempts instead of the
 * default 2. Attach this analyzer to @Test methods that run on Safari, or apply it
 * globally to all UI tests if they run across all browsers including Safari.
 */
public class SafariRetryAnalyzer extends RetryAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(SafariRetryAnalyzer.class);
    private static final int SAFARI_RETRY_COUNT = 3;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < SAFARI_RETRY_COUNT && result.getStatus() == ITestResult.FAILURE) {
            retryCount++;
            logger.warn("Safari test {} failed. Retry attempt: {}/{}",
                    result.getMethod().getMethodName(), retryCount, SAFARI_RETRY_COUNT);
            return true;
        }
        return false;
    }
}
