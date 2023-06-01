package helper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

/**
 * Date and time utility class
 */
public class DateTimeUtils {

    public static DateTimeFormatter ISO8601Formatter = ISO_LOCAL_DATE_TIME;

    public static String formatISO8601(TemporalAccessor temporalAccessor) {
        if (temporalAccessor instanceof Instant)
            temporalAccessor = Instant.from(temporalAccessor).atZone(ZoneOffset.UTC);

        return ISO8601Formatter.format(temporalAccessor);
    }

    public static TemporalAccessor parseISO8601(String datetimeString) {
        return ISO8601Formatter.parse(datetimeString);
    }

    public static long getEpochSeconds(String datetimeString) {
        return LocalDateTime.from(parseISO8601(datetimeString)).toEpochSecond(ZoneOffset.UTC);
    }
}