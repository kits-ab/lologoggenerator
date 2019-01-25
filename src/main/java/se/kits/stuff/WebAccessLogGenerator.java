package se.kits.stuff;

import se.kits.stuff.model.WebAccessLogGeneratorData;
import se.kits.stuff.model.WebAccessLogGeneratorProfile;
import se.kits.stuff.model.WeightedOption;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WebAccessLogGenerator extends WebAccessLogGeneratorData {

    private static final String TIME_LOCAL_PATTERN = "dd/MMM/yyyy:HH:mm:ss Z";
    private static final int HTTP_BODY_BYTES_SENT = 5678;
    private static final int UTC_OFFSET_HOURS = 1;
    private static final int MINIMUM_BYTES_SENT = 50;
    private static Random random = new Random();

    public WebAccessLogGenerator() {

    }

    public static String replaceVariablesInPattern(String parameterizedLogRow, Map<String, String> queuedMap) {
        Map<String, List<WeightedOption<String>>> userDefinedProfileMap = WebAccessLogGeneratorProfile.readWebAccessLogGeneratorProfileFromFile();
        for (String variable : VARIABLE_LIST) {
            if (DEFAULT_OPTIONS_MAPPING.containsKey(variable)) {
                parameterizedLogRow = parameterizedLogRow.replace(variable, selectReplacement(variable, userDefinedProfileMap, queuedMap));
            }
        }
        parameterizedLogRow = parameterizedLogRow.replace(BODY_BYTES_SENT, String.valueOf(random.nextInt(HTTP_BODY_BYTES_SENT) + MINIMUM_BYTES_SENT));
        return parameterizedLogRow.replace(TIME_LOCAL, Utility.getFormattedTimestamp(TIME_LOCAL_PATTERN, UTC_OFFSET_HOURS));
    }

    private static String selectReplacement(String key, Map<String, List<WeightedOption<String>>> customProfileMap, Map<String, String> queuedMap) {
        return queuedMap.containsKey(key) ? queuedMap.get(key) : Utility.rollWeightedOptions(customProfileMap.getOrDefault(key, DEFAULT_OPTIONS_MAPPING.get(key)));
    }

    public static Map<String, String> generateStatusCodeForQueue(SpringLogGenerator.LoglevelMessageStatusCodeConnection loglevelMessageStatusCodeConnection) {
        Integer httpStatusCode = loglevelMessageStatusCodeConnection.getLogMessageAndStatusCode().getHttpStatusCode();
        if (httpStatusCode != null) {
            Map<String, String> replacements = new HashMap<>();
            replacements.put(WebAccessLogGeneratorData.STATUS, String.valueOf(httpStatusCode));
            return replacements;
        } else {
            return Collections.emptyMap();
        }
    }
}