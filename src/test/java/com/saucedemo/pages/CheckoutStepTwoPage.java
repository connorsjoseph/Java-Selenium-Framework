package com.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CheckoutStepTwoPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By itemTotalLabel = By.className("summary_subtotal_label");
    private final By taxLabel = By.className("summary_tax_label");
    private final By totalLabel = By.className("summary_total_label");
    private final By finishButton = By.id("finish");
    private final By cancelButton = By.id("cancel");

    public CheckoutStepTwoPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getItemTotalText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(itemTotalLabel)).getText();
    }

    public String getTaxText() {
        return driver.findElement(taxLabel).getText();
    }

    public String getTotalText() {
        return driver.findElement(totalLabel).getText();
    }

    public void clickFinish() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(finishButton));
        button.click();
    }

    public void clickCancel() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(cancelButton));
        button.click();
    }
}
