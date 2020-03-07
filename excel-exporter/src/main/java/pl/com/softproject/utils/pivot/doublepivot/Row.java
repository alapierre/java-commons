package pl.com.softproject.utils.pivot.doublepivot;

import java.util.LinkedHashSet;

/**
 * Class Row
 *
 * @author Marcin Jasiński {@literal <mkjasinski@gmail.com>}
 */
public class Row implements Cell, Containable {

    private String key;
    private String name;
    private LinkedHashSet<SubRow> subRows = new LinkedHashSet<SubRow>();

    public Row() {
    }

    public Row(final String key, final String name, final LinkedHashSet<SubRow> subRows) {
        this.key = key;
        this.name = name;
        this.subRows = subRows;
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

    public LinkedHashSet<SubRow> getSubRows() {
        return subRows;
    }

    public void setSubRows(final LinkedHashSet<SubRow> subRows) {
        this.subRows = subRows;
    }

    @Override
    public boolean contains(final String elementKey) {
        for (final SubRow subRow : subRows) {
            if (subRow.getKey().equals(elementKey)) {
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

        final Row row = (Row) o;

        return !(key != null ? !key.equals(row.key) : row.key != null);

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Row{" +
               "key='" + key + '\'' +
               ", name='" + name + '\'' +
               '}';
    }
}
