package io.alapierre.commons.date;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.alapierre.common.util.NullUtil.*;


/**
 * @author Adrian Lapierre {@literal <adrian@soft-project.pl>}
 */
@SuppressWarnings("unused")
public final class DateUtils {

    private DateUtils() {
    }

    public static boolean isSunday(@NotNull LocalDate day) {
        return day.getDayOfWeek().getValue() == 7;
    }

    public static boolean isSatday(@NotNull LocalDate day) {
        return day.getDayOfWeek().getValue() == 6;
    }

    @Contract("null -> null")
    public static Date asDate(LocalDate localDate) {
        return localDate != null ? Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    @Contract("null -> null")
    public static Date asDate(LocalDateTime localDateTime) {
        return localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    @Contract("null -> null")
    public static LocalDate asLocalDate(Date date) {
        return date != null ? Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    public static LocalDateTime asLocalDateTime(@NotNull Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime asLocalDateTime(@NotNull LocalDate date, @NotNull LocalTime time) {
        return LocalDateTime.of(date, time);
    }

    @Contract("null -> null")
    public static LocalDate asLocalDate(LocalDateTime date) {
        return ifNotNull(date, LocalDateTime::toLocalDate);
    }

    @Contract("null -> null")
    public static LocalTime asLocalTime(LocalDateTime date) {
        return ifNotNull(date, LocalDateTime::toLocalTime);
    }


    /**
     * Generuje obiekty dat w podanym okresie inclusive
     *
     * @param from data początkowa włącznie
     * @param to   data końcowa włącznie
     * @return strumień z datami w podanym okresie
     */
    public static Stream<LocalDate> datesFromTo(@NotNull LocalDate from, @NotNull LocalDate to) {
        checkDates(from, to);
        return datesForDays(from, ChronoUnit.DAYS.between(from, to) + 1);
    }

    private static void checkDates(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "data początkowa nie może być null");
        Objects.requireNonNull(to, "data końcowa nie może być null");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Data " + from
                    + " \"od\" nie może być większa od " + to
                    + " \"do\"");
        }
    }

    /**
     * Generuje wskazaną ilość dat
     *
     * @param from data początkowa
     * @param days ilość dni
     */
    private static Stream<LocalDate> datesForDays(@NotNull LocalDate from, long days) {

        return Stream.iterate(from,
                day -> day.plusDays(1))
                .limit(days);
    }

    /**
     * Zwraca mapę w której kluczem jest data a wartością wynik działania funkcji
     *
     * @param from        data początkowa inclusive
     * @param days        ilość dni
     */
    static Map<LocalDate, ?> mapDates(@NotNull LocalDate from, long days, @NotNull Function<LocalDate, ?> valueMapper) {

        return datesForDays(from, days)
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                valueMapper,
                                (v1, v2) -> {
                                    throw new IllegalArgumentException();
                                },
                                TreeMap::new
                        ));
    }

    /**
     * Zwraca mapę w której kluczem jest data a wartością wynik działania fukcji
     *
     * @param from        data początkowa inclusive
     * @param to          data kończowa inclusive
     */
    public static Map<LocalDate, ?> mapDates(@NotNull LocalDate from, LocalDate to, @NotNull Function<LocalDate, ?> valueMapper) {
        checkDates(from, to);
        return mapDates(from, ChronoUnit.DAYS.between(from, to) + 1, valueMapper);
    }

    /**
     * Ilość miesięcy między dwoma datami
     *
     */
    public static long monthsBetweenDates(@NotNull LocalDate from, @NotNull LocalDate to) {
        return ChronoUnit.MONTHS.between(from, to);
    }

    /**
     * Ilość tygodni między dwoma datami
     */
    public static long weeksBetweenDates(@NotNull LocalDate from, @NotNull LocalDate to) {
        return ChronoUnit.WEEKS.between(from, to);
    }

    /**
     * Ilość dni między dwoma datami
     *
     */
    public static long daysBetweenDates(@NotNull LocalDate from, @NotNull LocalDate to) {
        return ChronoUnit.DAYS.between(from, to);
    }

    /**
     * Ilość godzin między dwoma punktami w czasie
     */
    static long hoursBetweenDates(@NotNull LocalDateTime from, @NotNull LocalDateTime to) {
        return ChronoUnit.HOURS.between(from, to);
    }

    /**
     * Sprawdza czy podany zakres zawiera się w określonym zakresie (inclusive)
     *
     * @param dayFrom  początek okresu do sprawdzenia
     * @param dayTo  koniec okresu do sprawdzenia
     * @param from początek okresu
     * @param to   koniec okresu
     */
    public static boolean isRangeInRange(@NotNull LocalDateTime dayFrom, @NotNull LocalDateTime dayTo, @NotNull LocalDateTime from, @NotNull LocalDateTime to) {
        return !(dayFrom.isBefore(from) || dayFrom.isAfter(to)) && !(dayTo.isBefore(from) || dayTo.isAfter(to));
    }

    /**
     * Sprawdza czy podana data zawiera się w określonym zakresie (inclusive)
     *
     * @param day  data do sprawdzenia
     * @param from początek okresu
     * @param to   koniec okresu
     */
    public static boolean isDayInRange(@NotNull Date day, @NotNull Date from, @NotNull Date to) {
        return !(day.before(from) || day.after(to));
    }

    /**
     * Sprawdza czy podana data zawiera się w określonym zakresie (inclusive)
     *
     * @param day  data do sprawdzenia
     * @param from początek okresu
     * @param to   koniec okresu
     */
    public static boolean isDayInRange(@NotNull LocalDate day, @NotNull LocalDate from, @NotNull LocalDate to) {
        return !(day.isBefore(from) || day.isAfter(to));
    }

    /**
     * Sprawdza czy podana data zawiera się w określonym zakresie (inclusive)
     *
     * @param day  data do sprawdzenia
     * @param from początek okresu
     * @param to   koniec okresu
     */
    public static boolean isDayInRange(@NotNull LocalDate day, @NotNull Date from, @NotNull Date to) {
        return !(day.isBefore(asLocalDate(from)) || day.isAfter(asLocalDate(to)));
    }

    /**
     * Sprawdza czy podana data zawiera się w określonym zakresie (inclusive)
     *
     * @param day  data do sprawdzenia
     * @param from początek okresu
     * @param to   koniec okresu
     */
    public static boolean isDayInRange(@NotNull LocalDateTime day, @NotNull LocalDateTime from, @NotNull LocalDateTime to) {
        return !(day.isBefore(from) || day.isAfter(to));
    }

    /**
     * Sprawdza czy data jest równa lub młodsza od podanej
     *
     * @param day1
     * @param day2
     * @return
     */
    public static boolean isAfterOrEquals(@NotNull LocalDate day1, @NotNull LocalDate day2) {
        return !day1.isBefore(day2);
    }

    /**
     * Sprawdza czy data jest równia lub starsza od podanej
     *
     * @param day1
     * @param day2
     * @return
     */
    public static boolean isBeforeOrEquals(@NotNull LocalDate day1, @NotNull LocalDate day2) {
        return !day1.isAfter(day2);
    }

    /**
     * Czy daty pokrywają się częściowo lub całkowicie
     *
     * @param start1
     * @param end1
     * @param start2
     * @param end2
     * @return
     */
    public static boolean overlap(@NotNull LocalDate start1, @NotNull LocalDate end1, @NotNull LocalDate start2, @NotNull LocalDate end2) {
        return !(start1.isAfter(end2) || end1.isBefore(start2));
    }

    /**
     * Czy daty pokrywają się częściowo lub całkowicie
     *
     * @param start1
     * @param end1
     * @param start2
     * @param end2
     * @return
     */
    public static boolean overlap(@NotNull LocalDateTime start1, @NotNull LocalDateTime end1, @NotNull LocalDateTime start2, @NotNull LocalDateTime end2) {
        return !(start1.isAfter(end2) || end1.isBefore(start2));
    }

    /**
     * Czy daty pokrywają się częściowo lub całkowicie z wyłączeniem przylegania
     *
     * @param start1
     * @param end1
     * @param start2
     * @param end2
     * @return
     */
    public static boolean overlapWithoutBorder(@NotNull LocalDateTime start1, @NotNull LocalDateTime end1, @NotNull LocalDateTime start2, @NotNull LocalDateTime end2) {
        return !(start1.isAfter(end2) || end1.isBefore(start2)) && !start1.equals(end2) && !end1.equals(start2);
    }

    public static LocalDate parseDate(String dateFormatted) {
        return LocalDate.parse(dateFormatted, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static LocalDate lastDayOfMonth(@NotNull LocalDate initial) {
        Objects.requireNonNull(initial, "Data nie może być null");
        return initial.withDayOfMonth(initial.lengthOfMonth());
    }

    public static LocalDate firstDayOfMonth(@NotNull LocalDate initial) {
        Objects.requireNonNull(initial, "Data nie może być null");
        return initial.withDayOfMonth(1);
    }

    public static LocalDate firstDayOfYear(@NotNull LocalDate initial) {
        Objects.requireNonNull(initial, "Data nie może być null");
        return initial.withDayOfMonth(1).withMonth(Month.JANUARY.getValue());
    }

    public static LocalDate lastDayOfYear(@NotNull LocalDate initial) {
        Objects.requireNonNull(initial, "Data nie może być null");
        return initial.withMonth(Month.DECEMBER.getValue()).withDayOfMonth(31);
    }

    public static LocalDate monthlyPeriodEndFrom(@NotNull LocalDate initial, long months) {

        return initial.getDayOfMonth() == 1 ?
                DateUtils.firstDayOfMonth(initial.plusMonths(months)).minusDays(1)
                : initial.plusMonths(months);
    }

    public static int weekNumber(@NotNull LocalDate initial) {
        Objects.requireNonNull(initial, "Data nie może być null");
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return initial.get(weekFields.weekOfWeekBasedYear());
    }

}
