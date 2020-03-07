package io.alapierre.func;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * @author Adrian Lapierre {@literal <alapierre@soft-project.pl>}
 */
public class Reductors {

    public static <T> Optional<Pair<T, T>> minMax(Stream<T> stream, Comparator<T> comparator) {
        return stream
                .map(i -> new Pair<>(i /* "min" */, i /* "max" */))
                .reduce((a, b) -> new Pair<>(
                        // The min of the min elements.
                        comparator.compare(a.getKey(), b.getKey()) < 0 ? a.getKey() : b.getKey(),
                        // The max of the max elements.
                        comparator.compare(a.getValue(), b.getValue()) > 0 ? a.getValue() : b.getValue()));
    }

    public static <T extends Comparable<T>> Optional<Pair<T, T>> minMax(Stream<T> stream) {
        return stream
                .map(i -> new Pair<>(i /* "min" */, i /* "max" */))
                .reduce(minMax());
    }

    private static <T extends Comparable<T>> BinaryOperator<Pair<T, T>> minMax() {
        return (a, b) -> new Pair<>(
                // The min of the min elements.
                a.getKey().compareTo(b.getKey()) < 0 ? a.getKey() : b.getKey(),
                // The max of the max elements.
                a.getValue().compareTo(b.getValue()) > 0 ? a.getValue() : b.getValue());
    }


}
