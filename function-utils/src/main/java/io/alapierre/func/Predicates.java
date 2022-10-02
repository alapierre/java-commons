package io.alapierre.func;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2020.10.02
 */
public class Predicates {

    public static <T> Predicate notNull() {
        return (Predicate<T>) Objects::nonNull;
    }

    /**
     * Ogranicza do unikalnych elementów wg podanego klucza
     *
     * @param keyExtractor funkcja mapująca element do wartości po jakiej ma być wykonany distinct
     * @param <T> typ elementu kolekcji
     * @return predykat do użycia w metodzie filter
     */
    public static <T> Predicate<T> distinctByKey(@NotNull Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * weryfikuje czy przekazana kolekcja da się zmapować do jednego unikalnego elementu, zgodnie z przekazaną
     * funkcją mapującą.
     *
     * @param input kolekcja wejściowa
     * @param mapper funkcja mapująca elementy kolekcji na element, który ma być unikalny
     * @param <T> typ elementu kolekcji
     * @param <R> typ wynikowy funkcji mapującej
     * @return unikalny element
     * @throws RuntimeException jeśli po zmapowaniu unikalnych elementów nie jest dokładnie jeden
     */
    public static <T, R> R uniqueOrThrow(@NotNull Collection<T> input, @NotNull Function<? super T,? extends R> mapper) {
        List<? extends R> res = input.stream()
                .map(mapper)
                .distinct()
                .collect(Collectors.toList());

        if(res.size() != 1)
            throw new RuntimeException("Expected exactly 1 distinct element but get " + res.size());

        return res.get(0);
    }

    /**
     * Returns a predicate that evaluates to true if the object reference being tested is a member of the given collection.
     *
     * @param target the collection that may contain the function input
     * @param <T> typ elementu kolekcji
     * @return predicate for use in filter method
     */
    public static <T> Predicate<T> in(@NotNull Collection<? extends T> target) {
        return target::contains;
    }

    /**
     * Returns a predicate that evaluates to true if the object reference being tested is NOT a member of the given collection.
     *
     * @param target the collection that may contain the function input
     * @param <T> typ elementu kolekcji
     * @return predicate for use in filter method
     */
    public static <T> Predicate<T> notIn(@NotNull Collection<? extends T> target) {
        return t-> !target.contains(t);
    }

    /**
     * Returns a predicate that evaluates to true if the object reference being tested is a member of the given collection.
     *
     * @param target the collection that may contain the function input
     * @param mapper function transform T in R
     * @param <R> typ elementu kolekcji użytej do filtrowania
     * @param <T> typ elementu wejściowego
     * @return predicate for use in filter method
     */
    public static <T, R> Predicate<T> in(@NotNull Collection<? extends R> target, @NotNull Function<? super T,? extends R> mapper) {
        return t -> target.contains(mapper.apply(t));
    }

    /**
     * Returns a predicate that evaluates to true if the object reference being tested is NOT a member of the given collection.
     *
     * @param target the collection that may contain the function input
     * @param mapper function transform T in R
     * @param <R> typ elementu kolekcji użytej do filtrowania
     * @param <T> typ elementu wejściowego
     * @return predicate for use in filter method
     */
    public static <T, R> Predicate<T> notIn(@NotNull Collection<? extends R> target, @NotNull Function<? super T,? extends R> mapper) {
        return t -> !target.contains(mapper.apply(t));
    }

}
