package io.alapierre.common.util;

import java.util.Iterator;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.14
 */
public class Iterators {

    public static <T> int size(Iterator<T> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }
}
