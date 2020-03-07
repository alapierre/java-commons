package pl.com.softproject.utils.pivot.doublepivot;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import pl.com.softproject.utils.excelexporter.ExcelCellRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashSet;

/**
 * Class PivotDoubleGroupingExcelExporter
 *
 * @author Marcin Jasiński {@literal <mkjasinski@gmail.com>}
 */
public class PivotDoubleGroupingExcelExporter implements Serializable {

    private Workbook workbook;
    private ExcelCellRenderer excelCellRenderer;

    public PivotDoubleGroupingExcelExporter() {
        this.workbook = new HSSFWorkbook();
    }

    public PivotDoubleGroupingExcelExporter(final Workbook workbook) {
        this.workbook = workbook;
    }

    public void export(PivotDoubleGroupingTableModel pivotDoubleGroupingTableModel,
                       String sheetName) {
        if (workbook == null) {
            workbook = new HSSFWorkbook();
        }

        Font font = workbook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle cs = workbook.createCellStyle();
        cs.setFont(font);
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        Sheet sheet = workbook.createSheet(sheetName);

        org.apache.poi.ss.usermodel.Row row;
        org.apache.poi.ss.usermodel.Cell cell;

        PivotDoubleGroupingTableModelImpl.RowIterator rows =
                (PivotDoubleGroupingTableModelImpl.RowIterator) pivotDoubleGroupingTableModel
                        .iterator();

        short rowNumber = 0;

        row = sheet.createRow(rowNumber++);

        LinkedHashSet<String> columnNames = pivotDoubleGroupingTableModel.getColumnNames();
        int headerCell = 2;
        for (final String columnName : columnNames) {
            cell = row.createCell(headerCell++);
            cell.setCellStyle(cs);
            cell.setCellValue(columnName);
        }

        while (rows.hasNext()) {
            row = sheet.createRow(rowNumber++);

            Row oneRow = rows.next();
            cell = row.createCell(0);
            cell.setCellValue(oneRow.getName());
            cell.setCellStyle(cs);

            LinkedHashSet<SubRow> subRows = oneRow.getSubRows();
            int subRowsSize = subRows.size(), subRowCounter = 0;
            for (final SubRow subRow : subRows) {

                int i = 1;
                cell = row.createCell(i);
                String cellValue = subRow.getName();
                if (excelCellRenderer != null) {
                    excelCellRenderer.render(cell, cellValue);
                }
                cell.setCellValue(cellValue);

                LinkedHashSet<Column> columns = subRow.getColumns();
                Object cellValueObject;
                for (final Column column : columns) {
                    cell = row.createCell(++i);
                    cellValueObject = column.getValue();
                    if (cellValueObject instanceof Number) {
                        Number number = (Number) cellValueObject;
                        cell.setCellValue(number.doubleValue());
                    } else {
                        String string = (String) cellValueObject;
                        cell.setCellValue(string);
                    }
                }

                if (++subRowCounter < subRowsSize) {
                    row = sheet.createRow(rowNumber++);
                    cell = row.createCell(0);
                    cell.setCellValue(oneRow.getName());
                    cell.setCellStyle(cs);
                }
            }
        }

        rows = (PivotDoubleGroupingTableModelImpl.RowIterator) pivotDoubleGroupingTableModel
                .iterator();
        int firstRow = 1;
        while (rows.hasNext()) {
            Row nextRow = rows.next();
            int size = nextRow.getSubRows().size();

            sheet.addMergedRegion(new CellRangeAddress(firstRow, firstRow + size - 1, 0, 0));

            firstRow += size;
        }

        autoSizeColumn(sheet, 0);
    }

    public void setExcelCellRenderer(ExcelCellRenderer excelCellRenderer) {
        this.excelCellRenderer = excelCellRenderer;
    }

    public void autoSizeColumn(Sheet sheet, int colIndex) {
        sheet.autoSizeColumn(colIndex);
    }

    public void autoSizeColumn(String sheetName, int colIndex) {
        Sheet sheet = workbook.getSheet(sheetName);
        autoSizeColumn(sheet, colIndex);
    }

    public void saveWorkbook(File file) throws IOException {
        if (workbook == null) {
            workbook = new HSSFWorkbook();
        }
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
    }
}
