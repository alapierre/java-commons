package pl.com.softproject.utils.pivot.doublepivot;

import com.google.common.collect.Iterators;

import pl.com.softproject.utils.pivot.doublepivot.exception.ColumnNotFoundException;
import pl.com.softproject.utils.pivot.doublepivot.exception.RowNotFoundException;
import pl.com.softproject.utils.pivot.doublepivot.exception.SubRowNotFoundException;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Class PivotDoubleGroupingTableModelImpl
 *
 * @author Marcin Jasiński {@literal <mkjasinski@gmail.com>}
 */
public class PivotDoubleGroupingTableModelImpl implements PivotDoubleGroupingTableModel {

    public static final String DEFAULT_EMPTY_VALUE = "";

    private LinkedHashSet<Row> rows = new LinkedHashSet<Row>();

    @Override
    public void addRow(final Row row) {
        rows.add(row);
    }

    @Override
    public void addRows(final LinkedHashSet<Row> rows) {
        for (final Row row : rows) {
            addRow(row);
        }
    }

    @Override
    public int getRowsCount() {
        RowIterator rowIterator = (RowIterator) iterator();
        return Iterators.size(rowIterator);
    }

    @Override
    public LinkedHashSet<String> getRowNames() {

        RowIterator rowIterator = (RowIterator) iterator();
        LinkedHashSet<String> names = new LinkedHashSet<String>(rows.size());

        while (rowIterator.hasNext()) {
            names.add(rowIterator.next().getName());
        }
        return names;
    }

    @Override
    public boolean contains(final String elementKey) {
        RowIterator rowIterator = (RowIterator) iterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getKey().equals(elementKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Row getRow(final String rowKey) {
        RowIterator rowIterator = (RowIterator) iterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getKey().equals(rowKey)) {
                return row;
            }
        }
        return null;
    }

    @Override
    public void removeRow(final String rowKey) {
        RowIterator rowIterator = (RowIterator) iterator();

        while (rowIterator.hasNext()) {
            Row next = rowIterator.next();
            if (next.getKey().equals(rowKey)) {
                rowIterator.remove();
            }
        }
    }

    @Override
    public void addSubRow(final String rowKey, final SubRow subRow) {
        Row row = getRow(rowKey);
        if (row == null) {
            throw new RowNotFoundException(String.format("row with key [%s] not found", rowKey));
        }
        row.getSubRows().add(subRow);
    }

    @Override
    public void addSubRows(final String rowKey, final LinkedHashSet<SubRow> subRows) {
        for (final SubRow subRow : subRows) {
            addSubRow(rowKey, subRow);
        }
    }

    @Override
    public int getSubRowCount(final String rowKey) {
        Row row = getRow(rowKey);
        if (row == null) {
            throw new RowNotFoundException(String.format("row with key [%s] not found", rowKey));
        }
        return row.getSubRows().size();
    }

    @Override
    public LinkedHashSet<String> getSubRowNames(final String rowKey) {
        Row row = getRow(rowKey);
        if (row == null) {
            throw new RowNotFoundException(String.format("row with key [%s] not found", rowKey));
        }
        LinkedHashSet<SubRow> subRows = row.getSubRows();
        LinkedHashSet<String> names = new LinkedHashSet<String>(subRows.size());
        for (final SubRow subRow : subRows) {
            names.add(subRow.getName());
        }
        return names;
    }

    @Override
    public SubRow getSubRow(final String rowKey, final String subRowKey) {
        Row row = getRow(rowKey);
        if (row == null) {
            throw new RowNotFoundException(String.format("row with key [%s] not found", rowKey));
        }
        LinkedHashSet<SubRow> subRows = row.getSubRows();
        for (final SubRow subRow : subRows) {
            if (subRow.getKey().equals(subRowKey)) {
                return subRow;
            }
        }
        return null;
    }

    @Override
    public void removeSubRow(final String rowKey, final String subRowKey) {
        Row row = getRow(rowKey);
        if (row == null) {
            throw new RowNotFoundException(String.format("row with key [%s] not found", rowKey));
        }
        Iterator<SubRow> subRowIterator = row.getSubRows().iterator();
        while (subRowIterator.hasNext()) {
            SubRow subRow = subRowIterator.next();
            if (subRow.getKey().equals(subRowKey)) {
                subRowIterator.remove();
            }
        }
    }

    @Override
    public void addColumn(final String rowKey, final String subRowKey, final Column column) {
        Row row = getRow(rowKey);
        if (row == null) {
            throw new RowNotFoundException(String.format("row with key [%s] not found", rowKey));
        }
        SubRow subRow = getSubRow(rowKey, subRowKey);
        if (subRow == null) {
            throw new SubRowNotFoundException(
                    String.format("subRow with key [%s] not found", subRowKey));
        }

        RowIterator rowIterator = (RowIterator) iterator();
        while (rowIterator.hasNext()) {
            Row oneRow = rowIterator.next();
            for (final SubRow oneSubRow : oneRow.getSubRows()) {
                if (oneRow.getKey().equals(rowKey) && oneSubRow.getKey().equals(subRowKey)) {
                    oneSubRow.getColumns().add(column);
                } else {
                    Column emptyColumn =
                            new Column(column.getKey(), column.getName(), DEFAULT_EMPTY_VALUE);
                    oneSubRow.getColumns().add(emptyColumn);
                }
            }
        }
    }

    @Override
    public void addColumns(final String rowKey, final String subRowKey,
                           final LinkedHashSet<Column> columns) {
        for (final Column column : columns) {
            addColumn(rowKey, subRowKey, column);
        }
    }

    @Override
    public int getColumnCount() {
        RowIterator rowIterator = (RowIterator) iterator();
        if (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<SubRow> subRowIterator = row.getSubRows().iterator();
            if (subRowIterator.hasNext()) {
                SubRow subRow = subRowIterator.next();
                return subRow.getColumns().isEmpty() ? 0 : subRow.getColumns().size();
            }
        }
        return 0;
    }

    @Override
    public LinkedHashSet<String> getColumnNames() {
        LinkedHashSet<String> columnNames = new LinkedHashSet<String>();

        RowIterator rowIterator = (RowIterator) iterator();
        if (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<SubRow> subRowIterator = row.getSubRows().iterator();
            if (subRowIterator.hasNext()) {
                SubRow subRow = subRowIterator.next();
                LinkedHashSet<Column> columns = subRow.getColumns();
                for (final Column column : columns) {
                    columnNames.add(column.getName());
                }
                return columnNames;
            }
        }
        return new LinkedHashSet<String>();
    }

    @Override
    public Column getColumn(final String rowKey, final String subRowKey, final String columnKey) {
        Row row = getRow(rowKey);
        if (row == null) {
            throw new RowNotFoundException(String.format("row with key [%s] not found", rowKey));
        }
        SubRow subRow = getSubRow(rowKey, subRowKey);
        if (subRow == null) {
            throw new SubRowNotFoundException(
                    String.format("subRow with key [%s] not found", subRowKey));
        }
        LinkedHashSet<Column> columns = subRow.getColumns();
        for (final Column column : columns) {
            if (column.getKey().equals(columnKey)) {
                return column;
            }
        }
        return null;
    }

    @Override
    public void removeColumn(final String rowKey, final String subRowKey, final String columnKey) {
        Row row = getRow(rowKey);
        if (row == null) {
            throw new RowNotFoundException(String.format("row with key [%s] not found", rowKey));
        }
        SubRow subRow = getSubRow(rowKey, subRowKey);
        if (subRow == null) {
            throw new SubRowNotFoundException(
                    String.format("subRow with key [%s] not found", subRowKey));
        }

        RowIterator rowIterator = (RowIterator) iterator();
        while (rowIterator.hasNext()) {
            Row oneRow = rowIterator.next();
            for (final SubRow oneSubRow : oneRow.getSubRows()) {
                Iterator<Column> columnIterator = oneSubRow.getColumns().iterator();
                while (columnIterator.hasNext()) {
                    Column column = columnIterator.next();
                    if (column.getKey().equals(columnKey)) {
                        columnIterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public Class getColumnClass(final String rowKey, final String subRowKey,
                                final String columnKey) {
        Column column = getColumn(rowKey, subRowKey, columnKey);
        if (column == null) {
            throw new ColumnNotFoundException(
                    String.format("column with key [%s] not found", columnKey));
        }
        return column.getValue().getClass();
    }

    @Override
    public Object get(final String rowKey, final String subRowKey, final String columnKey) {
        Column column = getColumn(rowKey, subRowKey, columnKey);
        if (column == null) {
            throw new ColumnNotFoundException(
                    String.format("column with key [%s] not found", columnKey));
        }
        return column.getValue();
    }

    @Override
    public Iterator iterator() {
        return new RowIterator(this);
    }

    public static class RowIterator implements Iterator<Row> {

        private final Iterator<Row> iterator;

        RowIterator(PivotDoubleGroupingTableModelImpl pivotDoubleTableModel) {
            iterator = pivotDoubleTableModel.rows.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Row next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
