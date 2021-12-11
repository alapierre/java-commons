package io.alapierre.func;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * @author Adrian Lapierre {@literal <alapierre@soft-project.pl>}
 */
@SuppressWarnings("unused")
public class Reductors {

    /**
     * Returns min ane max value from given stream as a Pair
     *
     * @param stream input stream
     * @param comparator comparator for type of data in stream
     * @param <T> type od data
     * @return min and max value as Optional
     */
    public static <T> @NotNull Optional<Pair<T, T>> minMax(@NotNull Stream<T> stream, @NotNull Comparator<T> comparator) {
        return stream
                .map(i -> new Pair<>(i /* "min" */, i /* "max" */))
                .reduce((a, b) -> new Pair<>(
                        // The min of the min elements.
                        comparator.compare(a.getKey(), b.getKey()) < 0 ? a.getKey() : b.getKey(),
                        // The max of the max elements.
                        comparator.compare(a.getValue(), b.getValue()) > 0 ? a.getValue() : b.getValue()));
    }

    /**
     * Returns min ane max value from given stream of comparable data as a Pair
     *
     * @param stream input stream of comparable objects
     * @param <T> type od data
     * @return min and max value as Optional
     */
    public static <T extends Comparable<T>> @NotNull Optional<Pair<T, T>> minMax(@NotNull Stream<T> stream) {
        return stream
                .map(i -> new Pair<>(i /* "min" */, i /* "max" */))
                .reduce(minMax());
    }

    /**
     * Convenient way to get min value from given two comparable objects with null handling.
     *
     * @param a first value
     * @param b second value
     * @param <T> type od data
     * @return 'b' if 'a' is null or 'a' if 'b' is null or min from 'a' and 'b'
     */
    public static <T extends Comparable<T>> T min(@Nullable T a, @Nullable T b) {
        return a == null ? b : (b == null ? a : (a.compareTo(b) < 0 ? a : b));
    }

    /**
     * Max value from given two comparable objects
     *
     * @param a first value
     * @param b second value
     * @param <T> type od data
     * @return 'b' if 'a' is null or 'a' if 'b' is null or max from 'a' and 'b'
     */
    public static <T extends Comparable<T>> T max(@Nullable T a, @Nullable T b) {
        return a == null ? b : (b == null ? a : (a.compareTo(b) > 0 ? a : b));
    }

    @Contract(pure = true)
    private static <T extends Comparable<T>> @NotNull BinaryOperator<Pair<T, T>> minMax() {
        return (a, b) -> new Pair<>(
                // The min of the min elements.
                a.getKey().compareTo(b.getKey()) < 0 ? a.getKey() : b.getKey(),
                // The max of the max elements.
                a.getValue().compareTo(b.getValue()) > 0 ? a.getValue() : b.getValue());
    }

}
