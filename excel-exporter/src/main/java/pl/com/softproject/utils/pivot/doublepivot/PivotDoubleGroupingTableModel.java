package pl.com.softproject.utils.pivot.doublepivot;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Class PivotDoubleGroupingTableModel
 *
 * @author Marcin Jasiński {@literal <mkjasinski@gmail.com>}
 */
public interface PivotDoubleGroupingTableModel extends Containable {

    void addRow(Row row);

    void addRows(LinkedHashSet<Row> rows);

    int getRowsCount();

    LinkedHashSet<String> getRowNames();

    Row getRow(String rowKey);

    void removeRow(String rowKey);

    void addSubRow(String rowKey, SubRow subRow);

    void addSubRows(String rowKey, LinkedHashSet<SubRow> subRows);

    int getSubRowCount(String rowKey);

    LinkedHashSet<String> getSubRowNames(String rowKey);

    SubRow getSubRow(String rowKey, String subRowKey);

    void removeSubRow(String rowKey, String subRowKey);

    void addColumn(String rowKey, String subRowKey, Column column);

    void addColumns(String rowKey, String subRowKey, LinkedHashSet<Column> columns);

    int getColumnCount();

    LinkedHashSet<String> getColumnNames();

    Column getColumn(String rowKey, String subRowKey, String columnKey);

    void removeColumn(String rowKey, String subRowKey, String columnKey);

    Class getColumnClass(String rowKey, String subRowKey, String columnKey);

    Object get(String rowKey, String subRowKey, String columnKey);

    Iterator iterator();
}
