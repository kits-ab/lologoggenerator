package se.kits.stuff.model;

import se.kits.stuff.model.qualifiers.CustomLogPattern;
import se.kits.stuff.model.qualifiers.LogPreset;

import javax.enterprise.inject.Produces;
import java.util.LinkedHashMap;

public class Presets {

    @Produces
    @CustomLogPattern
    private static final String CUSTOM_LOG_PATTERN_STRING = "CustomLogPattern";

    @Produces
    @LogPreset
    private LinkedHashMap<String, String> buildLogPatternPresets() {
        LinkedHashMap<String, String> presetMap = new LinkedHashMap<>();
        presetMap.put("Custom Log Pattern", CUSTOM_LOG_PATTERN_STRING);
        presetMap.put("Example Java EE format", "%date{ISO8601} [%thread] %-5level %logger{32} - %msg%n");
        presetMap.put("Logstash Encoder", "{\"@timestamp\": \"%date{yyyy-MM-dd}T%date{HH:mm:ss.SSS}\", \"@version\": int, \"message\": %msg, \"logger_name\": %logger, \"thread_name\": %thread \"level\": %-5level, \"level_value\": %level, \"stack_trace\": \"string\", \"X-Span-Id\": \"string\"}%n");
        return presetMap;
    }
}