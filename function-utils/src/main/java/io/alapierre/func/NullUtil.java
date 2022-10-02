package io.alapierre.func;

import java.util.function.Function;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2016.08.03
 */
public class NullUtil {

    private NullUtil() {
    }

    public static <T, R> R ifNotNull(T value, Function<T, R> valueMapper) {
        if(value != null) return valueMapper.apply(value);
        return null;
    }

}
