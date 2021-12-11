package io.alapierre.func;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created 12.12.18 copyright original authors 2018
 *
 * @author Adrian Lapierre {@literal <adrian@soft-project.pl>}
 */
public class Pair<K, V> implements Serializable {

    private final K key;

    private final V value;

    public Pair(@NotNull K key, @NotNull V value) {
        this.key = key;
        this.value = value;
    }

    @NotNull K getKey() {
        return key;
    }

    @NotNull V getValue() {
        return value;
    }
}
