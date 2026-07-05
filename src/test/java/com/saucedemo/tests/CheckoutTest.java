package com.saucedemo.tests;

import com.aventstack.extentreports.Status;
import com.saucedemo.base.BaseTest;
import com.saucedemo.listeners.TestListener;
import com.saucedemo.pages.CartPage;
import com.saucedemo.pages.CheckoutCompletePage;
import com.saucedemo.pages.CheckoutStepOnePage;
import com.saucedemo.pages.CheckoutStepTwoPage;
import com.saucedemo.pages.InventoryPage;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.utils.ExcelReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CheckoutTest extends BaseTest {

    private static final String VALID_USERNAME = "standard_user";
    private static final String VALID_PASSWORD = "secret_sauce";

    private static final String TEST_DATA_PATH = "src/test/resources/TestData.xlsx";
    private static final String CHECKOUT_SHEET_NAME = "CheckoutData";

    @DataProvider(name = "checkoutData", parallel = true)
    public Object[][] checkoutDataProvider() {
        return ExcelReader.getTestData(TEST_DATA_PATH, CHECKOUT_SHEET_NAME);
    }

    @Test(dataProvider = "checkoutData")
    public void testCompletePurchaseFlow(String firstName, String lastName, String postalCode) {

        TestListener.getTest().log(Status.INFO, "Logging in as: " + VALID_USERNAME);
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login(VALID_USERNAME, VALID_PASSWORD);

        InventoryPage inventoryPage = new InventoryPage(getDriver());
        Assert.assertTrue(inventoryPage.isInventoryPageDisplayed(), "Login did not land on the Inventory page");
        TestListener.getTest().log(Status.PASS, "Login successful, Inventory page displayed");

        inventoryPage.addFirstItemToCart();
        String badgeCount = inventoryPage.getCartBadgeCount();
        Assert.assertEquals(badgeCount, "1", "Cart badge did not update to 1 after adding an item");
        TestListener.getTest().log(Status.PASS, "Item added to cart, badge count confirmed: " + badgeCount);

        inventoryPage.goToCart();
        CartPage cartPage = new CartPage(getDriver());
        int cartItemCount = cartPage.getCartItemCount();
        Assert.assertEquals(cartItemCount, 1, "Expected exactly 1 item in the cart");
        TestListener.getTest().log(Status.PASS,
                "Cart page confirmed " + cartItemCount + " item(s): " + cartPage.getCartItemNames());

        cartPage.clickCheckout();
        CheckoutStepOnePage checkoutStepOne = new CheckoutStepOnePage(getDriver());
        checkoutStepOne.enterShippingInfo(firstName, lastName, postalCode);
        checkoutStepOne.clickContinue();
        TestListener.getTest().log(Status.PASS,
                "Shipping information submitted for: " + firstName + " " + lastName + ", " + postalCode);

        CheckoutStepTwoPage checkoutStepTwo = new CheckoutStepTwoPage(getDriver());
        String itemTotal = checkoutStepTwo.getItemTotalText();
        String tax = checkoutStepTwo.getTaxText();
        String total = checkoutStepTwo.getTotalText();
        Assert.assertTrue(itemTotal.contains("Item total"), "Item total line not found on order summary");
        TestListener.getTest().log(Status.PASS,
                "Order summary confirmed - " + itemTotal + " | " + tax + " | " + total);

        checkoutStepTwo.clickFinish();
        CheckoutCompletePage completePage = new CheckoutCompletePage(getDriver());
        Assert.assertTrue(completePage.isOrderCompleteDisplayed(), "Order confirmation banner was not displayed");

        String confirmationText = completePage.getCompleteHeaderText();
        Assert.assertEquals(confirmationText, "Thank you for your order!",
                "Confirmation header text did not match expected value");
        TestListener.getTest().log(Status.PASS, "Order completed successfully: \"" + confirmationText + "\"");
    }
}
