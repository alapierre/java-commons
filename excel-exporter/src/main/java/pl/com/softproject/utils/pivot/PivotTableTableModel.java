/*
 * Copyright 2013-03-07 the original author or authors.
 */
package pl.com.softproject.utils.pivot;

import lombok.extern.slf4j.Slf4j;
import pl.com.softproject.utils.pivot.PivotTableModelImpl.RowIterator;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 *
 * @author Adrian Lapierre <alapierre@softproject.com.pl>
 */
@Slf4j
public class PivotTableTableModel<T> extends AbstractTableModel {

    protected PivotTableModel pivot = new PivotTableModelImpl();

    protected Object[][] dataTab;
    protected String columnNames[];
    protected String rowNames[];
    protected int columnCount;
    protected int rowCount;

    public void add(String rowKey, String columnKey, T value) {
        pivot.add(rowKey, columnKey, value);
    }

    public void addRow(String rowKey, Map<String, Object> rowValue) {
        pivot.addRow(rowKey, rowValue);
    }

    /**Metoda do wypełniania całej kolumny jedną wartością**/
    public void addColumn(String columnKey, Object columnValue) throws Exception {
        pivot.addColumn(columnKey, columnValue);
    }

    public void addColumn(String columnKey, Map<String, Object> rowKeyToColumnValueMap) {
        pivot.addColumn(columnKey, rowKeyToColumnValueMap);
    }

    public void removeColumn(String columnKey) {
        pivot.removeColumn(columnKey);
    }

    public boolean containsColumnName(String columnName) {
        if (columnNames != null) {
            List<String> list = Arrays.asList(columnNames);
            if (list.contains(columnName))
                return true;
        }
        return false;
    }

    public void removeRow(String rowKey) {
        pivot.removeRow(rowKey);
    }

    public void prepareForTabe() {

        Set<String> columns = pivot.getColumnNames();

        columnNames = columns.toArray(new String[0]);

        columnCount = columnNames.length + 1;

        rowNames = pivot.getRowNames().toArray(new String[0]);
        rowCount = rowNames.length;

        dataTab = new Object[rowCount][];

        PivotTableModelImpl.RowIterator rows = (PivotTableModelImpl.RowIterator) pivot.iterator();

        int rownum = 0;

        while (rows.hasNext()) {
            Map<String, Object> pivotRow = rows.next();

            List<Object> tmp = new LinkedList<Object>(pivotRow.values());
            tmp.add(0, rowNames[rownum]);
            dataTab[rownum++] = tmp.toArray();
        }
        rowCount = rownum;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public T getValueAt(int rowIndex, int columnIndex) {

        if(columnIndex < dataTab[rowIndex].length) {

            return (T)dataTab[rowIndex][columnIndex];
        } else return null;
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Klucz" : columnNames[column - 1];
    }

    public String getRowName(int row) {
        return rowNames[row];
    }

    @Override
    public PivotTableTableModel<T> clone() {

        PivotTableTableModel<T> pivotTableTableModel = new PivotTableTableModel<T>();

        RowIterator iter = (RowIterator)pivot.iterator();
        while (iter.hasNext()) {

            Map<String, Object> rowValue = iter.next();
            String rowKey = iter.rowKey();
            pivotTableTableModel.addRow(rowKey, rowValue);
        }
        pivotTableTableModel.prepareForTabe();

        return pivotTableTableModel;
    }
}
