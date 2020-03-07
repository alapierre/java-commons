/*
 * Copyright 2012-12-10 the original author or authors.
 */
package pl.com.softproject.utils.pivot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import pl.com.softproject.utils.excelexporter.ExcelCellRenderer;

/**
 *
 * @author Adrian Lapierre <adrian@softproject.com.pl>
 */
public class PivotTableExcelExporter {

    private Workbook wb;
    private boolean addSumarryColumn;
    private ExcelCellRenderer renderer;

    public PivotTableExcelExporter() {
    }

    public PivotTableExcelExporter(Workbook wb) {
        this.wb = wb;
    }
    
    private void createWorkbook() {
        wb = new HSSFWorkbook();
    }

    public void export(PivotTableModel pivotTableModel, String sheetName, String[] columns) {

        if (wb == null) {
            createWorkbook();
        }

        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle cs = wb.createCellStyle();
        cs.setFont(font);
        cs.setAlignment(CellStyle.ALIGN_CENTER);

        Sheet sheet = wb.createSheet(sheetName);

        Row row ;
        Cell cell;

        Row header = addHeader(columns, sheet, cs);

        PivotTableModelImpl.RowIterator rows = (PivotTableModelImpl.RowIterator) pivotTableModel.iterator();

        short rownum = 1;

        while (rows.hasNext()) {
            row = sheet.createRow(rownum++);

            Map<String, Object> pivotRow = rows.next();
            String rowKey = rows.rowKey();
            cell = row.createCell(0);
            cell.setCellValue(rowKey);
            short cellnum = 1;
            for (String column : columns) {
                cell = row.createCell(cellnum++);                
                Object cellValue = pivotRow.get(column);
                
                if(renderer != null)
                    renderer.render(cell, cellValue);
                
                if (cellValue instanceof Number) {
                    
                    Number number = (Number) cellValue;
                    cell.setCellValue(number.doubleValue());                    
                } else if (cellValue instanceof String) {                    
                    
                    String result = (String) cellValue;
                    cell.setCellValue(result);                    
                } else {
                    
                    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        cell.setCellValue(0);
                    } else {
                        cell.setCellValue("");
                    }
                }
            }
            if(addSumarryColumn) {
                addSumarryColumn(cellnum, rownum, row);
            }

        }

        autoSizeColumn(sheet, 0);        
    }

    public void autoSizeColumn(Sheet sheet, int colIndex) {
        sheet.autoSizeColumn(colIndex);
    }

    public void autoSizeColumn(String seetName, int colIndex) {
        Sheet sheet = wb.getSheet(seetName);
        autoSizeColumn(sheet, colIndex);
    }

    public void saveWorkbook(File file) throws FileNotFoundException, IOException {
        
        if (wb == null) {
            createWorkbook();
        }
        
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();
    }

    private Row addHeader(String[] columns, Sheet sheet, CellStyle cs) {

        Row row = sheet.createRow(0);
        short cellnum = 1;

        for (String colName : columns) {
            Cell cell = row.createCell(cellnum++);
            cell.setCellValue(colName);
            cell.setCellStyle(cs);
        }
        if(addSumarryColumn) {
            Cell cell = row.createCell(cellnum++);
            cell.setCellValue("suma");
            cell.setCellStyle(cs);
        }
        
        return row;
    }

    public void setAddSumarryColumn(boolean addSumarryColumn) {
        this.addSumarryColumn = addSumarryColumn;
    }
    
    

    private void addSumarryColumn(int cellnum, int rownum, Row row) {
        Cell cell = row.createCell(cellnum);
        CellReference cellReference = new org.apache.poi.hssf.util.CellReference(rownum-1, cellnum-1, false, false);
        CellReference startCellReference = new org.apache.poi.hssf.util.CellReference(rownum-1, 1, false, false);
        
        cellReference.formatAsString();
        String formula = "sum(" + startCellReference.formatAsString() + ":" + cellReference.formatAsString() + ")";
        cell.setCellFormula(formula);
    }

    public void setRenderer(ExcelCellRenderer renderer) {
        this.renderer = renderer;
}

}
