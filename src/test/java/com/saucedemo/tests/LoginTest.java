package com.saucedemo.tests;

import com.saucedemo.base.BaseTest;
import com.saucedemo.pages.InventoryPage;
import com.saucedemo.pages.LoginPage;
import com.saucedemo.utils.ExcelReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    private static final String TEST_DATA_PATH = "src/test/resources/TestData.xlsx";
    private static final String SHEET_NAME = "Sheet1";

    @DataProvider(name = "loginData", parallel = true)
    public Object[][] loginDataProvider() {
        return ExcelReader.getTestData(TEST_DATA_PATH, SHEET_NAME);
    }

    @Test(dataProvider = "loginData")
    public void testLogin(String username, String password, String expectedResult) {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login(username, password);

        if (expectedResult.equalsIgnoreCase("pass")) {
            InventoryPage inventoryPage = new InventoryPage(getDriver());
            Assert.assertTrue(
                    inventoryPage.isInventoryPageDisplayed(),
                    "Expected the Inventory page to be displayed for user: " + username);
            Assert.assertEquals(
                    inventoryPage.getPageTitleText(),
                    "Products",
                    "Inventory page title did not match expected value for user: " + username);

        } else if (expectedResult.equalsIgnoreCase("fail")) {
            boolean errorShown = loginPage.isErrorDisplayed();
            Assert.assertTrue(
                    errorShown,
                    "Expected an error message to be displayed for invalid login: " + username);

            String actualError = loginPage.getErrorMessageText();
            Assert.assertFalse(
                    actualError.isEmpty(),
                    "Error message text was empty for invalid login attempt: " + username);

        } else {
            Assert.fail("Unrecognized ExpectedResult value in TestData.xlsx: '" + expectedResult
                    + "'. Must be 'pass' or 'fail'.");
        }
    }
}
