package se.kits.stuff.model;

import se.kits.stuff.model.qualifiers.LogPreset;

import javax.enterprise.inject.Produces;
import java.util.LinkedHashMap;
import java.util.UUID;

public class Presets {

    private static final String CUSTOM_LOG_PATTERN_VALUE = UUID.randomUUID().toString();

    @Produces
    @LogPreset
    private LinkedHashMap<LogPatternPresetKey, String> buildLogPatternPresets() {
        LinkedHashMap<LogPatternPresetKey, String> presetMap = new LinkedHashMap<>();
        presetMap.put(LogPatternPresetKey.WILDFLY, "%-24date{\"yyyy-MM-dd HH:mm:ss,SSS\"} %-5level [%logger] \\(%thread\\) WFLYSOMETHING0017: %msg%n");
        presetMap.put(LogPatternPresetKey.SPRING, "%-25date{yyyy-MM-dd HH:mm:ss.SSS} %-5level 99999 --- [%30.30thread] %-40logger : %msg%n");
        presetMap.put(LogPatternPresetKey.LOGSTASH_ENCODER, "{\"@timestamp\": \"%date{yyyy-MM-dd}T%date{HH:mm:ss.SSS}\", \"@version\": int, \"message\": %msg, \"logger_name\": %logger, \"thread_name\": %thread \"level\": %-5level, \"level_value\": %level, \"stack_trace\": \"string\", \"X-Span-Id\": \"string\"}%n");
        presetMap.put(LogPatternPresetKey.WEB_ACCESS_LOG, "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$http_referer\" \"$http_user_agent\"");
        presetMap.put(LogPatternPresetKey.CUSTOM_PATTERN, CUSTOM_LOG_PATTERN_VALUE);
        return presetMap;
    }
}
