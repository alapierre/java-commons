/*
 * Copyright 2011-03-30 the original author or authors.
 */

package pl.com.softproject.utils.excelexporter;

import jodd.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serializuje obiekty do pliku .xls
 *
 * @author {@literal al@alapierre.io}
 */
@SuppressWarnings("unused")
@Slf4j
public class ExcelExporter {

    private static final String SUM_FORMULA_PATTERN = "SUM(%s%d:%s%d)";
    
    protected List<ColumnDescriptor> columns = new LinkedList<>();
    protected List<ColumnDescriptor> additionalColumns = new LinkedList<>();
    protected OutputStream out;
    protected String sheetName;
    protected Sheet sheet;
    protected Workbook wb;
    protected Row header;
    protected int currentRowNumber;
    protected int currentColumnNumber;

    protected DataFormat format;
    protected CellStyle styleMoney;
    protected CellStyle styleDate;
    protected CellStyle styleHeader;
    protected CellStyle styleDefault;
    protected CellStyle styleDateNoTime;

    private final Map<ColumnStyleDescriptor, CellStyle> styles = new HashMap<>();

    public ExcelExporter(String sheetName) {
        this.sheetName = sheetName;
        init();
    }

    /**
     * Tworzy kolumnę w wynikowym arkuszu. Do arkusza trafią wyłącznie
     * te właściwości obiektu, które wcześniej zostaną dodane
     * przy pomocy tej metody
     *
     * @param columnDescriptor parametry kolumny do dodania
     * @return this
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
     * Dodaje wiersz do arkusza pobierając refleksją dane z dostarczonego beana
     *
     * @param bean JavaBean z danymi do wstawienia w wierszu
     */
    public void createRow(Object bean) {

        if(currentRowNumber == 0)
            createHeaderRow();

        Row row = sheet.createRow(currentRowNumber);
        for(ColumnDescriptor column : columns) {
            createCell(row, bean, column);
        }
        currentColumnNumber = 0;
        currentRowNumber++;
    }

    /**
     * Wariant metody createRow() pozwalający na utworzenie headerów w danym wierszu - przydatna w połączeniu z
     * goToNextRow() w przypadku ręcznego tworzenia danych przed masowym użyciem niniejszej metody
     *
     * @param bean                      JavaBean z danymi do wstawienia w wierszu
     * @param columnDescriptorHeaderRow Wiersz w którym mają utworzyć się nagłówki naszych kolumn
     */
    public void createRow(Object bean, int columnDescriptorHeaderRow) {
        if (currentRowNumber == columnDescriptorHeaderRow)
            createHeaderRow();

        Row row = sheet.createRow(currentRowNumber);
        for (ColumnDescriptor column : columns) {
            createCell(row, bean, column);
        }
        currentColumnNumber = 0;
        currentRowNumber++;
    }

    public void createRow(Object bean, List<Object> additionalBeans) {
        createRow(bean, additionalBeans, this.additionalColumns);
    }

    public void createRow(Object bean, List<Object> additionalBeans, List<ColumnDescriptor> additionalColumns) {

        if(currentRowNumber == 0) {
            createHeaderRowWithAdditional(additionalColumns);
        }

        Row row = sheet.createRow(currentRowNumber);
        for(ColumnDescriptor column : columns) {
            createCell(row, bean, column);
        }
        for(ColumnDescriptor column : additionalColumns) {
            if(additionalBeans != null && additionalColumns.indexOf(column) < additionalBeans.size())
                createCell(row, additionalBeans.get(additionalColumns.indexOf(column)), column);
        }
        currentColumnNumber = 0;
        currentRowNumber++;
    }

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
     * @param outputFile plik, do jakiego należy zapisać dane
     * @throws FileNotFoundException — jeśli ścieżka do pliku nie istnieje
     * @throws IOException inne błędy IO
     */
    public void save(File outputFile) throws IOException {
        OutputStream os = Files.newOutputStream(outputFile.toPath());
        wb.write(os);
        os.close();
    }

    /**
     * Zapisuje utworzony arkusz do OutputStream
     *
     * @param os need to closed
     * @throws FileNotFoundException ścieżka do pliku nie istnieje
     * @throws IOException inne błędy IO
     */
    public void save(OutputStream os) throws IOException {
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
        font.setBold(true);
        styleHeader = wb.createCellStyle();
        styleHeader.setAlignment(HorizontalAlignment.CENTER);
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
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
            }
            if (columnStyleDescriptor.getType().equals(ColumnStyleType.SUCCESS)) {
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREEN.getIndex());
            }
            if (columnStyleDescriptor.getType().equals(ColumnStyleType.WARNING)) {
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.YELLOW.getIndex());
            }
            if (columnStyleDescriptor.getType().equals(ColumnStyleType.BLUE)) {
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_BLUE.getIndex());
            }
        }

        styles.put(columnStyleDescriptor, cellStyle);
        return cellStyle;
    }

    protected CellStyle cellStyleFromColumnDescriptor(ColumnStyleDescriptor columnStyleDescriptor) {
        return createCellStyle(columnStyleDescriptor);
    }

    /**
     * Zwraca domyślny styl lub styl zdefiniowany w ColumnDescriptor
     *
     * @param defaultCellStyle - domyślny styl dla tej kolumny
     * @return CellStyle domyślny lub utworzony z ColumnDescriptor
     */
    protected CellStyle determinateCellStyle(ColumnStyleDescriptor columnStyleDescriptor, CellStyle defaultCellStyle) {

        if(columnStyleDescriptor == null)
            return defaultCellStyle;

        if (columnStyleDescriptor.getExcelFormatMask() != null || columnStyleDescriptor.getType() != null) {
            return cellStyleFromColumnDescriptor(columnStyleDescriptor);
        }

        return defaultCellStyle;
    }

    protected void createExcelSheet() {
        wb = new XSSFWorkbook();
        sheet = wb.createSheet(sheetName);
    }

    public void addSheet(String name) {
        sheetName = name;
        sheet = wb.createSheet(name);
        currentColumnNumber = 0;
        currentRowNumber = 0;
        clearColumns();
    }

    public Workbook getWorkbook() {
        return wb;
    }

    protected void createHeaderRow() {
        createRowAndCells();
        currentColumnNumber = 0;
        currentRowNumber++;
    }

    protected void createHeaderRowWithAdditional() {
        createHeaderRowWithAdditional(additionalColumns);
    }

    protected void createHeaderRowWithAdditional(List<ColumnDescriptor> additionalColumns) {

        createRowAndCells();
        createCells(additionalColumns);

        currentColumnNumber = 0;
        currentRowNumber++;
    }

    private void createRowAndCells() {
        header = sheet.createRow(currentRowNumber);
        createCells(columns);
    }

    private void createCells(List<ColumnDescriptor> columns) {
        for(ColumnDescriptor column : columns) {
            Cell cell = header.createCell(currentColumnNumber++);
            cell.setCellValue(column.getHeaderName());
            cell.setCellStyle(styleHeader);
            if(column.getColumns() != null && column.getColumns() > 1){
                sheet.addMergedRegion(new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), cell.getColumnIndex(), cell.getColumnIndex() + column.getColumns() - 1));
            }
        }
    }

    protected void createCell(Row row, Object bean, ColumnDescriptor columnDescriptor) {

        if(columnDescriptor.getPropertyName() == null) {
            currentColumnNumber++;
            return;
        }

        Object property;

        if(columnDescriptor instanceof EnumeratedColumnDescription) {
            property = ((EnumeratedColumnDescription<?>)columnDescriptor).getValue(currentRowNumber, bean);
        } else if(columnDescriptor instanceof ConcatationColumnDescriptor) {
            property = getMultiProperty(bean, (ConcatationColumnDescriptor)columnDescriptor);
        }
        else
            property = getProperty(bean, columnDescriptor);

        if(property == null) {
            currentColumnNumber++;
            return;
        }

        Cell cell;
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
            cell.setCellValue(Date.from(localData.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDateNoTime));
        }
        else if(property instanceof LocalDateTime) {
            cell = row.createCell(currentColumnNumber++);
            LocalDateTime localDataTime = (LocalDateTime) property;
            cell.setCellValue(Date.from(localDataTime.atZone(ZoneId.systemDefault()).toInstant()));
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
        } else if(property instanceof BigDecimal) {
            cell = row.createCell(currentColumnNumber++);
            cell.setCellValue(((BigDecimal)property).doubleValue());
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDefault));
        } else {
            cell = row.createCell(currentColumnNumber++);
            cell.setCellValue(property.toString().trim());
            cell.setCellStyle(determinateCellStyle(columnDescriptor.getStyleDescriptor(), styleDefault));
        }
    }

    protected Object getProperty(Object bean, ColumnDescriptor columnDescriptor) {
        return BeanUtil.silent.getProperty(bean, columnDescriptor.getPropertyName());
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

        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(Font.COLOR_RED);
        style.setFont(font);
        sheet.getRow(row).getCell(cell).setCellStyle(style);
    }

    public void setCellStyleBold(int row, int cell) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        sheet.getRow(row).getCell(cell).setCellStyle(style);
    }

    public void setCellStyleColor(int row, int cell, int r, int g, int b) {
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(r,g,b),new DefaultIndexedColorMap()));
        sheet.getRow(row).getCell(cell).setCellStyle(cellStyle);
    }

    public void setCellStyle(int row, int cell, CellStyle cellStyle) {
        sheet.getRow(row).getCell(cell).setCellStyle(cellStyle);
    }

    public CellStyle createCellStyleColor(int r, int g, int b){
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(r,g,b),new DefaultIndexedColorMap()));
        return cellStyle;
    }

    public void goToNextRow() {
        this.currentRowNumber++;
    }

    /**
     * Adds a summary row at the bottom of the sheet with SUM formulas for specified columns.
     *
     * @param columnsToSum List of column indices to sum (0-based)
     * @param config Configuration object for summary row options
     * @return Row index where the summary was added
     */
    public int addSummaryRow(@NotNull List<Integer> columnsToSum, @NotNull SummaryRowConfig config) {
        Row summaryRow = sheet.createRow(currentRowNumber);
        
        addSummaryLabel(summaryRow, config.getSummaryLabel(), config.getLabelColumnIndex(), config.isBoldText());
        
        addSumFormulas(summaryRow, columnsToSum, config.getSkipFirstRows(), config.isBoldText());
        
        currentRowNumber++;
        
        return currentRowNumber - 1;
    }
    
    /**
     * Adds a summary row at the bottom of the sheet with SUM formulas for specified columns by their names.
     *
     * @param columnNamesToSum List of column names to sum
     * @param config Configuration object for summary row options
     * @return Row index where the summary was added
     */
    public int addSummaryRowByColumnNames(@NotNull List<String> columnNamesToSum, @NotNull SummaryRowConfig config) {
        List<Integer> columnIndices = mapColumnNamesToIndices(columnNamesToSum);
        return addSummaryRow(columnIndices, config);
    }

    
    /**
     * Adds the summary label to the specified cell of the summary row with appropriate styling.
     * 
     * @param summaryRow Row where to add the label
     * @param summaryLabel Text for the label (or default if null)
     * @param labelColumnIndex Column index where to put the label
     * @param boldText Whether to make the text bold
     */
    private void addSummaryLabel(Row summaryRow, String summaryLabel, int labelColumnIndex, boolean boldText) {
        Cell labelCell = summaryRow.createCell(labelColumnIndex);
        labelCell.setCellValue(summaryLabel);
        
        if (boldText) {
            CellStyle boldStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            boldStyle.setFont(font);
            labelCell.setCellStyle(boldStyle);
        }
    }
    
    /**
     * Adds SUM formulas to the specified columns in the summary row.
     * 
     * @param summaryRow Row where to add the formulas
     * @param columnsToSum Column indices to add formulas for
     * @param skipFirstRows Number of rows to skip from the beginning
     * @param boldText Whether to make the text bold
     */
    private void addSumFormulas(Row summaryRow, List<Integer> columnsToSum, int skipFirstRows, boolean boldText) {
        for (Integer colIndex : columnsToSum) {
            if (colIndex < 0 || colIndex >= columns.size()) {
                continue;
            }
            
            Cell sumCell = summaryRow.createCell(colIndex);
            
            int startRow = skipFirstRows + 1;
            
            String colLetter = CellReference.convertNumToColString(colIndex);
            
            String formula = String.format(SUM_FORMULA_PATTERN, colLetter, startRow, colLetter, currentRowNumber);
            sumCell.setCellFormula(formula);
            
            ColumnDescriptor column = columns.get(colIndex);
            CellStyle cellStyle = determinateCellStyle(column.getStyleDescriptor(), styleMoney);
            
            if (boldText) {
                CellStyle boldStyle = wb.createCellStyle();
                boldStyle.cloneStyleFrom(cellStyle);
                
                Font font = wb.createFont();
                font.setBold(true);
                boldStyle.setFont(font);
                
                sumCell.setCellStyle(boldStyle);
            } else {
                sumCell.setCellStyle(cellStyle);
            }
        }
    }

    private @NotNull List<Integer> mapColumnNamesToIndices(List<String> columnNamesToSum) {
        return columnNamesToSum.stream()
            .map(this::getColumnIndex)
            .filter(index -> index >= 0)
            .collect(Collectors.toList());
    }

    /**
     * Adds a summary row with default settings for specified columns.
     * This is a simple convenience method that doesn't require a config object.
     *
     * @param columnsToSum List of column indices to sum (0-based)
     * @return Row index where the summary was added
     */
    public int addSummaryRow(@NotNull List<Integer> columnsToSum) {
        return addSummaryRow(columnsToSum, SummaryRowConfig.builder().build());
    }
    
    /**
     * Adds a summary row with default settings for specified columns by their names.
     * This is a simple convenience method that doesn't require a config object.
     *
     * @param columnNamesToSum List of column names to sum
     * @return Row index where the summary was added
     */
    public int addSummaryRowByColumnNames(@NotNull List<String> columnNamesToSum) {
        return addSummaryRowByColumnNames(columnNamesToSum, SummaryRowConfig.builder().build());
    }

}
