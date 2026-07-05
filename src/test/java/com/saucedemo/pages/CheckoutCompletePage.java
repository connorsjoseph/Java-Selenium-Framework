package com.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CheckoutCompletePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By completeHeader = By.className("complete-header");
    private final By backHomeButton = By.id("back-to-products");
    private final By ponyExpressImage = By.className("pony_express");

    public CheckoutCompletePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isOrderCompleteDisplayed() {
        try {
            WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(completeHeader));
            return header.isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public String getCompleteHeaderText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(completeHeader)).getText();
    }

    public boolean isConfirmationImageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(ponyExpressImage)).isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public void clickBackHome() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(backHomeButton));
        button.click();
    }
}
