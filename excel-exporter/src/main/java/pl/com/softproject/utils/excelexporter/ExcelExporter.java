/*
 * Copyright 2011-03-30 the original author or authors.
 */

package pl.com.softproject.utils.excelexporter;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Serializuje obiekty do pliku .xls
 *
 * @author <a href="mailto:alapierre@soft-project.pl">Adrian Lapierre</a>
 * $Rev: $, $LastChangedBy: $
 * $LastChangedDate: $
 */
@Slf4j
public class ExcelExporter {

    protected List<ColumnDescriptor> columns = new LinkedList<ColumnDescriptor>();
    protected List<ColumnDescriptor> additionalColumns = new LinkedList<ColumnDescriptor>();
    protected OutputStream out;
    protected String sheetName;
    protected HSSFSheet sheet;
    protected HSSFWorkbook wb;
    protected HSSFRow header;
    protected short currentRowNumber;
    protected int currentColumnNumber;

    protected DataFormat format;
    protected CellStyle styleMoney;
    protected CellStyle styleDate;
    protected CellStyle styleHeader;
    protected CellStyle styleDefault;
    protected CellStyle styleDateNoTime;

    private Map<ColumnStyleDescriptor, CellStyle> styles = new HashMap<>();

    public ExcelExporter(String sheetName) {        
        this.sheetName = sheetName;        
        init();        
    }

    /**
     * Tworzy kolumnę w wynikowym arkuszu. Do arkusza trafią wyłącznie
     * te właściwości serializowanego obiektu, które wcześniej zostaną dodane
     * przy pomocy tej metody
     *
     * @param columnDescriptor
     * @return
     */
    public ExcelExporter addColumn(ColumnDescriptor columnDescriptor) {
        columns.add(columnDescriptor);
        return this;
    }

    public ExcelExporter addAdditionalColumn(ColumnDescriptor columnDescriptor) {
        additionalColumns.add(columnDescriptor);
        return this;
    }
    
    /**
     * Usuwa zdefiniowane kolumny dla arkusza
     */
    public void clearColumns() {
        columns.clear();
    }
    
    /**
     * Dodaje wiersz do akrusza pobierając refleksją dane z dostarczonego beana
     *
     * @param bean
     */
    public void createRow(Object bean) {

        if(currentRowNumber == 0)
            createHeaderRow();

        HSSFRow row = sheet.createRow(currentRowNumber);
        for(ColumnDescriptor column : columns) {
            createCell(row, bean, column);
        }
        currentColumnNumber = 0;
        currentRowNumber++;
    }

    public void createRow(Object bean, List<Object> additionalBeans) {

        if(currentRowNumber == 0) {
            createHeaderRowWithAdditional();
        }

        HSSFRow row = sheet.createRow(currentRowNumber);
        for(ColumnDescriptor column : columns) {
            createCell(row, bean, column);
        }
        for(ColumnDescriptor column : additionalColumns) {
            if(additionalBeans != null && !(additionalColumns.indexOf(column) >= additionalBeans.size()))
                createCell(row, additionalBeans.get(additionalColumns.indexOf(column)), column);
        }
        currentColumnNumber = 0;
        currentRowNumber++;
    }

    public void createRow(Object bean, List<Object> additionalBeans, List<ColumnDescriptor> additionalColumns) {

        if(currentRowNumber == 0) {
            createHeaderRowWithAdditional(additionalColumns);
        }

        HSSFRow row = sheet.createRow(currentRowNumber);
        for(ColumnDescriptor column : columns) {
            createCell(row, bean, column);
        }
        for(ColumnDescriptor column : additionalColumns) {
            if(additionalBeans != null && !(additionalColumns.indexOf(column) >= additionalBeans.size()))
                createCell(row, additionalBeans.get(additionalColumns.indexOf(column)), column);
        }
        currentColumnNumber = 0;
        currentRowNumber++;
    }

//    public void createOtherRow(Object bean, int colNumber, List<ColumnDescriptor> columns) {
//        
//        currentColumnNumber = colNumber;
//        HSSFRow row = sheet.createRow(currentRowNumber);
//        for(ColumnDescriptor column : columns) {
//            createCell(row, bean, column);
//        }
//        currentColumnNumber = 0;
//        currentRowNumber++;
//    }

    public int getColumnIndex(String headerName) {
        
        int result = 0;
        boolean finded = false;
        
        for(ColumnDescriptor column : columns) {
            if(headerName.equals(column.getHeaderName())) {
                finded=true;
                break;
            }
            result++;
        }
        
        return finded ? result : -1;
    }
    
    /**
     * Zapisuje utworzony arkusz do pliku
     *
     * @param outputFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void save(File outputFile) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(outputFile);
        wb.write(os);
        os.close();
    }

    /**
     * Zapisuje utworzony arkusz do OutputStream
     *
     * @param os need to by closed
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void save(OutputStream os) throws FileNotFoundException, IOException {
        wb.write(os);
    }

    /**
     * Ustawia dla wszystkich wypełnionych kolumn szerokość na auto-size
     */
    public void autoSizeAllColumns() {
        if(header!=null) {
            for (Cell col : header) {
                sheet.autoSizeColumn(col.getColumnIndex(), true);
            }
        }
    }

    private void init() {
        createExcelSheet();
        format = wb.createDataFormat();
        initDefaultCellStyles();
        initHeaderStyle();
    }

    protected void initDefaultCellStyles() {        
        styleMoney = createCellStyle(new ColumnStyleDescriptor("#,##0.00", ColumnStyleType.DEFAULT));
        styleDate = createCellStyle(new ColumnStyleDescriptor("yyyy-mm-dd hh:mm", ColumnStyleType.DEFAULT));
        styleDateNoTime = createCellStyle(new ColumnStyleDescriptor("yyyy-mm-dd", ColumnStyleType.DEFAULT));
        styleDefault = wb.createCellStyle();
    }

    protected void initHeaderStyle() {
        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        styleHeader = wb.createCellStyle();
        styleHeader.setAlignment(CellStyle.ALIGN_CENTER);
        styleHeader.setFont(font);
    }

    protected CellStyle createCellStyle(ColumnStyleDescriptor columnStyleDescriptor) {

        CellStyle cellStyle;
        if(styles.containsKey(columnStyleDescriptor)){
            cellStyle = styles.get(columnStyleDescriptor);
            return cellStyle;
        } else {
            cellStyle = wb.createCellStyle();
        }

        if(columnStyleDescriptor.getExcelFormatMask() != null)
            cellStyle.setDataFormat(format.getFormat(columnStyleDescriptor.getExcelFormatMask()));
        if(columnStyleDescriptor.getType() != null) {
            if (columnStyleDescriptor.getType().equals(ColumnStyleType.ERROR)) {
                cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                cellStyle.setFillForegroundColor(HSSFColor.RED.index);
            }
            if (columnStyleDescriptor.getType().equals(ColumnStyleType.SUCCESS)) {
                cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                cellStyle.setFillForegroundColor(HSSFColor.GREEN.index);
            }
            if (columnStyleDescriptor.getType().equals(ColumnStyleType.WARNING)) {
                cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                cellStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
            }
            if (columnStyleDescriptor.getType().equals(ColumnStyleType.BLUE)) {
                cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                cellStyle.setFillForegroundColor(HSSFColor.LIGHT_BLUE.index);
            }
        }

        styles.put(columnStyleDescriptor, cellStyle);
        return cellStyle;
    }

    protected CellStyle cellStyleFromColumnlDescriptor(ColumnStyleDescriptor columnStyleDescriptor) {
        return createCellStyle(columnStyleDescriptor);
    }

    /**
     * Zwraca domyślny styl lub styl zdefiniowany w ColumnDescriptor
     *
     * @param defaultCellStyle - domyślny styl dla tej kolumny
     * @return
     */
    protected CellStyle determinateCellStyle(ColumnStyleDescriptor columnStyleDescriptor, CellStyle defaultCellStyle) {

        if(columnStyleDescriptor == null)
            return defaultCellStyle;

        if (columnStyleDescriptor.getExcelFormatMask() != null || columnStyleDescriptor.getType() != null) {
            return cellStyleFromColumnlDescriptor(columnStyleDescriptor);
        }

        return defaultCellStyle;
    }

    protected void createExcelSheet() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet(sheetName);
    }
    
    public void addSheet(String name) {
        sheetName = name;
        sheet = wb.createSheet(name);
        currentColumnNumber = 0;
        currentRowNumber = 0;
    }
    
    public Workbook getWorkbook() {
        return wb;
    }
    
    protected void createHeaderRow() {
        header = sheet.createRow(currentRowNumber);
        for(ColumnDescriptor column : columns) {
            Cell cell = header.createCell(currentColumnNumber++);
            cell.setCellValue(column.getHeaderName());
            cell.setCellStyle(styleHeader);
            if(column.getColumns() != null && column.getColumns() > 1){
                sheet.addMergedRegion(new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), cell.getColumnIndex(), cell.getColumnIndex() + column.getColumns() - 1));
            }
        }
        currentColumnNumber = 0;
        currentRowNumber++;
    }

    protected void createHeaderRowWithAdditional() {
        header = sheet.createRow(currentRowNumber);

        for(ColumnDescriptor column : columns) {
            Cell cell = header.createCell(currentColumnNumber++);
            cell.setCellValue(column.getHeaderName());
            cell.setCellStyle(styleHeader);
            if(column.getColumns() != null && column.getColumns() > 1){
                sheet.addMergedRegion(new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), cell.getColumnIndex(), cell.getColumnIndex() + column.getColumns() - 1));
            }
        }

        for(ColumnDescriptor column : additionalColumns) {
            Cell cell = header.createCell(currentColumnNumber++);
            cell.setCellValue(column.getHeaderName());
            cell.setCellStyle(styleHeader);
            if(column.getColumns() != null && column.getColumns() > 1){
                sheet.addMergedRegion(new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), cell.getColumnIndex(), cell.getColumnIndex() + column.getColumns() - 1));
            }
        }

        currentColumnNumber = 0;
        currentRowNumber++;
    }

    protected void createHeaderRowWithAdditional(List<ColumnDescriptor> additionalColumns) {
        header = sheet.createRow(currentRowNumber);

        for(ColumnDescriptor column : columns) {
            Cell cell = header.createCell(currentColumnNumber++);
            cell.setCellValue(column.getHeaderName());
            cell.setCellStyle(styleHeader);
            if(column.getColumns() != null && column.getColumns() > 1){
                sheet.addMergedRegion(new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), cell.getColumnIndex(), cell.getColumnIndex() + column.getColumns() - 1));
            }
        }

        for(ColumnDescriptor column : additionalColumns) {
            Cell cell = header.createCell(currentColumnNumber++);
            cell.setCellValue(column.getHeaderName());
            cell.setCellStyle(styleHeader);
            if(column.getColumns() != null && column.getColumns() > 1){
                sheet.addMergedRegion(new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), cell.getColumnIndex(), cell.getColumnIndex() + column.getColumns() - 1));
            }
        }

        currentColumnNumber = 0;
        currentRowNumber++;
    }


    protected void createCell(HSSFRow row, Object bean, ColumnDescriptor columnDescriptor) {

        if(columnDescriptor.getPropertyName() == null) {
            currentColumnNumber++;
            return;
        }

        Object property = null;

        if(columnDescriptor instanceof EnumeratedColumnDescription) {
            property = ((EnumeratedColumnDescription)columnDescriptor).getValue(currentRowNumber, bean);
        } else if(columnDescriptor instanceof ConcatationColumnDescriptor) {
            property = getMultiProperty(bean, (ConcatationColumnDescriptor)columnDescriptor);
        }
        else
            property = getProperty(bean, columnDescriptor);

        if(property == null) {
            currentColumnNumber++;
            return;
        }

        HSSFCell cell = null;
        if(columnDescriptor.getColumnValueFormatter() != null) {
            cell = row.createCell(currentColumnNumber++);
            cell.setCellValue(columnDescriptor.getColumnValueFormatter().format(property));
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDefault));
        } else if(property instanceof Date) {
            cell = row.createCell(currentColumnNumber++);
            cell.setCellValue((Date)property);
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDate));
        }
        else if(property instanceof LocalDate) {
            cell = row.createCell(currentColumnNumber++);
            LocalDate localData = (LocalDate) property;
            cell.setCellValue(localData != null ? Date.from(localData.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDateNoTime));
        }
        else if(property instanceof LocalDateTime) {
            cell = row.createCell(currentColumnNumber++);
            LocalDateTime localDataTime = (LocalDateTime) property;
            cell.setCellValue(localDataTime != null ? Date.from(localDataTime.atZone(ZoneId.systemDefault()).toInstant()) : null);
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDate));
        }
        else if(property instanceof String) {
            cell = row.createCell(currentColumnNumber++);
            cell.setCellValue((String)property);
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDefault));
        } else if(property instanceof Integer) {
            cell = row.createCell(currentColumnNumber++);
            cell.setCellValue((Integer)property);
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDefault));
        } else if(property instanceof Long) {
            cell = row.createCell(currentColumnNumber++);
            cell.setCellValue((Long)property);
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDefault));
        } else if(property instanceof Double) {
            cell = row.createCell(currentColumnNumber++);
            cell.setCellValue((Double)property);
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDefault));
        } else {
            cell = row.createCell(currentColumnNumber++);
            cell.setCellValue(property.toString().trim());
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDefault));
        }
    }

    protected Object getProperty(Object bean, ColumnDescriptor columnDescriptor) {
        try {
            return PropertyUtils.getNestedProperty(bean, columnDescriptor.getPropertyName());
        } catch (IllegalAccessException ex) {
            throw new PropertyAccessException(ex);
        } catch (InvocationTargetException ex) {
            throw new PropertyAccessException(ex);
        } catch (NoSuchMethodException ex) {
            throw new PropertyAccessException(ex);
        } catch (NestedNullException ignore) {
            if(log.isDebugEnabled())
                log.debug(ignore.getMessage());
            return null;
        } catch (IndexOutOfBoundsException ignore) {
            if(log.isDebugEnabled())
                log.debug("for property " + columnDescriptor.getPropertyName() + " " + ignore.getMessage());
            return null;
        }
    }

    public boolean isEmpty() {
        return currentRowNumber == 0;
    }

    private Object getMultiProperty(Object bean, ConcatationColumnDescriptor columnDescriptor) {

        StringBuilder sb = new StringBuilder();

        Object prop = getProperty(bean, columnDescriptor);
        if(prop != null)
            sb.append(prop);

        for(ColumnDescriptor cd : columnDescriptor.getColumnDescriptors()) {
            
            if(getProperty(bean, cd) != null) {
                sb.append(' ');
                sb.append(getProperty(bean, cd));
            }
        }

        return sb.toString();
    }

    public void addMergedRegion(int rowFrom, int rowTo, int colFrom, int colTo) {
        sheet.addMergedRegion(new CellRangeAddress(rowFrom,rowTo,colFrom,colTo));
    }

    public void setCellStyleRedBold(int row, int cell) {

        HSSFCellStyle my_style = wb.createCellStyle();
        HSSFFont my_font = wb.createFont();
        my_font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        my_font.setColor(HSSFFont.COLOR_RED);
        my_style.setFont(my_font);
        sheet.getRow(row).getCell(cell).setCellStyle(my_style);
    }

    public void setCellStyleColor(int row, int cell, int r, int g, int b) {
        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        HSSFPalette palette = wb.getCustomPalette();
        HSSFColor color = palette.findSimilarColor(r, g, b);
        cellStyle.setFillForegroundColor(color.getIndex());
        sheet.getRow(row).getCell(cell).setCellStyle(cellStyle);
    }

    public void setCellStyle(int row, int cell, HSSFCellStyle cellStyle) {
        sheet.getRow(row).getCell(cell).setCellStyle(cellStyle);
    }

    public HSSFCellStyle createCellStyleColor(int r, int g, int b){
        HSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        HSSFPalette palette = wb.getCustomPalette();
        HSSFColor color = palette.findSimilarColor(r, g, b);
        cellStyle.setFillForegroundColor(color.getIndex());
        return cellStyle;
    }

}
