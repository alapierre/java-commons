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
        int summaryRowIndex = exporter.addSummaryRow(columnsToSum, "Total:");
        
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
        assertEquals("SUM(B1:B5)", priceCell.getCellFormula(), "Price column should have SUM formula starting from row 1");
        
        // Check formula in quantity column (index 2)
        Cell quantityCell = summaryRow.getCell(2);
        assertEquals("SUM(C1:C5)", quantityCell.getCellFormula(), "Quantity column should have SUM formula starting from row 1");
        
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
        int summaryRowIndex = exporter.addSummaryRowByColumnNames(columnsToSum, "Grand Total:");
        
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
        
        assertEquals("SUM(" + priceColLetter + "1:" + priceColLetter + "5)", 
                     priceCell.getCellFormula(), 
                     "Price column should have correct SUM formula starting from row 1");
        
        assertEquals("SUM(" + quantityColLetter + "1:" + quantityColLetter + "5)", 
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
        int summaryRowIndex = exporter.addSummaryRow(columnsToSum, "Total:", skipFirstRows);
        
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
        
        // Check formula in quantity column (index 2)
        Cell quantityCell = summaryRow.getCell(2);
        assertEquals("SUM(C4:C11)", quantityCell.getCellFormula(),
                     "Quantity column should have SUM formula starting from row 4");
        
        // Close workbook
        workbook.close();
    }
    
    @Test
    public void testAddSummaryRowByColumnNamesWithSkipRows() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5),
                new Product("Keyboard", 80.0, 3),
                new Product("Monitor", 300.0, 1)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Products by Name with Skip");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add a comment row that should be excluded from the sum
        exporter.goToNextRow();
        
        // Add more data
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add summary row by column names with skipFirstRows=2 (header + comment row)
        List<String> columnsToSum = Arrays.asList("Price", "Quantity");
        int skipFirstRows = 2;
        int summaryRowIndex = exporter.addSummaryRowByColumnNames(columnsToSum, "Custom Total:", skipFirstRows);
        
        // Save to temp file
        File outputFile = new File(tempDir, "summary_by_name_with_skip_test.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Products by Name with Skip");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check label
        Cell labelCell = summaryRow.getCell(0);
        assertEquals("Custom Total:", labelCell.getStringCellValue(), "Label should be 'Custom Total:'");
        
        // Get column indices
        int priceIndex = exporter.getColumnIndex("Price");
        int quantityIndex = exporter.getColumnIndex("Quantity");
        
        // Get column references
        String priceColLetter = getCellReference(priceIndex);
        String quantityColLetter = getCellReference(quantityIndex);
        
        // Check formulas - should start from row 3 (skip first 2 rows)
        Cell priceCell = summaryRow.getCell(priceIndex);
        assertEquals("SUM(" + priceColLetter + "3:" + priceColLetter + "10)",
                     priceCell.getCellFormula(), 
                     "Price column should have SUM formula starting from row 3");
        
        Cell quantityCell = summaryRow.getCell(quantityIndex);
        assertEquals("SUM(" + quantityColLetter + "3:" + quantityColLetter + "10)",
                     quantityCell.getCellFormula(), 
                     "Quantity column should have SUM formula starting from row 3");
        
        // Close workbook
        workbook.close();
    }

    @Test
    public void testSaveSummaryRowToPermanentFile() throws IOException {
        // Define output directory and ensure it exists
        String outputDirectory = "C:"; // Change as needed
        File directory = new File(outputDirectory);
        if (!directory.exists()) {
            assertTrue(directory.mkdirs(), "Failed to create output directory.");
        }

        // Define output file path
        File outputFile = new File(directory, "summary_test.xlsx");

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

        // Add summary row for "Price" and "Quantity" columns (indices 1 and 2)
        List<Integer> columnsToSum = Arrays.asList(1, 2);
        exporter.addSummaryRow(columnsToSum, "Total:");

        // Save to permanent file
        exporter.save(outputFile);

        // Verify that the file is created
        assertTrue(outputFile.exists(), "The Excel file should be created.");

        // Open and verify content
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Products");

        // Verify summary row
        Row summaryRow = sheet.getRow(sheet.getLastRowNum());
        assertNotNull(summaryRow, "Summary row should exist");

        // Check label
        Cell labelCell = summaryRow.getCell(0);
        assertEquals("Total:", labelCell.getStringCellValue(), "Label should be 'Total:'");

        // Check SUM formulas
        Cell priceCell = summaryRow.getCell(1);
        assertEquals("SUM(B1:B5)", priceCell.getCellFormula(), "Price column should have correct SUM formula");

        Cell quantityCell = summaryRow.getCell(2);
        assertEquals("SUM(C1:C5)", quantityCell.getCellFormula(), "Quantity column should have correct SUM formula");

        // Close workbook
        workbook.close();

        // Print message (optional)
        System.out.println("Excel file saved at: " + outputFile.getAbsolutePath());
    }

    @Test
    public void testCustomLabelColumnPosition() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5),
                new Product("Keyboard", 80.0, 3),
                new Product("Monitor", 300.0, 1)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Products with Custom Label Position");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add summary row with custom label column position (put label in column 2 - "Quantity")
        List<Integer> columnsToSum = Arrays.asList(1, 2);
        int labelColumnIndex = 2;
        int summaryRowIndex = exporter.addSummaryRow(columnsToSum, "TOTALS:", 0, labelColumnIndex);
        
        // Save to temp file
        File outputFile = new File(tempDir, "summary_custom_label_position.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Products with Custom Label Position");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check that the label is in column 2 (Quantity column)
        Cell labelCell = summaryRow.getCell(labelColumnIndex);
        assertNotNull(labelCell, "Label cell should exist in column " + labelColumnIndex);
        assertEquals("TOTALS:", labelCell.getStringCellValue(), "Label should be 'TOTALS:'");
        
        // First column should not have the label
        Cell firstCell = summaryRow.getCell(0);
        assertNull(firstCell, "First cell should be empty");
        
        // Check formula in price column (index 1)
        Cell priceCell = summaryRow.getCell(1);
        assertEquals("SUM(B1:B5)", priceCell.getCellFormula(), "Price column should have SUM formula");
        
        // Close workbook
        workbook.close();
    }
    
    @Test
    public void testCustomLabelColumnByName() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5),
                new Product("Keyboard", 80.0, 3),
                new Product("Monitor", 300.0, 1)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Label by Name");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add summary row with label in the "Price" column
        List<String> columnsToSum = Arrays.asList("Price", "Quantity");
        String labelColumnName = "Price";
        int summaryRowIndex = exporter.addSummaryRowByColumnNames(columnsToSum, "SUMMARY:", labelColumnName);
        
        // Save to temp file
        File outputFile = new File(tempDir, "summary_label_by_name.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Label by Name");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Get Price column index
        int priceIndex = exporter.getColumnIndex("Price");
        int quantityIndex = exporter.getColumnIndex("Quantity");
        
        // Check that the label is in the Price column
        Cell labelCell = summaryRow.getCell(priceIndex);
        assertNotNull(labelCell, "Label cell should exist in Price column");
        assertEquals("SUMMARY:", labelCell.getStringCellValue(), "Label should be 'SUMMARY:'");
        
        // Check that the Quantity column has a SUM formula
        Cell quantityCell = summaryRow.getCell(quantityIndex);
        String quantityColLetter = getCellReference(quantityIndex);
        assertEquals("SUM(" + quantityColLetter + "1:" + quantityColLetter + "5)", 
                     quantityCell.getCellFormula(), 
                     "Quantity column should have SUM formula");
        
        // Close workbook
        workbook.close();
    }

    @Test
    public void testSummaryRowWithBuilder() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5),
                new Product("Keyboard", 80.0, 3),
                new Product("Monitor", 300.0, 1)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Builder Demo");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Create a SummaryRowConfig using the builder pattern
        SummaryRowConfig config = SummaryRowConfig.builder()
                .columnNamesToSum(Arrays.asList("Price", "Quantity"))
                .summaryLabel("GRAND TOTAL:")
                .skipFirstRows(0)
                .labelColumnName("Name")
                .build();
        
        // Add summary row using the config
        int summaryRowIndex = exporter.addSummaryRow(config);
        
        // Save to temp file
        File outputFile = new File(tempDir, "summary_with_builder.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Builder Demo");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Get column indices
        int nameIndex = exporter.getColumnIndex("Name");
        int priceIndex = exporter.getColumnIndex("Price");
        int quantityIndex = exporter.getColumnIndex("Quantity");
        
        // Check that the label is in the Name column
        Cell labelCell = summaryRow.getCell(nameIndex);
        assertNotNull(labelCell, "Label cell should exist in Name column");
        assertEquals("GRAND TOTAL:", labelCell.getStringCellValue(), "Label should be 'GRAND TOTAL:'");
        
        // Verify formulas
        String priceColLetter = getCellReference(priceIndex);
        String quantityColLetter = getCellReference(quantityIndex);
        
        Cell priceCell = summaryRow.getCell(priceIndex);
        assertEquals("SUM(" + priceColLetter + "1:" + priceColLetter + "5)", 
                     priceCell.getCellFormula(), 
                     "Price column should have correct SUM formula");
        
        Cell quantityCell = summaryRow.getCell(quantityIndex);
        assertEquals("SUM(" + quantityColLetter + "1:" + quantityColLetter + "5)", 
                     quantityCell.getCellFormula(), 
                     "Quantity column should have correct SUM formula");
        
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
        ExcelExporter exporter = new ExcelExporter("Minimal Builder");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Create a minimal config - just specifying which columns to sum
        SummaryRowConfig config = SummaryRowConfig.builder()
                .columnsToSum(Arrays.asList(1, 2))
                .build();
        
        // Add summary row with minimal config
        int summaryRowIndex = exporter.addSummaryRow(config);
        
        // Save to temp file
        File outputFile = new File(tempDir, "minimal_builder.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Minimal Builder");
        
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
    public void testSimplifiedConfigApproach() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5),
                new Product("Keyboard", 80.0, 3),
                new Product("Monitor", 300.0, 1)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("Simplified Config");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Add summary row for specific columns using a specific config
        List<Integer> columnsToSum = Arrays.asList(1, 2);
        SummaryRowConfig config = SummaryRowConfig.builder()
                .summaryLabel("TOTAL:")
                .skipFirstRows(1)  // Skip header row
                .labelColumnIndex(2)  // Put label in the Quantity column
                .build();
        
        int summaryRowIndex = exporter.addSummaryRow(columnsToSum, config);
        
        // Save to temp file
        File outputFile = new File(tempDir, "simplified_config.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("Simplified Config");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Check label position (should be in column 2 - Quantity)
        Cell labelCell = summaryRow.getCell(2);
        assertNotNull(labelCell, "Label cell should exist in column 2");
        assertEquals("TOTAL:", labelCell.getStringCellValue(), "Label should be 'TOTAL:'");
        
        // Check formulas - should start from row 2 (skipped header row)
        Cell priceCell = summaryRow.getCell(1);
        assertEquals("SUM(B2:B5)", priceCell.getCellFormula(), 
                     "Price column should have SUM formula starting from row 2");
        
        // Close workbook
        workbook.close();
    }
    
    @Test
    public void testByColumnNames() throws IOException {
        // Create test data
        List<Product> products = Arrays.asList(
                new Product("Laptop", 1500.0, 2),
                new Product("Mouse", 25.0, 5)
        );

        // Setup ExcelExporter
        ExcelExporter exporter = new ExcelExporter("By Column Names");
        exporter.addColumn(new ColumnDescriptor("Name", "name"));
        exporter.addColumn(new ColumnDescriptor("Price", "price"));
        exporter.addColumn(new ColumnDescriptor("Quantity", "quantity"));
        
        // Add rows
        for (Product product : products) {
            exporter.createRow(product);
        }
        
        // Create config with label in Name column
        SummaryRowConfig config = SummaryRowConfig.builder()
                .summaryLabel("Grand Total:")
                .labelColumnName("Name")  // Put label in the Name column
                .build();
        
        // Add summary row by column names with config
        List<String> columnNamesToSum = Arrays.asList("Price", "Quantity");
        int summaryRowIndex = exporter.addSummaryRowByColumnNames(columnNamesToSum, config);
        
        // Save to temp file
        File outputFile = new File(tempDir, "by_column_names.xlsx");
        exporter.save(outputFile);
        
        // Verify summary row
        Workbook workbook = WorkbookFactory.create(outputFile);
        Sheet sheet = workbook.getSheet("By Column Names");
        
        // Check if summary row exists at the correct position
        Row summaryRow = sheet.getRow(summaryRowIndex);
        assertNotNull(summaryRow, "Summary row should exist");
        
        // Get column indices
        int nameIndex = exporter.getColumnIndex("Name");
        int priceIndex = exporter.getColumnIndex("Price");
        
        // Check that the label is in the Name column
        Cell labelCell = summaryRow.getCell(nameIndex);
        assertNotNull(labelCell, "Label cell should exist in Name column");
        assertEquals("Grand Total:", labelCell.getStringCellValue(), "Label should be 'Grand Total:'");
        
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
