package se.kits.stuff.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;
import se.kits.stuff.SpringLogGenerator;
import se.kits.stuff.WebAccessLogGenerator;
import se.kits.stuff.model.LogFileDefinition;
import se.kits.stuff.model.LogPatternPresetKey;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GenerateLogTask implements Runnable {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(GenerateLogTask.class);
    private static final String APPENDER_SUFFIX = "-appender";
    private static final String MESSAGE_NEWLINE_PATTERN = "%msg%n";
    private static final String LOGGER_SUFFIX = "-logger";
    private LogFileDefinition logFileDefinition;
    private static final String APPLOLOGOG_DIR = "/app/lologog/";

    private boolean running;
    private ConcurrentHashMap<GenerateLogTask, ConcurrentLinkedQueue<Map<String, String>>> queueTracker;
    private ConcurrentLinkedQueue<Map<String, String>> replacerQueue = new ConcurrentLinkedQueue<>();

    public GenerateLogTask() {
    }

    public GenerateLogTask(LogFileDefinition logFileDefinition,
                           ConcurrentHashMap<GenerateLogTask, ConcurrentLinkedQueue<Map<String, String>>> queueTracker) {
        this.logFileDefinition = logFileDefinition;
        this.running = true;
        this.queueTracker = queueTracker;
        this.queueTracker.put(this, replacerQueue);
    }

    private String produceLogRowMessage(LogFileDefinition logFileDefinition) {
        switch (logFileDefinition.getLogPatternPreset()) {
            case WEB_ACCESS_LOG:
                ConcurrentLinkedQueue<Map<String, String>> replacerQueue = queueTracker.get(this);
                Map<String, String> replacements = replacerQueue.isEmpty() ? Collections.emptyMap() : replacerQueue.poll();
                return WebAccessLogGenerator.replaceVariablesInPattern(logFileDefinition.getLogPattern(), replacements);
            case LOGSTASH_ENCODER:
                return logFileDefinition.getLogPattern();
            case SPRING:
                SpringLogGenerator.LoglevelMessageStatusCodeConnection queuedLoglevelMessageStatusCodeConnection = SpringLogGenerator.generateLogEventForQueue();
                addHttpStatusToQueue(queuedLoglevelMessageStatusCodeConnection);
                return SpringLogGenerator.replaceVariablesInPattern(logFileDefinition.getLogPattern(), queuedLoglevelMessageStatusCodeConnection);
            case WILDFLY:
            case CUSTOM_PATTERN:
                return "An informative logger message...";
            default:
                LOGGER.error("No matching log pattern preset keys!");
                break;
        }
        return null;
    }

    private void addHttpStatusToQueue(SpringLogGenerator.LoglevelMessageStatusCodeConnection queuedLoglevelMessageStatusCodeConnection) {
        Map<String, String> queuedStatusToWebLogMap = WebAccessLogGenerator.generateStatusCodeForQueue(queuedLoglevelMessageStatusCodeConnection);
        GenerateLogTask webLogTask = getWebLogTask();
        if (webLogTask != null && !queuedStatusToWebLogMap.isEmpty()) {
            queueTracker.get(webLogTask).offer(queuedStatusToWebLogMap);
            LOGGER.info("Add web log status to queue: {}", queuedStatusToWebLogMap);
        }
    }

    private GenerateLogTask getWebLogTask() {
        return queueTracker.keySet().stream()
                .filter(generateLogTask -> generateLogTask.getLogFileDefinition().getLogPatternPreset().equals(LogPatternPresetKey.WEB_ACCESS_LOG))
                .findFirst()
                .orElse(null);
    }

    private void outputOneLogLine(Logger logger, LogFileDefinition logFileDefinition) {
        String answer = produceLogRowMessage(logFileDefinition);
        logger.info(answer);
    }

    @Override
    public void run() {
        Logger customLogger = createFileLogger(this.logFileDefinition);
        try {
            Thread.sleep((long) (1000 * this.logFileDefinition.getTimeSkewSeconds()));
            while (this.running) {

                outputOneLogLine(customLogger, this.logFileDefinition);

                double frequencyPerMinute = this.logFileDefinition.getFrequencyPerMinute();
                if (frequencyPerMinute > 0) {
                    double v = 60.0 / frequencyPerMinute;
                    long delayMillis = (long) (v * 1000);
                    Thread.sleep(delayMillis);
                }
            }
            customLogger.detachAndStopAllAppenders();
        } catch (InterruptedException e) {
            LOGGER.error("thread sleep error: {}", e.toString());
        }
    }

    private static boolean useLogbackSyntax(LogFileDefinition logFileDefinition) {
        switch (logFileDefinition.getLogPatternPreset()) {
            case WILDFLY:
            case CUSTOM_PATTERN:
                return true;
            case SPRING:
            case LOGSTASH_ENCODER:
            case WEB_ACCESS_LOG:
            default:
                return false;
        }
    }

    private static Logger createFileLogger(LogFileDefinition logFileDefinition) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(loggerContext);
        String tempPatternOfChoice = useLogbackSyntax(logFileDefinition) ? logFileDefinition.getLogPattern() : MESSAGE_NEWLINE_PATTERN;
//        patternLayoutEncoder.setPattern(logFileDefinition.getLogPattern());
        patternLayoutEncoder.setPattern(tempPatternOfChoice);
        patternLayoutEncoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setName(logFileDefinition.getFileName() + APPENDER_SUFFIX);
        fileAppender.setFile(APPLOLOGOG_DIR + logFileDefinition.getFileName());
        fileAppender.setEncoder(patternLayoutEncoder);
        fileAppender.setContext(loggerContext);
        fileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(logFileDefinition.getFileName() + LOGGER_SUFFIX);
        logger.addAppender(fileAppender);
        logger.setLevel(Level.ALL);
//        logger.setAdditive(false);

        return logger;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public LogFileDefinition getLogFileDefinition() {
        return logFileDefinition;
    }
}
