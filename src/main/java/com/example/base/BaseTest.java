package com.example.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.ITestResult;

public class BaseTest {

    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @BeforeMethod
    public void setUp(ITestResult testResult) {
        logger.info("Starting test: {}", testResult.getMethod().getMethodName());
    }

    @AfterMethod
    public void tearDown(ITestResult testResult) {
        logger.info("Finished test: {} with status: {}", testResult.getMethod().getMethodName(),
                statusName(testResult.getStatus()));
    }

    private String statusName(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "SUCCESS";
            case ITestResult.FAILURE -> "FAILURE";
            case ITestResult.SKIP -> "SKIP";
            default -> "UNKNOWN";
        };
    }
}
