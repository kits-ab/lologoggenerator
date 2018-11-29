package se.kits.stuff;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import se.kits.stuff.model.LogFileDefinition;

import javax.enterprise.context.RequestScoped;
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
@RequestScoped
public class Settings implements Serializable {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Settings.class);
    private String fileName;
    private String logPattern;
    private int timeSkewSeconds;
    private int frequencyPerMinute;

    private static final String APPLOLOGOG_DIR = "/app/lologog/";
    private static final String CONFIG_FILENAME = "logfile_config.json";
    private static final String CONFIG_FILEPATH = APPLOLOGOG_DIR + CONFIG_FILENAME;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void writeNewLogFileConfig() {
        LogFileDefinition logFileDefinition = toLogFileDefinition();
        LOGGER.info("New config to file: {}", logFileDefinition);
        List<LogFileDefinition> logFileDefinitions = new ArrayList<>();
        logFileDefinitions.add(logFileDefinition);
        writeNewConfigToFile(logFileDefinitions);
        LOGGER.info("config file overwritten: {}", CONFIG_FILENAME);
    }

    public void addConfigToFile() {
        LOGGER.info("add to config clicked");
        ArrayList<LogFileDefinition> logFileDefinitions = readConfigsFromFile();
        LogFileDefinition logFileDefinition = toLogFileDefinition();
        if (logFileDefinitions != null) {
            logFileDefinitions.add(logFileDefinition);
            writeNewConfigToFile(logFileDefinitions);
        } else {
            writeNewConfigToFile(Collections.singletonList(logFileDefinition));
        }
        LOGGER.info("config added to file: {}", CONFIG_FILENAME);
    }

    private LogFileDefinition toLogFileDefinition() {
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
}
