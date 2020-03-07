package pl.com.softproject.utils.pivot.doublepivot.exception;

/**
 * Class SubRowNotFoundException
 *
 * @author Marcin Jasiński {@literal <mkjasinski@gmail.com>}
 */
public class ColumnNotFoundException extends PivotDoubleGroupingTableException {

    public ColumnNotFoundException(final String message) {
        super(message);
    }

    public ColumnNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
