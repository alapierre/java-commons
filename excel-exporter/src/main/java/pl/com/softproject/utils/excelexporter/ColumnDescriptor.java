/*
 * Copyright 2011-03-30 the original author or authors.
 */

package pl.com.softproject.utils.excelexporter;

/**
 * Opisuje kolumnę w tworzonym pliku .xls
 *
 * @author {@literal al@alapierre.io}
 * $Rev: $, $LastChangedBy: $
 * $LastChangedDate: $
 */
public class ColumnDescriptor {

    private String headerName;
    private String propertyName;
    private ColumnValueFormatter columnValueFormatter;
    private Integer columns;
    private ColumnStyleDescriptor styleDescriptor;

    /**
     *
     * @param headerName - nazwa kolumny widoczna w nagłówku
     * @param propertyName - nazwa property
     */
    public ColumnDescriptor(String headerName, String propertyName) {
        this.headerName = headerName;
        this.propertyName = propertyName;
    }

    public ColumnDescriptor(String headerName, String propertyName, String excelFormatMask) {
        this.headerName = headerName;
        this.propertyName = propertyName;
        this.styleDescriptor = new ColumnStyleDescriptor(excelFormatMask, null);
    }

    public ColumnDescriptor(String headerName, String propertyName, ColumnValueFormatter formatter) {
        this.headerName = headerName;
        this.propertyName = propertyName;
        this.columnValueFormatter = formatter;
    }

    public ColumnDescriptor(final String headerName, final String propertyName,
                            final ColumnStyleType columnStyleType) {
        this.headerName = headerName;
        this.propertyName = propertyName;
        this.styleDescriptor = new ColumnStyleDescriptor(null, columnStyleType);
    }

    public ColumnDescriptor(final String headerName, final String propertyName, final ColumnStyleType columnStyleType,
                            final Integer columns) {
        this.headerName = headerName;
        this.propertyName = propertyName;
        this.columns = columns;
        this.styleDescriptor = new ColumnStyleDescriptor(null, columnStyleType);
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public ColumnValueFormatter getColumnValueFormatter() {
        return columnValueFormatter;
    }

    public void setColumnValueFormatter(ColumnValueFormatter columnValueFormatter) {
        this.columnValueFormatter = columnValueFormatter;
    }

    public Integer getColumns() {
        return columns;
    }

    public void setColumns(final Integer columns) {
        this.columns = columns;
    }

    public ColumnStyleDescriptor getStyleDescriptor() {
        return styleDescriptor;
    }

    public void setStyleDescriptor(final ColumnStyleDescriptor styleDescriptor) {
        this.styleDescriptor = styleDescriptor;
    }
}
