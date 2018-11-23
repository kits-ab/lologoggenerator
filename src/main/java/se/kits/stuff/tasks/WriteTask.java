package se.kits.stuff.tasks;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class WriteTask implements Runnable {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(WriteTask.class);
    private Logger customLogger;

    public WriteTask(Logger logger) {
        this.customLogger = logger;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                customLogger.info("just another log row: {}", i);
                Thread.sleep(1000);
            }
            customLogger.detachAndStopAllAppenders();
        } catch (InterruptedException e) {
            LOGGER.error("Sleep thread error: {}", e.toString());
        }
    }
}
