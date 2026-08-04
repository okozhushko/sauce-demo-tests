package com.example.retry;

import com.example.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetryCount = Config.getRetryCount();
        if (retryCount < maxRetryCount && result.getStatus() == ITestResult.FAILURE) {
            retryCount++;
            logger.warn("Test {} failed. Retry attempt: {}/{}",
                    result.getMethod().getMethodName(), retryCount, maxRetryCount);
            return true;
        }
        return false;
    }
}
