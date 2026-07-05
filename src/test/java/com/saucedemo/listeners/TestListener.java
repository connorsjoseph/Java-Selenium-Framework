package com.saucedemo.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.saucedemo.base.BaseTest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestListener implements ITestListener {

    private static ExtentReports extentReports;
    private static final Map<Long, ExtentTest> extentTestMap = new ConcurrentHashMap<>();
    private static final Map<String, ExtentTest> retryIdentityMap = new ConcurrentHashMap<>();

    private static final String REPORT_PATH = System.getProperty("user.dir")
            + File.separator + "target" + File.separator + "ExtentReport.html";
    private static final String SCREENSHOT_DIR = System.getProperty("user.dir")
            + File.separator + "screenshots" + File.separator;

    @Override
    public void onStart(ITestContext context) {
        new File(SCREENSHOT_DIR).mkdirs();

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(REPORT_PATH);
        sparkReporter.config().setTheme(Theme.DARK);
        sparkReporter.config().setDocumentTitle("SauceDemo Automation Report");
        sparkReporter.config().setReportName("SauceDemo - Selenium 4 + TestNG Execution Report");

        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Application Under Test", "https://www.saucedemo.com");
        extentReports.setSystemInfo("Automation Framework", "Selenium 4 / TestNG / Java 17");
        extentReports.setSystemInfo("Execution Mode", "Cross-browser, parallel, auto-retry enabled");
    }

    @Override
    public void onTestStart(ITestResult result) {
        String identityKey = buildIdentityKey(result);
        ExtentTest test = retryIdentityMap.get(identityKey);

        if (test != null) {
            test.log(Status.WARNING, "Retrying after previous attempt failed...");
        } else {
            String testName = buildParameterizedTestName(result);
            test = extentReports.createTest(testName, result.getMethod().getDescription());
            retryIdentityMap.put(identityKey, test);
        }

        extentTestMap.put(Thread.currentThread().getId(), test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        getTest().log(Status.PASS, "Test passed: " + buildParameterizedTestName(result));
        retryIdentityMap.remove(buildIdentityKey(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = getTest();
        test.log(Status.FAIL, "Test failed: " + buildParameterizedTestName(result));
        test.log(Status.FAIL, result.getThrowable());

        String base64Screenshot = captureScreenshotBase64(result.getMethod().getMethodName());
        if (base64Screenshot != null) {
            test.fail("Screenshot on failure:",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
        }

        retryIdentityMap.remove(buildIdentityKey(result));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String identityKey = buildIdentityKey(result);
        ExtentTest test = retryIdentityMap.get(identityKey);

        if (test != null) {
            String reason = result.getThrowable() != null
                    ? result.getThrowable().getMessage()
                    : "assertion failure";
            test.log(Status.WARNING, "Attempt failed, will retry - " + reason);
            extentTestMap.put(Thread.currentThread().getId(), test);
        } else {
            ExtentTest newTest = extentReports.createTest(
                    buildParameterizedTestName(result), result.getMethod().getDescription());
            String reason = result.getThrowable() != null
                    ? result.getThrowable().getMessage()
                    : "test body never executed";
            newTest.log(Status.SKIP, "Test skipped - setup/configuration failure: " + reason);
            extentTestMap.put(Thread.currentThread().getId(), newTest);
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extentReports != null) {
            extentReports.flush();
        }
    }

    public static ExtentTest getTest() {
        return extentTestMap.get(Thread.currentThread().getId());
    }

    private String buildIdentityKey(ITestResult result) {
        String browser = getBrowserName(result);
        String methodName = result.getMethod().getMethodName();
        String paramsKey = Arrays.deepToString(result.getParameters());
        return browser + "::" + methodName + "::" + paramsKey;
    }

    private String buildParameterizedTestName(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        Object[] params = result.getParameters();
        String browserPrefix = "[" + getBrowserName(result).toUpperCase() + "] ";

        if (params == null || params.length == 0) {
            return browserPrefix + methodName;
        }

        if ("testLogin".equals(methodName)) {
            return browserPrefix + buildLoginTestLabel(methodName, params);
        }

        return browserPrefix + buildGenericParameterizedLabel(methodName, params);
    }

    private String getBrowserName(ITestResult result) {
        try {
            String browser = result.getTestContext().getCurrentXmlTest().getParameter("browser");
            return (browser == null || browser.isBlank()) ? "unknown" : browser;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String buildLoginTestLabel(String methodName, Object[] params) {
        String username = String.valueOf(params[0]);
        String expected = String.valueOf(params[params.length - 1]);
        String label = methodName + " [user: " + username + ", expected: " + expected + "]";

        if (params.length >= 2) {
            String maskedPassword = maskPassword(String.valueOf(params[1]));
            label += " (pwd: " + maskedPassword + ")";
        }

        return label;
    }

    private String buildGenericParameterizedLabel(String methodName, Object[] params) {
        StringBuilder label = new StringBuilder(methodName).append(" [");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                label.append(", ");
            }
            label.append(params[i]);
        }
        label.append("]");
        return label.toString();
    }

    private String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "****";
        }
        int visibleChars = Math.min(2, password.length());
        return password.substring(0, visibleChars) + "****";
    }

    private String captureScreenshotBase64(String testName) {
        WebDriver driver = BaseTest.getDriver();
        if (driver == null) {
            return null;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String fileName = testName + "_" + timestamp + ".png";
        String destinationPath = SCREENSHOT_DIR + fileName;

        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(srcFile.toPath(), Paths.get(destinationPath));
            byte[] fileBytes = Files.readAllBytes(Paths.get(destinationPath));
            return java.util.Base64.getEncoder().encodeToString(fileBytes);
        } catch (IOException e) {
            System.err.println("Failed to capture/save screenshot for " + testName + ": " + e.getMessage());
            return null;
        }
    }
}
