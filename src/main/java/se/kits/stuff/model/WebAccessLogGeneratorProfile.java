package se.kits.stuff.model;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import se.kits.stuff.WebAccessLogGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebAccessLogGeneratorProfile {

    private static final HashMap<String, List<WeightedOption<String>>> PROFILE = new HashMap<>();

    private static final String APPLOLOGOG_CONFIG_DIR = "/app/lologog/config/";
    private static final String WEBACCESSLOG_PROFILE = "webaccesslog_profile.json";
    private static final String WEBACCESSLOG_PROFILE_FILEPATH = APPLOLOGOG_CONFIG_DIR + WEBACCESSLOG_PROFILE;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(WebAccessLogGenerator.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void setNewOptions(String key, List<WeightedOption<String>> newOptions) {
        PROFILE.put(key, newOptions);
    }

    public HashMap<String, List<WeightedOption<String>>> getProfile() {
        return PROFILE;
    }

    public static void writeWebAccessLogGeneratorProfileToFile(Map<String, List<WeightedOption<String>>> customProfileMap) {
        try {
            Files.write(Paths.get(WEBACCESSLOG_PROFILE_FILEPATH), OBJECT_MAPPER.writeValueAsString(customProfileMap).getBytes());
            LOGGER.info("Profile written to: {}", WEBACCESSLOG_PROFILE_FILEPATH);
        } catch (IOException e) {
            LOGGER.error("Write failure when setting profile: {}: {}", WEBACCESSLOG_PROFILE_FILEPATH, e.toString());
        }
    }

    public static Map<String, List<WeightedOption<String>>> readWebAccessLogGeneratorProfileFromFile() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(WEBACCESSLOG_PROFILE_FILEPATH));
            TypeReference<Map<String, List<WeightedOption<String>>>> typeReference = new TypeReference<Map<String, List<WeightedOption<String>>>>() {
            };
            LOGGER.info("Read Web Access Log Profile from file: {}", WEBACCESSLOG_PROFILE_FILEPATH);
            return OBJECT_MAPPER.readValue(bytes, typeReference);
        } catch (IOException e) {
            LOGGER.error("Reading failure when getting web access log profile: {}: {}", WEBACCESSLOG_PROFILE_FILEPATH, e.toString());
            return Collections.emptyMap();
        }
    }
}
