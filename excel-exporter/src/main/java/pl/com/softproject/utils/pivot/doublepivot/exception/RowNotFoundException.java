package pl.com.softproject.utils.pivot.doublepivot.exception;

/**
 * Class RowNotFoundException
 *
 * @author Marcin Jasiński {@literal <mkjasinski@gmail.com>}
 */
public class RowNotFoundException extends PivotDoubleGroupingTableException {

    public RowNotFoundException(final String message) {
        super(message);
    }

    public RowNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
