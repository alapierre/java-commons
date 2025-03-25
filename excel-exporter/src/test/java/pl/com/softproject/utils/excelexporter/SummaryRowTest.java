package pl.com.softproject.utils.excelexporter;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for verifying summary row functionality in ExcelExporter
 */
public class SummaryRowTest {

    @TempDir
    File tempDir;

    @Test
    public void testAddSummaryRowByIndices() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5),
                new Product("Keyboard", 80.0, 3),
                new Product("Monitor", 300.0, 1)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Products");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add summary row for price and quantity columns (indices 1 and 2)
        List<Integer> columnsToSum = Arrays.asList(1, 2);
        SummaryRowConfig config = SummaryRowConfig.builder()
                .summaryLabel("Total:")
                .build();
        int summaryRowIndex = exporter.addSummaryRow(columnsToSum, config);
        
        // Save to temp file
        File outputFile = new File(tempDir, "summary_test.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Products");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check label
        Cell labelCell = summaryRow.getCell(0);
        assertEquals("Total:", labelCell.getStringCellValue(), "Label should be 'Total:'");
        
        // Check formula in price column (index 1)
        Cell priceCell = summaryRow.getCell(1);
        assertEquals("SUM(B2:B5)", priceCell.getCellFormula(), "Price column should have SUM formula starting from row 1");
        
        // Check formula in quantity column (index 2)
        Cell quantityCell = summaryRow.getCell(2);
        assertEquals("SUM(C2:C5)", quantityCell.getCellFormula(), "Quantity column should have SUM formula starting from row 1");
        
        // Close workbook
        workbook.close();
    }
    
    @Test
    public void testAddSummaryRowByColumnNames() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5),
                new Product("Keyboard", 80.0, 3),
                new Product("Monitor", 300.0, 1)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Products");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add summary row by column names
        List<String> columnsToSum = Arrays.asList("Price", "Quantity");
        SummaryRowConfig config = SummaryRowConfig.builder()
                .summaryLabel("Grand Total:")
                .build();
        int summaryRowIndex = exporter.addSummaryRowByColumnNames(columnsToSum, config);
        
        // Save to temp file
        File outputFile = new File(tempDir, "summary_by_name_test.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Products");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check label
        Cell labelCell = summaryRow.getCell(0);
        assertEquals("Grand Total:", labelCell.getStringCellValue(), "Label should be 'Grand Total:'");
        
        // Check formulas - the columns indices should match the column names
        int priceIndex = exporter.getColumnIndex("Price");
        int quantityIndex = exporter.getColumnIndex("Quantity");
        
        Cell priceCell = summaryRow.getCell(priceIndex);
        Cell quantityCell = summaryRow.getCell(quantityIndex);
        
        String priceColLetter = getCellReference(priceIndex);
        String quantityColLetter = getCellReference(quantityIndex);
        
        assertEquals("SUM(" + priceColLetter + "2:" + priceColLetter + "5)",
                     priceCell.getCellFormula(), 
                     "Price column should have correct SUM formula starting from row 1");
        
        assertEquals("SUM(" + quantityColLetter + "2:" + quantityColLetter + "5)",
                     quantityCell.getCellFormula(), 
                     "Quantity column should have correct SUM formula starting from row 1");
        
        // Close workbook
        workbook.close();
    }
    
    @Test
    public void testAddSummaryRowWithSkipRows() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5),
                new Product("Keyboard", 80.0, 3),
                new Product("Monitor", 300.0, 1)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Products with Skip");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Simulate adding some metadata or info rows
        // Add two empty rows that should be skipped in the sum calculation
        exporter.goToNextRow();
        exporter.goToNextRow();
        
        // Add another set of products after the empty rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add summary row with skipFirstRows=3 (skip header + two empty rows)
        List<Integer> columnsToSum = Arrays.asList(1, 2);
        int skipFirstRows = 3;
        SummaryRowConfig config = SummaryRowConfig.builder()
                .summaryLabel("Total:")
                .skipFirstRows(skipFirstRows)
                .build();
        int summaryRowIndex = exporter.addSummaryRow(columnsToSum, config);
        
        // Save to temp file
        File outputFile = new File(tempDir, "summary_with_skip_test.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Products with Skip");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check formula in price column (index 1) - should start from row 4 (skip first 3 rows)
        Cell priceCell = summaryRow.getCell(1);
        assertEquals("SUM(B4:B11)", priceCell.getCellFormula(),
                     "Price column should have SUM formula starting from row 4");
        
        // Close workbook
        workbook.close();
    }
    
    @Test
    public void testCustomLabelColumnPosition() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Custom Label Position");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Test setting the label in a non-default column (Price column)
        int priceIndex = exporter.getColumnIndex("Price");
        
        // Create config with custom label position
        SummaryRowConfig config = SummaryRowConfig.builder()
                .summaryLabel("SUMMARY:")
                .labelColumnIndex(priceIndex)  // Put label in the Price column
                .build();
        
        // Add summary row by column names with config
        List<String> columnNamesToSum = List.of("Quantity");
        int summaryRowIndex = exporter.addSummaryRowByColumnNames(columnNamesToSum, config);
        
        // Save to temp file
        File outputFile = new File(tempDir, "summary_custom_label.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Custom Label Position");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check that the label is in the Price column
        Cell labelCell = summaryRow.getCell(priceIndex);
        assertNotNull(labelCell, "Label cell should exist in Price column");
        assertEquals("SUMMARY:", labelCell.getStringCellValue(), "Label should be 'SUMMARY:'");
        
        // Check that the first cell (default label position) is empty
        Cell firstCell = summaryRow.getCell(0);
        assertNull(firstCell, "First cell should be empty");
        
        // Close workbook
        workbook.close();
    }
    
    @Test
    public void testMinimalBuilderConfig() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Minimal Config");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Create a minimal config with default values only
        SummaryRowConfig config = SummaryRowConfig.builder().build();
        
        // Add summary row with minimal config
        List<Integer> columnsToSum = Arrays.asList(1, 2);
        int summaryRowIndex = exporter.addSummaryRow(columnsToSum, config);
        
        // Save to temp file
        File outputFile = new File(tempDir, "minimal_config.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Minimal Config");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check default label
        Cell labelCell = summaryRow.getCell(0);
        assertEquals("Total:", labelCell.getStringCellValue(), "Label should use the default 'Total:'");
        
        // Check formulas use default settings (starting from row 1)
        Cell priceCell = summaryRow.getCell(1);
        assertEquals("SUM(B2:B3)", priceCell.getCellFormula(),
                     "Price column should have SUM formula with default settings");
        
        // Close workbook
        workbook.close();
    }
    
    @Test
    public void testSimpleMethodWithoutConfig() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Simple Method");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add summary row using the simplified method (no config needed)
        List<Integer> columnsToSum = Arrays.asList(1, 2);
        int summaryRowIndex = exporter.addSummaryRow(columnsToSum);
        
        // Save to temp file
        File outputFile = new File(tempDir, "simple_method.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Simple Method");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check default label
        Cell labelCell = summaryRow.getCell(0);
        assertEquals("Total:", labelCell.getStringCellValue(), "Label should use the default 'Total:'");
        
        // Close workbook
        workbook.close();
    }
    
    @Test
    public void testSimpleMethodByColumnNames() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Simple By Names");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add summary row using the simplified method with column names
        List<String> columnNamesToSum = Arrays.asList("Price", "Quantity");
        int summaryRowIndex = exporter.addSummaryRowByColumnNames(columnNamesToSum);
        
        // Save to temp file
        File outputFile = new File(tempDir, "simple_by_names.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Simple By Names");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check default label
        Cell labelCell = summaryRow.getCell(0);
        assertEquals("Total:", labelCell.getStringCellValue(), "Label should use the default 'Total:'");
        
        // Verify formulas are created for the correct columns
        int priceIndex = exporter.getColumnIndex("Price");
        int quantityIndex = exporter.getColumnIndex("Quantity");
        
        Cell priceCell = summaryRow.getCell(priceIndex);
        Cell quantityCell = summaryRow.getCell(quantityIndex);
        
        assertNotNull(priceCell, "Price column should have a formula");
        assertNotNull(quantityCell, "Quantity column should have a formula");
        
        // Close workbook
        workbook.close();
    }

    private String getCellReference(int columnIndex) {
        return org.apache.poi.ss.util.CellReference.convertNumToColString(columnIndex);
    }
    
    @Data
    @AllArgsConstructor
    static class Product {
        private String name;
        private double price;
        private int quantity;
    }
} 
