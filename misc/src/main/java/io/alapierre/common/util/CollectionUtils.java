package io.alapierre.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 03.06.19 copyright original authors 2019
 *
 * @author Adrian Lapierre {@literal <al@soft-project.pl>}
 */
@SuppressWarnings("unused")
public class CollectionUtils {

    /**
     * Zwraca listę wartości powstałych przez wykonanie funkcji na każdym elemencie kolekcji wejściowej
     *
     * użycie — pobranie wszystkich id z kolekcji DTO: mapOnePropertyList(list, Dto::getId)`
     *
     * @param list kolekcja wejściowa
     * @param function funkcja, która zostanie wywołana na każdym elemencie kolekcji
     * @param <T> typ kolekcji wejściowej
     * @param <R> typ elementu zwracanego przez funkcję
     * @return lista elementów przekształconych przez funkcję
     */
    public static <T, R> List<R> mapOnePropertyList(@NotNull Collection<T> list, @NotNull Function<? super T, ? extends R> function) {
        return list.stream().map(function).collect(Collectors.toList());
    }

    /**
     * Tworzy zbiór identyfikatorów dla podanej kolekcji wejściowej i funkcji mapującej element do jego identyfikatora
     *
     * @param source - kolekcja wejściowa
     * @param mapper - funkcja mapująca element na klucz
     * @param <R> typ klucza
     * @param <T> typ elementu kolekcji
     * @return zbiór kluczy
     */
    public static <R,T> Set<R> toKeySet(@NotNull List<T> source, @NotNull Function<? super T,? extends R> mapper) {
        return source
                .stream()
                .map(mapper)
                .collect(Collectors.toSet());
    }

    /**
     * Weryfikuje czy przekazana kolekcja jest pusta lub równa null
     *
     * @param collection kolekcja wejściowa
     * @return false, jeśli kolekcja nie jest pusta
     */
    @Contract(value = "null -> true", pure = true)
    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

}
