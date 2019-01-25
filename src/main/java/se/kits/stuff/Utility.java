package se.kits.stuff;

import se.kits.stuff.model.WeightedOption;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Utility {
    static String getFormattedTimestamp(String formatPattern, int offsetHours) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(offsetHours));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatPattern);
        return now.format(dateTimeFormatter);
    }

    public static <T> T rollWeightedOptions(List<WeightedOption<T>> weightedOptions) {
        double cumulative = 0.0;
        for (WeightedOption option : weightedOptions) {
            cumulative += option.getWeight();
        }
        double roll = Math.random() * cumulative;
        double stepping = 0.0;

        for (WeightedOption<T> option : weightedOptions) {
            stepping += option.getWeight();
            if (stepping >= roll) {
                return option.getOption();
            }
        }
        throw new RuntimeException("Error at random selection. No valid option selected");
    }
}
