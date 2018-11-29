package se.kits.stuff;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import se.kits.stuff.model.LogFileDefinition;
import se.kits.stuff.tasks.GenerateLogTask;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Named
@ApplicationScoped
public class Settings implements Serializable {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Settings.class);
    private boolean running = false;
    private String fileName;
    private String logPattern;
    private int timeSkewSeconds;
    private int frequencyPerMinute;

    private static final String APPLOLOGOG_DIR = "/app/lologog/";
    private static final String CONFIG_FILENAME = "logfile_config.json";
    private static final String CONFIG_FILEPATH = APPLOLOGOG_DIR + CONFIG_FILENAME;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ArrayList<GenerateLogTask> generateLogTasks = new ArrayList<>();

    @Resource
    private ManagedThreadFactory managedThreadFactory;

    public void start() {
        if (running) {
            LOGGER.info("Start button clicked when loggers are already running");
        } else {
            ArrayList<LogFileDefinition> logFileDefinitions = readConfigsFromFile();
            if (logFileDefinitions != null) {
                for (LogFileDefinition logFileDefinition : logFileDefinitions) {
                    GenerateLogTask generateLogTask = new GenerateLogTask(logFileDefinition);
                    Thread threadForLogFileDefinition = managedThreadFactory.newThread(generateLogTask);
                    threadForLogFileDefinition.start();
                    this.generateLogTasks.add(generateLogTask);
                }
                running = true;
                LOGGER.info("Log generation has started.");
            } else {
                LOGGER.info("No valid config. No threads should be logging");
            }
        }
    }

    public void stop() {
        if (running) {
            running = false;
            for (GenerateLogTask generateLogTask : this.generateLogTasks) {
                generateLogTask.setRunning(running);
            }
            LOGGER.info("The log generation has been stopped.");
        } else {
            LOGGER.info("Stop button clicked when no loggers are running");
        }
    }

    public void writeNewLogFileConfig() {
        LogFileDefinition logFileDefinition = jsfViewInputToLogFileDefinition();
        LOGGER.info("New config to file: {}", logFileDefinition);
        List<LogFileDefinition> logFileDefinitions = new ArrayList<>();
        logFileDefinitions.add(logFileDefinition);
        writeNewConfigToFile(logFileDefinitions);
        LOGGER.info("config file overwritten: {}", CONFIG_FILENAME);
    }

    public void addConfigToFile() {
        LOGGER.info("add to config clicked");
        ArrayList<LogFileDefinition> logFileDefinitions = readConfigsFromFile();
        LogFileDefinition logFileDefinition = jsfViewInputToLogFileDefinition();
        if (logFileDefinitions != null) {
            logFileDefinitions.add(logFileDefinition);
            writeNewConfigToFile(logFileDefinitions);
        } else {
            writeNewConfigToFile(Collections.singletonList(logFileDefinition));
        }
        LOGGER.info("config added to file: {}", CONFIG_FILENAME);
    }

    private LogFileDefinition jsfViewInputToLogFileDefinition() {
        return LogFileDefinition.builder()
                .fileName(this.fileName)
                .logPattern(this.logPattern)
                .timeSkewSeconds(this.timeSkewSeconds)
                .frequencyPerMinute(this.frequencyPerMinute)
                .build();
    }

    private void writeNewConfigToFile(List<LogFileDefinition> logFileDefinitions) {
        try {
            Files.write(Paths.get(CONFIG_FILEPATH), OBJECT_MAPPER.writeValueAsString(logFileDefinitions).getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to write to file: {} - {}", CONFIG_FILENAME, e.toString());
        }
    }

    private ArrayList<LogFileDefinition> readConfigsFromFile() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(CONFIG_FILEPATH));
            LogFileDefinition[] logFileDefinitionsArray = OBJECT_MAPPER.readValue(bytes, LogFileDefinition[].class);
            return new ArrayList<>(Arrays.asList(logFileDefinitionsArray));
        } catch (IOException e) {
            LOGGER.error("IO Error, failed to read {}", CONFIG_FILEPATH);
            return null;
        }
    }

    public void validateFrequencyRange(FacesContext context, UIComponent toValidate, Object value) {
        double inputFrequency = (double) ((Integer) value);
        if (inputFrequency <= 0) {
            FacesMessage message = new FacesMessage("Frequency must be greater than 0.");
            context.addMessage(toValidate.getClientId(context), message);
            ((UIInput) toValidate).setValid(false);
            LOGGER.info("JSF Validator for Frequency Failed for input: {}", inputFrequency);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = logPattern;
    }

    public int getTimeSkewSeconds() {
        return timeSkewSeconds;
    }

    public void setTimeSkewSeconds(int timeSkewSeconds) {
        this.timeSkewSeconds = timeSkewSeconds;
    }

    public int getFrequencyPerMinute() {
        return frequencyPerMinute;
    }

    public void setFrequencyPerMinute(int frequencyPerMinute) {
        this.frequencyPerMinute = frequencyPerMinute;
    }

    public boolean isRunning() {
        return running;
    }
}
