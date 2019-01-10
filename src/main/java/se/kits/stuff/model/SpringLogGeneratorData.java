package se.kits.stuff.model;

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

    public static final List<WeightedOption> LOG_LEVELS = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption("TRACE", 0.1),
                    new WeightedOption("DEBUG", 0.1),
                    new WeightedOption("INFO", 0.6),
                    new WeightedOption("WARN", 0.1),
                    new WeightedOption("ERROR", 0.1)
            )
    );

    public static final List<WeightedOption> THREADS = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption("Thread1", 0.5),
                    new WeightedOption("Thread2", 0.2),
                    new WeightedOption("Thread3", 0.2),
                    new WeightedOption("Thread4", 0.1)
            )
    );

    public static final List<WeightedOption> LOGGERS = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption("o.s.d.r.w.RepositoryRestHandlerMapping", 0.2),
                    new WeightedOption("o.s.b.w.embedded.tomcat.TomcatWebServer", 0.2),
                    new WeightedOption("s.w.s.m.m.a.RequestMappingHandlerMapping", 0.2),
                    new WeightedOption("o.s.d.n.mapping.Neo4jPersistentProperty", 0.2),
                    new WeightedOption("o.s.d.r.w.RepositoryRestHandlerAdapter", 0.2)
            )
    );

    public static final List<WeightedOption> TRACE_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption("trace message 1", 0.2),
                    new WeightedOption("trace message 2", 0.2),
                    new WeightedOption("trace message 3", 0.2),
                    new WeightedOption("trace message 4", 0.2),
                    new WeightedOption("trace message 5", 0.2)
            )
    );

    public static final List<WeightedOption> DEBUG_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption("debug message 1", 0.2),
                    new WeightedOption("debug message 2", 0.2),
                    new WeightedOption("debug message 3", 0.2),
                    new WeightedOption("debug message 4", 0.2),
                    new WeightedOption("debug message 5", 0.2)
            )
    );

    public static final List<WeightedOption> INFO_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption("User changed values", 0.2),
                    new WeightedOption("Endpoint accessed", 0.2),
                    new WeightedOption("this thing started", 0.2),
                    new WeightedOption("this thing stopped", 0.2),
                    new WeightedOption("DB query", 0.2)
            )
    );

    public static final List<WeightedOption> WARN_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption("check this", 0.5),
                    new WeightedOption("check that", 0.5)
            )
    );

    public static final List<WeightedOption> ERROR_MESSAGES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption("DB error", 0.25),
                    new WeightedOption("IO error", 0.25),
                    new WeightedOption("unexpected meltdown", 0.25),
                    new WeightedOption("timeout from service 1", 0.25)
            )
    );

    public static final Map<String, List<WeightedOption>> MESSAGES_PER_LEVEL = createMessagesPerLevelMap();

    private static Map<String, List<WeightedOption>> createMessagesPerLevelMap() {
        Map<String, List<WeightedOption>> map = new HashMap<>();
        map.put("TRACE", TRACE_MESSAGES);
        map.put("DEBUG", DEBUG_MESSAGES);
        map.put("INFO", INFO_MESSAGES);
        map.put("WARN", WARN_MESSAGES);
        map.put("ERROR", ERROR_MESSAGES);
        return map;
    }
}
