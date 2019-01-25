package se.kits.stuff;

import lombok.Value;
import se.kits.stuff.model.LogLevel;
import se.kits.stuff.model.SpringLogGeneratorData;
import se.kits.stuff.model.WeightedOption;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static se.kits.stuff.Utility.rollWeightedOptions;

public class SpringLogGenerator extends SpringLogGeneratorData {

    public static final String SPRING_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final int UTC_OFFSET_HOURS = 1;

    public static String replaceVariablesInPattern(String parameterizedLogRow, LoglevelMessageStatusCodeConnection loglevelMessageStatusCodeConnection) {
        Map<String, List<WeightedOption>> userDefinedProfileMap = Collections.emptyMap();
        Map<String, String> replacementMap = createSpringLogContent(userDefinedProfileMap, loglevelMessageStatusCodeConnection);
        for (String variable : VARIABLE_LIST) {
            parameterizedLogRow = parameterizedLogRow.replace(variable, replacementMap.get(variable));
        }
        return parameterizedLogRow;
    }

    private static Map<String, String> createSpringLogContent(Map<String, List<WeightedOption>> userDefinedProfileMap, LoglevelMessageStatusCodeConnection loglevelMessageStatusCodeConnection) {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put(TIMESTAMP, String.format("%-24s", Utility.getFormattedTimestamp(SPRING_TIMESTAMP_PATTERN, UTC_OFFSET_HOURS)));
        String logLevel;
        String logMessage;
        if (loglevelMessageStatusCodeConnection != null) {
            logLevel = loglevelMessageStatusCodeConnection.getLogLevel().toString();
            logMessage = loglevelMessageStatusCodeConnection.getLogMessageAndStatusCode().getLogMessage();
        } else {
            logLevel = rollWeightedOptions(LOG_LEVELS);
            logMessage = rollWeightedOptions(MESSAGES_PER_LEVEL.get(LogLevel.valueOf(logLevel))).getLogMessage();
        }
        resultMap.put(LOG_LEVEL, String.format("%-5s", logLevel));
        resultMap.put(LOG_MESSAGE, logMessage);

        resultMap.put(PROCESS_ID, String.format("%-5d", (Integer) (new Random().nextInt(20000) + 1)));
        resultMap.put(THREAD, String.format("%15.30s", rollWeightedOptions(THREADS)));
        resultMap.put(LOGGER_NAME, String.format("%-40s", rollWeightedOptions(LOGGERS)));
        return Collections.unmodifiableMap(resultMap);
    }

    public static LoglevelMessageStatusCodeConnection generateLogEventForQueue() {
        String randomizedLevel = rollWeightedOptions(LOG_LEVELS);
        LogMessageAndStatusCode logMessage = rollWeightedOptions(MESSAGES_PER_LEVEL.get(LogLevel.valueOf(randomizedLevel)));
        return new LoglevelMessageStatusCodeConnection(LogLevel.valueOf(randomizedLevel), logMessage);
    }

    @Value
    public static class LoglevelMessageStatusCodeConnection {
        private LogLevel logLevel;
        private LogMessageAndStatusCode logMessageAndStatusCode;
    }
}
