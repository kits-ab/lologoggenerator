package se.kits.stuff.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;
import se.kits.stuff.model.LogFileDefinition;

public class WriteTask implements Runnable {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(WriteTask.class);
    private static final String FILE_LOGGER_1 = "fileLogger1";
    private LogFileDefinition logFileDefinition;
    private static final String APPLOLOGOG_DIR = "/app/lologog/";
    private static final String FILEAPPENDER_1 = "fileappender1";

    public WriteTask(LogFileDefinition logFileDefinition) {
        this.logFileDefinition = logFileDefinition;
    }

    @Override
    public void run() {
        Logger customLogger = createFileLogger(this.logFileDefinition);
        try {
            for (int i = 0; i < 20; i++) {
                customLogger.info("just another log row: {}", i);

                double frequencyPerMinute = logFileDefinition.getFrequencyPerMinute();
                if (frequencyPerMinute > 0) {
                    double v = 60.0 / frequencyPerMinute;
                    long delay = (long) (v * 1000);
                    Thread.sleep(delay);
                }
            }
            customLogger.detachAndStopAllAppenders();
        } catch (InterruptedException e) {
            LOGGER.error("Sleep thread error: {}", e.toString());
        }
    }

    private static Logger createFileLogger(LogFileDefinition logFileDefinition) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.setPattern(logFileDefinition.getLogPattern());
//        patternLayoutEncoder.setPattern("%date{ISO8601} [%thread] %-5level %logger{36} - %msg%n");
        patternLayoutEncoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setName(FILEAPPENDER_1);
        fileAppender.setFile(APPLOLOGOG_DIR + logFileDefinition.getFileName());
        fileAppender.setEncoder(patternLayoutEncoder);
        fileAppender.setContext(loggerContext);
        fileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(FILE_LOGGER_1);
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
//        logger.setAdditive(false);

        return logger;
    }
}
