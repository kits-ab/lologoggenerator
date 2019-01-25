package se.kits.stuff.model;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SpringLogGeneratorData {

    public static final String TIMESTAMP = "$timestamp";
    public static final String LOG_LEVEL = "$logLevel";
    public static final String PROCESS_ID = "$processId";
    public static final String THREAD = "$thread";
    public static final String LOGGER_NAME = "$loggerName";
    public static final String LOG_MESSAGE = "$logMessage";

    public static final List<String> VARIABLE_LIST = Collections.unmodifiableList(
            Arrays.asList(
                    TIMESTAMP,
                    LOG_LEVEL,
                    PROCESS_ID,
                    THREAD,
                    LOGGER_NAME,
                    LOG_MESSAGE
            )
    );

    public static final List<WeightedOption<String>> LOG_LEVELS = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>(LogLevel.TRACE.toString(), 0.1),
                    new WeightedOption<>(LogLevel.DEBUG.toString(), 0.1),
                    new WeightedOption<>(LogLevel.INFO.toString(), 0.6),
                    new WeightedOption<>(LogLevel.WARN.toString(), 0.1),
                    new WeightedOption<>(LogLevel.ERROR.toString(), 0.1)
            )
    );

    public static final List<WeightedOption<String>> THREADS = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>("Thread1", 0.5),
                    new WeightedOption<>("Thread2", 0.2),
                    new WeightedOption<>("Thread3", 0.2),
                    new WeightedOption<>("Thread4", 0.1)
            )
    );

    public static final List<WeightedOption<String>> LOGGERS = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>("o.s.d.r.w.RepositoryRestHandlerMapping", 0.2),
                    new WeightedOption<>("o.s.b.w.embedded.tomcat.TomcatWebServer", 0.2),
                    new WeightedOption<>("s.w.s.m.m.a.RequestMappingHandlerMapping", 0.2),
                    new WeightedOption<>("o.s.d.n.mapping.Neo4jPersistentProperty", 0.2),
                    new WeightedOption<>("o.s.d.r.w.RepositoryRestHandlerAdapter", 0.2)
            )
    );

    @Value
    @AllArgsConstructor
    public static class LogMessageAndStatusCode {
        private String logMessage;
        private Integer httpStatusCode;

        public LogMessageAndStatusCode(String logMessage) {
            this.logMessage = logMessage;
            this.httpStatusCode = null;
        }
    }

    public static final List<WeightedOption<LogMessageAndStatusCode>> TRACE_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>(new LogMessageAndStatusCode("trace message 1"), 0.5),
                    new WeightedOption<>(new LogMessageAndStatusCode("trace message 2"), 0.5)
            )
    );

    public static final List<WeightedOption<LogMessageAndStatusCode>> DEBUG_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>(new LogMessageAndStatusCode("debug message 1"), 0.5),
                    new WeightedOption<>(new LogMessageAndStatusCode("debug message 2"), 0.5)
            )
    );

    public static final List<WeightedOption<LogMessageAndStatusCode>> INFO_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>(new LogMessageAndStatusCode("info message 1"), 0.2),
                    new WeightedOption<>(new LogMessageAndStatusCode("Deleted data", 200), 0.1),
                    new WeightedOption<>(new LogMessageAndStatusCode("Created data", 201), 0.1),
                    new WeightedOption<>(new LogMessageAndStatusCode("Fetched data", 200), 0.1),
                    new WeightedOption<>(new LogMessageAndStatusCode("Changed data", 200), 0.1),
                    new WeightedOption<>(new LogMessageAndStatusCode("Bad request", 400), 0.1),
                    new WeightedOption<>(new LogMessageAndStatusCode("Unauthorized", 401), 0.1),
                    new WeightedOption<>(new LogMessageAndStatusCode("Forbidden", 403), 0.1),
                    new WeightedOption<>(new LogMessageAndStatusCode("Not found", 404), 0.1)
            )
    );

    public static final List<WeightedOption<LogMessageAndStatusCode>> WARN_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>(new LogMessageAndStatusCode("warning message 1"), 0.5),
                    new WeightedOption<>(new LogMessageAndStatusCode("warning message 2"), 0.5)
            )
    );

    public static final List<WeightedOption<LogMessageAndStatusCode>> ERROR_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>(new LogMessageAndStatusCode("DB error", 500), 0.2),
                    new WeightedOption<>(new LogMessageAndStatusCode("IO error", 500), 0.2),
                    new WeightedOption<>(new LogMessageAndStatusCode("Runtime error", 500), 0.2),
                    new WeightedOption<>(new LogMessageAndStatusCode("Timeout from service 1", 504), 0.4)
            )
    );

    public static final Map<LogLevel, List<WeightedOption<LogMessageAndStatusCode>>> MESSAGES_PER_LEVEL = createMessagesPerLevelMap();

    private static Map<LogLevel, List<WeightedOption<LogMessageAndStatusCode>>> createMessagesPerLevelMap() {
        Map<LogLevel, List<WeightedOption<LogMessageAndStatusCode>>> map = new HashMap<>();
        map.put(LogLevel.TRACE, TRACE_MESSAGES);
        map.put(LogLevel.DEBUG, DEBUG_MESSAGES);
        map.put(LogLevel.INFO, INFO_MESSAGES);
        map.put(LogLevel.WARN, WARN_MESSAGES);
        map.put(LogLevel.ERROR, ERROR_MESSAGES);
        return map;
    }
}
