package pl.com.softproject.utils.excelexporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.13
 */
class ExcelExporterTest {

    @Test
    void test() throws IOException {
        val r = givenValidReportRow();
        List<ReportRow> rows = generateListOfRows(r, 1);

        try (ExcelExporter excelExporter = new ExcelExporter("report")) {
            excelExporter.addColumn(new ColumnDescriptor("Name", "name"));
            excelExporter.addColumn(new ColumnDescriptor("Last Name", "lastName"));
            excelExporter.addColumn(new ColumnDescriptor("Address", "address[1]"));

            rows.forEach(excelExporter::createRow);

            excelExporter.save(new File("/tmp/sample_excel.xlsx"));
        }
    }

    @Test
    void shouldFlushOldRowsToDiskWhenOverWindowSize() throws IOException {
        val r = givenValidReportRow();
        int rowsCount = 15;
        int windowSize = 10;
        String sheetName = "report";

        List<ReportRow> rows = generateListOfRows(r, rowsCount);

        try (ExcelExporter excelExporter = new ExcelExporter(sheetName, windowSize)) {
            excelExporter.addColumn(new ColumnDescriptor("Name", "name"));
            excelExporter.addColumn(new ColumnDescriptor("Last Name", "lastName"));
            excelExporter.addColumn(new ColumnDescriptor("Address", "address[1]"));

            rows.forEach(excelExporter::createRow);

            assertNull(
                excelExporter.getWorkbook().getSheet(sheetName).getRow(0),
                "First row should be flushed from memory"
            );
            assertNotNull(
                excelExporter.getWorkbook().getSheet("report").getRow(rowsCount - 1),
                "Last row should still be in memory"
            );
        }
    }

    @Test
    void shouldAutoSizeColumnsWhenAutoSizingPrepared() throws IOException {
        val r = givenValidReportRow();
        List<ReportRow> rows = generateListOfRows(r, 1);

        try (ExcelExporter excelExporter = new ExcelExporter("report")) {
            excelExporter.prepareAutoSizing();
            excelExporter.addColumn(new ColumnDescriptor("Name", "name"));
            excelExporter.addColumn(new ColumnDescriptor("Last Name", "lastName"));
            excelExporter.addColumn(new ColumnDescriptor("Address", "address[1]"));

            rows.forEach(excelExporter::createRow);
            excelExporter.autoSizeAllColumns();
            excelExporter.save(new File("/tmp/sample_excel.xlsx"));
        }
    }

    @Test
    void shouldThrowWhenAutoSizingColumnsWithoutPrepareCall() throws IOException {
        val r = givenValidReportRow();
        List<ReportRow> rows = generateListOfRows(r, 1);

        try (ExcelExporter excelExporter = new ExcelExporter("report")) {
            excelExporter.addColumn(new ColumnDescriptor("Name", "name"));
            excelExporter.addColumn(new ColumnDescriptor("Last Name", "lastName"));
            excelExporter.addColumn(new ColumnDescriptor("Address", "address[1]"));

            rows.forEach(excelExporter::createRow);
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                excelExporter::autoSizeAllColumns
            );
            assertEquals("autoSizeAllColumns() requires prepareAutoSizing() to be called first", ex.getMessage());
        }
    }

    private static ReportRow givenValidReportRow() {
        return ReportRow.builder()
            .name("Jan")
            .lastName("Kowalski")
            .address(List.of("Warszawska", "Marszałkowska"))
            .build();
    }

    private static @NotNull List<ReportRow> generateListOfRows(ReportRow r, int size) {
        List<ReportRow> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(
                r.toBuilder()
                    .lastName(r.getLastName() + " " + i)
                    .build()
            );
        }
        return result;
    }

    @Data
    @Builder(toBuilder = true)
    public static class ReportRow {

        private String name;
        private String lastName;
        private List<String> address;
    }
}
