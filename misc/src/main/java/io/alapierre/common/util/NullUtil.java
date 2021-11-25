package io.alapierre.common.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.function.Function;

/**
 * @author Adrian Lapierre {@literal <adrian@soft-project.pl>}
 */
public final class NullUtil {

    private NullUtil() {
    }

    @Contract("null, _ -> null")
    public static <T, R> R ifNotNull(@Nullable T value, @NotNull Function<T, R> valueMapper) {
        if(value != null) return valueMapper.apply(value);
        return null;
    }

}
