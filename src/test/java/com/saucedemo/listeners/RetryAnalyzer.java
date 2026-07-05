package com.saucedemo.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.concurrent.atomic.AtomicInteger;

public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRY_COUNT = 1;
    private final AtomicInteger retryCount = new AtomicInteger(0);

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount.getAndIncrement() < MAX_RETRY_COUNT) {
            System.out.println("Retrying " + result.getMethod().getMethodName()
                    + " - attempt " + (retryCount.get() + 1) + " of " + (MAX_RETRY_COUNT + 1));
            return true;
        }
        return false;
    }
}
