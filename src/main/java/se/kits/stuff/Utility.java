package se.kits.stuff;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Utility {
    static String getFormattedTimestamp(String formatPattern, int offsetHours) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(offsetHours));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatPattern);
        return now.format(dateTimeFormatter);
    }
}
