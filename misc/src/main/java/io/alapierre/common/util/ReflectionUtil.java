package io.alapierre.common.util;


import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.math.BigDecimal;

/**
 * @author Piotr Puchta {@literal <ppuchta@soft-project.pl>}
 */
@SuppressWarnings("unused")
public final class ReflectionUtil {

    public static <T> T getValue(@NotNull Class<T> type, String value) {

        Object response = null;
        if (type.isEnum()) {
            response = fromEnum((Class<? extends Enum>) type, value);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            response = fromBoolean(value);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            response = fromLong(value);
        } else if (type.equals(String.class)) {
            response = value;
        } else if(type.equals(BigDecimal.class)) {
            response = fromBigDecimal(value);
        } else if(type.equals(double.class) || type.equals(Double.class)) {
            response = fromDouble(value);
        }
        else if(type.equals(Integer.class) || type.equals(int.class)){
            response = fromInteger(value);
        }
        return (T) response;
    }

    private static Object fromEnum(@NotNull Class<? extends Enum> type, String value) {
        return Enum.valueOf(type, value);
    }

    private static Boolean fromBoolean(String value) {
        return Boolean.valueOf(value);
    }

    private static Long fromLong(String value) {

        if (value == null || "".equals(value))
            return null;

        return Long.valueOf(value);
    }

    private static BigDecimal fromBigDecimal(String value) {

        if (value == null || "".equals(value))
            return null;

        return BigDecimal.valueOf(Double.parseDouble(value));
    }

    private static Double fromDouble(String value) {

        if (value == null || "".equals(value))
            return null;

        return Double.valueOf(value);
    }

    private static Integer fromInteger(String value) {

        if (value == null || "".equals(value))
            return null;

        return Integer.valueOf(value);
    }

    public static void preventNullInBoolean(@NotNull Object target, boolean value) throws IllegalAccessException, NoSuchFieldException {
        for (Field field : target.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getType().equals(Boolean.class)) {
                Object fieldValue = field.get(target);
                if (fieldValue == null) {
                    Field oldField = target.getClass().getDeclaredField(field.getName());
                    oldField.setAccessible(true);
                    oldField.set(target, value);
                }
            }
        }
    }
}
