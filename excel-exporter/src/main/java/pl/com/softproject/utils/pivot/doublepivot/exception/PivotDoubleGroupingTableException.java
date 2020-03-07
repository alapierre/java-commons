package pl.com.softproject.utils.pivot.doublepivot.exception;

/**
 * Class PivotDoubleGroupingTableException
 *
 * @author Marcin Jasiński {@literal <mkjasinski@gmail.com>}
 */
public class PivotDoubleGroupingTableException extends RuntimeException {

    public PivotDoubleGroupingTableException(final String message) {
        super(message);
    }

    public PivotDoubleGroupingTableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
