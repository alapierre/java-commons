package pl.com.softproject.utils.pivot.doublepivot.exception;

/**
 * Class SubRowNotFoundException
 *
 * @author Marcin Jasiński {@literal <mkjasinski@gmail.com>}
 */
public class SubRowNotFoundException extends PivotDoubleGroupingTableException {

    public SubRowNotFoundException(final String message) {
        super(message);
    }

    public SubRowNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
