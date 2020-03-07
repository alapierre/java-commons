/*
 * Copyright 2011-08-31 the original author or authors.
 */

package pl.com.softproject.utils.excelexporter;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Used for custom cell format  
 * 
 * @author Adrian Lapierre <adrian@softproject.com.pl>
 */
public interface ExcelCellRenderer {
    
    /**
     * It will call for any exported cell 
     * 
     * @param cell - actual cell
     * @param value - actual cell value (will by set by exporter)
     */
    public void render(Cell cell, Object value);
    
}
