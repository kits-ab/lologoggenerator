package se.kits.stuff.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;
import se.kits.stuff.model.LogFileDefinition;

import java.time.Instant;

public class GenerateLogTask implements Runnable {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(WriteTask.class);
    private static final String APPENDER_SUFFIX = "-appender";
    private LogFileDefinition logFileDefinition;
    private static final String APPLOLOGOG_DIR = "/app/lologog/";

    private boolean running;

    public GenerateLogTask(LogFileDefinition logFileDefinition) {
        this.logFileDefinition = logFileDefinition;
        this.running = true;
    }

    @Override
    public void run() {
        Logger customLogger = createFileLogger(this.logFileDefinition);
        try {
            Thread.sleep((long) (1000 * logFileDefinition.getTimeSkewSeconds()));
            while (this.running) {
                customLogger.info("Logrow message {}", Instant.now().toString());
                double frequencyPerMinute = logFileDefinition.getFrequencyPerMinute();
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

    private static Logger createFileLogger(LogFileDefinition logFileDefinition) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.setPattern(logFileDefinition.getLogPattern());
        patternLayoutEncoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setName(logFileDefinition.getFileName() + APPENDER_SUFFIX);
        fileAppender.setFile(APPLOLOGOG_DIR + logFileDefinition.getFileName());
        fileAppender.setEncoder(patternLayoutEncoder);
        fileAppender.setContext(loggerContext);
        fileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(logFileDefinition.getFileName());
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
//        logger.setAdditive(false);

        return logger;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}