package com.saucedemo.base;

import com.saucedemo.utils.ConfigManager;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class BaseTest {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    private static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    @Parameters("browser")
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("chrome") String browser) throws MalformedURLException {
        WebDriver driver = ConfigManager.isRemoteExecution()
                ? createRemoteDriver(browser)
                : createLocalDriver(browser);

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        if (ConfigManager.isHeadless() || ConfigManager.isRemoteExecution()) {
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
        } else {
            driver.manage().window().maximize();
        }

        setDriver(driver);
        getDriver().get(ConfigManager.getBaseUrl());
    }

    private WebDriver createLocalDriver(String browser) {
        String normalizedBrowser = normalize(browser);

        switch (normalizedBrowser) {

            case "firefox": {
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (ConfigManager.isHeadless()) {
                    firefoxOptions.addArguments("-headless");
                }
                firefoxOptions.addArguments("-width=1920", "-height=1080");
                return new FirefoxDriver(firefoxOptions);
            }

            case "edge": {
                EdgeOptions edgeOptions = new EdgeOptions();
                if (ConfigManager.isHeadless()) {
                    edgeOptions.addArguments("--headless=new");
                }
                edgeOptions.addArguments("--start-maximized");
                edgeOptions.addArguments("--remote-allow-origins=*");
                edgeOptions.setExperimentalOption("prefs", passwordManagerPrefs());

                String edgeBinaryPath = ConfigManager.getEdgeBinaryPath();
                if (edgeBinaryPath != null && !edgeBinaryPath.isBlank()) {
                    edgeOptions.setBinary(edgeBinaryPath);
                }

                return new EdgeDriver(edgeOptions);
            }

            case "chrome":
            default: {
                ChromeOptions chromeOptions = new ChromeOptions();
                if (ConfigManager.isHeadless()) {
                    chromeOptions.addArguments("--headless=new");
                    chromeOptions.addArguments("--no-sandbox");
                    chromeOptions.addArguments("--disable-dev-shm-usage");
                }
                chromeOptions.addArguments("--remote-allow-origins=*");
                chromeOptions.addArguments("--disable-notifications");
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.setExperimentalOption("prefs", passwordManagerPrefs());
                return new ChromeDriver(chromeOptions);
            }
        }
    }

    private WebDriver createRemoteDriver(String browser) throws MalformedURLException {
        String normalizedBrowser = normalize(browser);
        URL hubUrl = URI.create(ConfigManager.getGridUrl()).toURL();

        MutableCapabilities capabilities;
        switch (normalizedBrowser) {
            case "firefox": {
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (ConfigManager.isHeadless()) {
                    firefoxOptions.addArguments("-headless");
                }
                capabilities = firefoxOptions;
                break;
            }
            case "edge": {
                EdgeOptions edgeOptions = new EdgeOptions();
                if (ConfigManager.isHeadless()) {
                    edgeOptions.addArguments("--headless=new");
                }
                capabilities = edgeOptions;
                break;
            }
            case "chrome":
            default: {
                ChromeOptions chromeOptions = new ChromeOptions();
                if (ConfigManager.isHeadless()) {
                    chromeOptions.addArguments("--headless=new");
                }
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                capabilities = chromeOptions;
                break;
            }
        }

        return new RemoteWebDriver(hubUrl, capabilities);
    }

    private Map<String, Object> passwordManagerPrefs() {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_leak_detection", false);
        return prefs;
    }

    private String normalize(String browser) {
        return browser == null ? "chrome" : browser.trim().toLowerCase();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver driver = getDriver();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }
}
