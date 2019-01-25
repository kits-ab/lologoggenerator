package se.kits.stuff.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class WebAccessLogGeneratorData {

    public static final String REMOTE_ADDR = "$remote_addr";
    public static final String REMOTE_USER = "$remote_user";
    public static final String REQUEST = "$request";
    public static final String STATUS = "$status";
    public static final String BODY_BYTES_SENT = "$body_bytes_sent";
    public static final String HTTP_REFERER = "$http_referer";
    public static final String HTTP_USER_AGENT = "$http_user_agent";
    public static final String TIME_LOCAL = "$time_local";

    public static final List<String> VARIABLE_LIST = Collections.unmodifiableList(Arrays.asList(
            REMOTE_ADDR,
            REMOTE_USER,
            TIME_LOCAL,
            REQUEST,
            STATUS,
            BODY_BYTES_SENT,
            HTTP_REFERER,
            HTTP_USER_AGENT
    ));

    public static final List<WeightedOption<String>> IP_ADDRESSES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>("11.11.11.22", 0.25),
                    new WeightedOption<>("22.22.22.33", 0.25),
                    new WeightedOption<>("33.33.33.44", 0.25),
                    new WeightedOption<>("44.44.44.55", 0.25)
            )
    );
    public static final List<WeightedOption<String>> USERS = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>("dummy-user", 0.47),
                    new WeightedOption<>("test-admin", 0.20),
                    new WeightedOption<>("-", 0.33)
            )
    );
    public static final List<WeightedOption<String>> REQUEST_TYPES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>("POST /users HTTP/1.1", 0.2),
                    new WeightedOption<>("GET /users/list HTTP/1.1", 0.8)
            )
    );

    public static final List<WeightedOption<String>> STATUS_TYPES = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>("200", 0.7),
                    new WeightedOption<>("400", 0.2),
                    new WeightedOption<>("500", 0.1)
            )
    );

    public static final List<WeightedOption<String>> REFERERS = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>("-", 0.25),
                    new WeightedOption<>("http://www.kits.se/", 0.75)
            )
    );
    public static final List<WeightedOption<String>> USER_AGENTS = Collections.unmodifiableList(
            Arrays.asList(
                    new WeightedOption<>("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/67.0.3396.99 Chrome/67.0.3396.99 Safari/537.36", 0.5),
                    new WeightedOption<>("Mozilla/5.0 (Android 8.0.0; Mobile; rv:63.0) Gecko/63.0 Firefox/63.0", 0.5)
            )
    );

    public static final Map<String, List<WeightedOption<String>>> DEFAULT_OPTIONS_MAPPING = bindDefaultOptionsMapping();

    private static Map<String, List<WeightedOption<String>>> bindDefaultOptionsMapping() {
        Map<String, List<WeightedOption<String>>> optionsMap = new HashMap<>();
        optionsMap.put(REMOTE_ADDR, IP_ADDRESSES);
        optionsMap.put(REMOTE_USER, USERS);
        optionsMap.put(REQUEST, REQUEST_TYPES);
        optionsMap.put(STATUS, STATUS_TYPES);
        optionsMap.put(HTTP_REFERER, REFERERS);
        optionsMap.put(HTTP_USER_AGENT, USER_AGENTS);
        return optionsMap;
    }
}
