/*
 * Copyright 2012-07-16 the original author or authors.
 */
package pl.com.softproject.utils.pivot;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Adrian Lapierre <adrian@softproject.com.pl>
 */
public interface PivotTableModel {

    void add(String rowKey, String columnKey, Object value);

    void addRow(String rowKey, Map<String, Object> rowValue);
    
    void addColumn(String columnKey, Object value) throws Exception;
    
    void addColumn(String columnKey, Map<String, Object> rowKeyToColumnValueMap);
      
    void removeRow(String rowKey);
    
    void removeColumn(String columnKey);
    
    Iterator iterator();
    
//    String[] getColumnNames();    
//    int getRowCount();
//    int getColumnCount();
//    Class getColumnClass(int columnIndex);
//    Object getValueAt(int rowIndex, int columnIndex);
//    void setValueAt(Object aValue, int rowIndex, int columnIndex);

    List<String> getRowNames();

    Set<String> getColumnNames();

    Object get(String rowKey, String columnKey);
    
}
