package se.kits.stuff;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebAccessLogGenerator {

    private static final String TIME_LOCAL_PATTERN = "dd/MMM/yyyy:HH:mm:ss Z";

    private static final String REMOTE_ADDR = "$remote_addr";
    private static final String REMOTE_USER = "$remote_user";
    private static final String REQUEST = "$request";
    private static final String STATUS = "$status";
    private static final String BODY_BYTES_SENT = "$body_bytes_sent";
    private static final String HTTP_REFERER = "$http_referer";
    private static final String HTTP_USER_AGENT = "$http_user_agent";
    private static final String TIME_LOCAL = "$time_local";

    private static final List<String> variableList = Collections.unmodifiableList(Arrays.asList(
            REMOTE_ADDR,
            REMOTE_USER,
            TIME_LOCAL,
            REQUEST,
            STATUS,
            BODY_BYTES_SENT,
            HTTP_REFERER,
            HTTP_USER_AGENT
    ));

    private static Map<String, String> CONTENT_MAP = createWebAccessLogContent();

    public WebAccessLogGenerator() {

    }

    private static Map<String, String> createWebAccessLogContent() {
        Map<String, String> map = new HashMap<>();
        map.put(REMOTE_ADDR, "38.39.40.41");
        map.put(REMOTE_USER, "dummy-user");
        map.put(REQUEST, "GET /news/53f8d72920ba2744fe873ebc.html HTTP/1.1");
        map.put(STATUS, "200");
        map.put(BODY_BYTES_SENT, "3021");
        map.put(HTTP_REFERER, "-");
        map.put(HTTP_USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/67.0.3396.99 Chrome/67.0.3396.99 Safari/537.36");

        return Collections.unmodifiableMap(map);
    }

    public static String replaceVariablesInPattern(String parameterizedLogRow) {
        for (String variable : variableList) {
            if (variable.equals(TIME_LOCAL)) {
                OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(1));
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(TIME_LOCAL_PATTERN);
                parameterizedLogRow = parameterizedLogRow.replace(variable, now.format(dateTimeFormatter));
            } else {
                parameterizedLogRow = parameterizedLogRow.replace(variable, CONTENT_MAP.get(variable));
            }
        }
        return parameterizedLogRow;
    }
}
