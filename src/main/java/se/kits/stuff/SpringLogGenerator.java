package se.kits.stuff;

import se.kits.stuff.model.SpringLogGeneratorData;
import se.kits.stuff.model.WeightedOption;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static se.kits.stuff.model.WeightedOption.rollWeightedOptions;

public class SpringLogGenerator extends SpringLogGeneratorData {

    public static final String SPRING_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final int UTC_OFFSET_HOURS = 1;

    public static String replaceVariablesInPattern(String parameterizedLogRow) {
        Map<String, List<WeightedOption>> userDefinedProfileMap = Collections.emptyMap();
        Map<String, String> replacementMap = createSpringLogContent(userDefinedProfileMap);
        for (String variable : VARIABLE_LIST) {
            parameterizedLogRow = parameterizedLogRow.replace(variable, replacementMap.get(variable));
        }
        return parameterizedLogRow;
    }

    private static Map<String, String> createSpringLogContent(Map<String, List<WeightedOption>> userDefinedProfileMap) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(TIMESTAMP, String.format("%-24s", Utility.getFormattedTimestamp(SPRING_TIMESTAMP_PATTERN, UTC_OFFSET_HOURS)));
        String levelPicked = rollWeightedOptions(LOG_LEVELS);
        resultMap.put(LOG_LEVEL, String.format("%-5s", levelPicked));
        resultMap.put(LOG_MESSAGE, rollWeightedOptions(MESSAGES_PER_LEVEL.get(levelPicked)));
        resultMap.put(PROCESS_ID, String.format("%-5d", randomizeProcessId()));
        resultMap.put(THREAD, String.format("%15.30s", rollWeightedOptions(THREADS)));
        resultMap.put(LOGGER_NAME, String.format("%-40s", rollWeightedOptions(LOGGERS)));
        return Collections.unmodifiableMap(resultMap);
    }

    public static Integer randomizeProcessId() {
        return new Random().nextInt(20000) + 1;
    }
}
