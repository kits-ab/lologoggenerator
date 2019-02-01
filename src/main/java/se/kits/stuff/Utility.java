package se.kits.stuff;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import se.kits.stuff.model.LogFileDefinition;
import se.kits.stuff.model.WeightedOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utility {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Utility.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    public static void writeConfigToFile(List<LogFileDefinition> logFileDefinitions, String configFilepath) {
        try {
            Files.write(Paths.get(configFilepath), OBJECT_MAPPER.writeValueAsString(logFileDefinitions).getBytes());
            LOGGER.info("Wrote config {} to file {}", logFileDefinitions, configFilepath);
        } catch (IOException e) {
            LOGGER.error("Failed to write to file: {} - {}", configFilepath, e.toString());
        }
    }

    public static ArrayList<LogFileDefinition> readConfigsFromFile(String configFilepath) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(configFilepath));
            LogFileDefinition[] logFileDefinitionsArray = OBJECT_MAPPER.readValue(bytes, LogFileDefinition[].class);
            return new ArrayList<>(Arrays.asList(logFileDefinitionsArray));
        } catch (IOException e) {
            LOGGER.error("IO Error, failed to read {}", configFilepath);
            return null;
        }
    }
}
