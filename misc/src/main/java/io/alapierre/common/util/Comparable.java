package io.alapierre.common.util;

/**
 * Created 19.04.19 copyright original authors 2019
 *
 * @author Adrian Lapierre {@literal <al@soft-project.pl>}
 */
@SuppressWarnings("unused")
public class Comparable {

    public static <T extends java.lang.Comparable<T>> T min(T a, T b) {
        return a == null ? b : (b == null ? a : (a.compareTo(b) < 0 ? a : b));
    }

    public static <T extends java.lang.Comparable<T>> T max(T a, T b) {
        return a == null ? b : (b == null ? a : (a.compareTo(b) > 0 ? a : b));
    }

}
