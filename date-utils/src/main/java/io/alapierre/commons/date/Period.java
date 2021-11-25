package io.alapierre.commons.date;

import lombok.Value;
import org.jetbrains.annotations.NotNull;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created 15.05.19 copyright original authors 2019
 *
 * @author Adrian Lapierre {@literal <al@soft-project.pl>}
 */
@Value
public class Period implements Serializable {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    LocalDate start;
    LocalDate end;

    public static Period of(@NotNull LocalDate start, @NotNull LocalDate end) {
        return new Period(start, end);
    }

    public static Period fromString(@NotNull String start, @NotNull String end) {
        return fromString(start,end, DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));
    }

    public static Period fromString(@NotNull String start, @NotNull String end, DateTimeFormatter formatter) {
        return new Period(LocalDate.parse(start, formatter), LocalDate.parse(end, formatter));
    }
}
