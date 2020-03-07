package pl.com.softproject.utils.pivot.doublepivot;

import java.util.LinkedHashSet;

/**
 * Class SubRow
 *
 * @author Marcin Jasiński {@literal <mkjasinski@gmail.com>}
 */
public class SubRow implements Cell, Containable {

    private String key;
    private String name;
    private LinkedHashSet<Column> columns = new LinkedHashSet<Column>();

    public SubRow() {
    }

    public SubRow(final String key, final String name, final LinkedHashSet<Column> columns) {
        this.key = key;
        this.name = name;
        this.columns = columns;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LinkedHashSet<Column> getColumns() {
        return columns;
    }

    public void setColumns(final LinkedHashSet<Column> columns) {
        this.columns = columns;
    }

    @Override
    public boolean contains(final String elementKey) {
        for (final Column column : columns) {
            if (column.getKey().equals(elementKey)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SubRow subRow = (SubRow) o;

        return !(key != null ? !key.equals(subRow.key) : subRow.key != null);

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SubRow{" +
               "key='" + key + '\'' +
               ", name='" + name + '\'' +
               '}';
    }
}
