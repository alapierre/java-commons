package io.alapierre.io;

import java.io.IOException;

/**
 * Interfejs funkcyjny dla obsługi operacji IO
 *
 * Created by adrian on 2017-12-10.
 */
@FunctionalInterface
public interface IOConsumer<T> {
    void accept(T t) throws IOException;
}
