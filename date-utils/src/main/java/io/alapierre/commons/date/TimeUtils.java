package io.alapierre.commons.date;

import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * @author Adrian Lapierre {@literal <al@alapierre.io>}
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class TimeUtils {

    private TimeUtils() {
    }

    public static LocalTime asLocalTime(@NotNull Time time) {
        return time.toLocalTime();
    }

    public static Time asTime(@NotNull LocalTime localTime) {
        return Time.valueOf(localTime);
    }

    public static boolean isInRange(@NotNull LocalTime time, @NotNull LocalTime from, @NotNull LocalTime to) {
        return !(time.isBefore(from) || time.isAfter(to));
    }

    public static boolean isInRange(@NotNull Time time, @NotNull Time from, @NotNull Time to) {
        return isInRange(asLocalTime(time), asLocalTime(from), asLocalTime(to));
    }

    public static boolean overlap(@NotNull LocalTime start1, @NotNull LocalTime end1, @NotNull LocalTime start2, @NotNull LocalTime end2) {
        return !(start1.isAfter(end2) || end1.isBefore(start2));
    }

    public static boolean overlap(@NotNull Time start1, @NotNull Time end1, @NotNull Time start2, @NotNull Time end2) {
        return overlap(asLocalTime(start1), asLocalTime(end1), asLocalTime(start2), asLocalTime(end2));
    }

    public static boolean overlapElusive(@NotNull LocalTime start1, @NotNull LocalTime end1, @NotNull LocalTime start2, @NotNull LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    public static String formatDuration(@NotNull Duration d) {
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();
        return String.format("%02d:%02d", hours, minutes);
    }

    public static double hoursBetweenTimes(@NotNull LocalTime hourFrom, @NotNull LocalTime hourTo) {
        return ChronoUnit.MINUTES.between(hourFrom, hourTo) / 60.0;
    }

    public static double hoursBetweenTimes(@NotNull LocalDateTime hourFrom, @NotNull LocalDateTime hourTo) {
        return ChronoUnit.MINUTES.between(hourFrom, hourTo) / 60.0;
    }

    public static double hoursBetweenTimes(@NotNull LocalTime hourFrom, @NotNull LocalTime hourTo, @NotNull LocalDate day) {

        if (hourFrom.isBefore(hourTo))
            return ChronoUnit.MINUTES.between(hourFrom, hourTo) / 60.0;

        LocalDateTime from = LocalDateTime.of(day, hourFrom);
        LocalDateTime to = LocalDateTime.of(day.plusDays(1), hourTo);
        return ChronoUnit.MINUTES.between(from, to) / 60.0;
    }

    public static double hourToNumber(@NotNull String hourString) {

        if (hourString.isEmpty())
            return 0d;
        String[] split = hourString.split(":");
        double hour = Double.parseDouble(split[0]);
        double minutes = Double.parseDouble(split[1]) / 60;
        return hour + minutes;
    }
}
