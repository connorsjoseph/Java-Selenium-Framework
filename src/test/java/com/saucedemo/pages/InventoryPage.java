package com.saucedemo.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class InventoryPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageTitle = By.className("title");
    private final By appLogo = By.className("app_logo");
    private final By inventoryContainer = By.id("inventory_container");
    private final By productItems = By.className("inventory_item");
    private final By addToCartButtons = By.cssSelector("button.btn_inventory");
    private final By cartLink = By.className("shopping_cart_link");
    private final By cartBadge = By.className("shopping_cart_badge");

    public InventoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isInventoryPageDisplayed() {
        try {
            WebElement container = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
            return container.isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public String getPageTitleText() {
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        return title.getText();
    }

    public int getProductCount() {
        return driver.findElements(productItems).size();
    }

    public boolean isAppLogoDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(appLogo)).isDisplayed();
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    public void addFirstItemToCart() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(addToCartButtons));
        button.click();
    }

    public void goToCart() {
        WebElement cart = wait.until(ExpectedConditions.elementToBeClickable(cartLink));
        cart.click();
    }

    public String getCartBadgeCount() {
        try {
            WebElement badge = wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge));
            return badge.getText();
        } catch (org.openqa.selenium.TimeoutException e) {
            return "0";
        }
    }
}
