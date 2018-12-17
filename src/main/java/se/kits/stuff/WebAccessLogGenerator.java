package se.kits.stuff;

import se.kits.stuff.model.WebAccessLogGeneratorData;
import se.kits.stuff.model.WebAccessLogGeneratorProfile;
import se.kits.stuff.model.WeightedOption;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    private static Map<String, String> createWebAccessLogContent(Map<String, List<WeightedOption>> customProfileMap) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(REMOTE_ADDR, WeightedOption.rollWeightedOptions(customProfileMap.getOrDefault(REMOTE_ADDR, IP_ADDRESSES)));
        resultMap.put(REMOTE_USER, WeightedOption.rollWeightedOptions(customProfileMap.getOrDefault(REMOTE_USER, USERS)));
        resultMap.put(REQUEST, WeightedOption.rollWeightedOptions(customProfileMap.getOrDefault(REQUEST, REQUEST_TYPES)));
        resultMap.put(TIME_LOCAL, getTimestamp());
        resultMap.put(STATUS, WeightedOption.rollWeightedOptions(customProfileMap.getOrDefault(STATUS, STATUS_TYPES)));
        resultMap.put(BODY_BYTES_SENT, String.valueOf(random.nextInt(HTTP_BODY_BYTES_SENT) + MINIMUM_BYTES_SENT));
        resultMap.put(HTTP_REFERER, WeightedOption.rollWeightedOptions(customProfileMap.getOrDefault(HTTP_REFERER, REFERERS)));
        resultMap.put(HTTP_USER_AGENT, WeightedOption.rollWeightedOptions(customProfileMap.getOrDefault(HTTP_USER_AGENT, USER_AGENTS)));

        return Collections.unmodifiableMap(resultMap);
    }

    public static String replaceVariablesInPattern(String parameterizedLogRow) {
        Map<String, List<WeightedOption>> userDefinedProfileMap = WebAccessLogGeneratorProfile.readWebAccessLogGeneratorProfileFromFile();
        Map<String, String> replacementMap = createWebAccessLogContent(userDefinedProfileMap); // todo temp
        for (String variable : VARIABLE_LIST) {
            parameterizedLogRow = parameterizedLogRow.replace(variable, replacementMap.get(variable));
        }
        return parameterizedLogRow;
    }

    private static String getTimestamp() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(UTC_OFFSET_HOURS));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TIME_LOCAL_PATTERN);
        return now.format(dateTimeFormatter);
    }
}
