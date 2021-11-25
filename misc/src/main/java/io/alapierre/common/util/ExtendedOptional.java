package io.alapierre.common.util;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created 04.04.19 copyright original authors 2019
 *
 * @author Adrian Lapierre {@literal <al@soft-project.pl>}
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
public class ExtendedOptional<T>  {

    private final Optional<T> optional;

    private ExtendedOptional(Optional<T> optional) {
        this.optional = optional;
    }

    public static <T> ExtendedOptional<T> of(Optional<T> optional) {
        return new ExtendedOptional<>(optional);
    }

    public ExtendedOptional<T> ifPresent(Consumer<T> c) {
        optional.ifPresent(c);
        return this;
    }

    public ExtendedOptional<T> ifNotPresent(Runnable r) {
        if (!optional.isPresent())
            r.run();
        return this;
    }

    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (!optional.isPresent())
            emptyAction.run();
        else action.accept(optional.get());
    }

}
