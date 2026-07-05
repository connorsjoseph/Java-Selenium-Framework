package com.saucedemo.utils;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class ExcelReader {

    public static Object[][] getTestData(String relativeFilePath, String sheetName) {
        String absoluteFilePath = System.getProperty("user.dir")
                + System.getProperty("file.separator")
                + relativeFilePath;

        Object[][] testData;
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(absoluteFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException(
                        "Sheet '" + sheetName + "' was not found in file: " + absoluteFilePath);
            }

            int totalRows = sheet.getLastRowNum();
            int totalColumns = sheet.getRow(0).getLastCellNum();

            testData = new Object[totalRows][totalColumns];

            for (int rowIndex = 1; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                for (int colIndex = 0; colIndex < totalColumns; colIndex++) {
                    String cellValue = "";
                    if (row != null && row.getCell(colIndex) != null) {
                        cellValue = formatter.formatCellValue(row.getCell(colIndex)).trim();
                    }
                    testData[rowIndex - 1][colIndex] = cellValue;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel test data from: " + absoluteFilePath, e);
        }

        return testData;
    }
}
