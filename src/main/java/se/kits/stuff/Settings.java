package se.kits.stuff;

import ch.qos.logback.classic.Logger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;
import se.kits.stuff.model.LogFileDefinition;
import se.kits.stuff.model.LogPatternPresetKey;
import se.kits.stuff.model.WebAccessLogGeneratorProfile;
import se.kits.stuff.model.qualifiers.LogPreset;
import se.kits.stuff.tasks.GenerateLogTask;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Named
@ApplicationScoped
public class Settings implements Serializable {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(Settings.class);
    private boolean running = false;
    private String logFileDefinitionId;
    private String fileName;
    private String logPattern;
    private LogPatternPresetKey logPatternPreSetSelection;
    private int timeSkewSeconds;
    private int frequencyPerMinute;
    private LogFileDefinition logFileDefinitionToEdit;
    private List<LogfileDefinitionHolder> holders = createLogDefinitionHoldersFromConfigFile();
    private String actionFeedback;
    private String color;

    private static final String APPLOLOGOG_CONFIG_DIR = "/app/lologog/config/";
    private static final String CONFIG_FILENAME = "logfile_config.json";
    private static final String CONFIG_FILEPATH = APPLOLOGOG_CONFIG_DIR + CONFIG_FILENAME;

    private ArrayList<GenerateLogTask> generateLogTasks = new ArrayList<>();

    private boolean disableFreeTextLogPattern = true;

    @Resource
    private ManagedThreadFactory managedThreadFactory;

    @Inject
    @LogPreset
    private LinkedHashMap<LogPatternPresetKey, String> logPatternPresets;

    private ConcurrentHashMap<GenerateLogTask, ConcurrentLinkedQueue<Map<String, String>>> queueTracker = new ConcurrentHashMap<>();

    public LinkedHashMap<LogPatternPresetKey, String> getLogPatternPresets() {
        return logPatternPresets;
    }

    public void start() {
        if (running) {
            LOGGER.info("Start button clicked when loggers are already running");
        } else {
            ArrayList<LogFileDefinition> logFileDefinitions = Utility.readConfigsFromFile(CONFIG_FILEPATH);
            if (logFileDefinitions != null) {
                for (LogFileDefinition logFileDefinition : logFileDefinitions) {
                    GenerateLogTask generateLogTask = new GenerateLogTask(logFileDefinition, queueTracker);
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
            queueTracker.clear();
            LOGGER.info("The log generation has been stopped.");
        } else {
            LOGGER.info("Stop button clicked when no loggers are running");
        }
    }

    public void writeNewLogFileConfig() {
        LogFileDefinition logFileDefinition = createNewLogFileDefinitionFromJsfViewInput();
        LOGGER.info("New config to file: {}", logFileDefinition);
        List<LogFileDefinition> logFileDefinitions = Collections.singletonList(logFileDefinition);
        updateLogFileDefinitionsInFileAndJsfView(logFileDefinitions, CONFIG_FILEPATH);
        WebAccessLogGeneratorProfile.writeWebAccessLogGeneratorProfileToFile(Collections.emptyMap());
        LOGGER.info("config file overwritten: {}", CONFIG_FILENAME);
        setFeedbackMessage("Wrote new config", FeedbackColor.GREEN);
    }

    public void addConfigToFile() {
        LOGGER.info("add to config clicked");
        LogFileDefinition logFileDefinition = createNewLogFileDefinitionFromJsfViewInput();
        List<LogFileDefinition> logFileDefinitions = Utility.readConfigsFromFile(CONFIG_FILEPATH);
        if (logFileDefinitions != null) {
            boolean hasConflict = logFileDefinitions.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(logFileDefinition1 -> logFileDefinition1.getFileName().equals(logFileDefinition.getFileName()));
            if (!hasConflict) {
                logFileDefinitions.add(logFileDefinition);
                updateLogFileDefinitionsInFileAndJsfView(logFileDefinitions, CONFIG_FILEPATH);
                LOGGER.info("config for {} added to file: {}", logFileDefinition.getLogPatternPreset(), CONFIG_FILENAME);
                setFeedbackMessage("Added log file definition to config file", FeedbackColor.GREEN);
            } else {
                LOGGER.info("Did not add logfile definition to config because of name conflict (fileName): {}", logFileDefinition.getFileName());
                setFeedbackMessage("Conflicting filename", FeedbackColor.RED);
            }
        } else {
            logFileDefinitions = Collections.singletonList(logFileDefinition);
            updateLogFileDefinitionsInFileAndJsfView(logFileDefinitions, CONFIG_FILEPATH);
            LOGGER.error("Config file not readable. Overwrote config file {} with new log file definition {}", CONFIG_FILEPATH, logFileDefinition);
            setFeedbackMessage("Config file not readable. Overwrote config file with new log file definition", FeedbackColor.RED);
        }
    }

    public String updateLogFileDefinition() {
        LogFileDefinition newLogFileDefinition = updateExistingLogFileDefinitionFromJsfViewInput();
        ArrayList<LogFileDefinition> logFileDefinitions = Utility.readConfigsFromFile(CONFIG_FILEPATH);
        if (logFileDefinitions != null) {
            List<LogFileDefinition> updatedList = logFileDefinitions.stream()
                    .filter(Objects::nonNull)
                    .map(logFileDefinition1 ->
                            logFileDefinition1.getId()
                                    .equals(logFileDefinitionToEdit.getId()) ? newLogFileDefinition : logFileDefinition1)
                    .collect(Collectors.toList());
            updateLogFileDefinitionsInFileAndJsfView(updatedList, CONFIG_FILEPATH);
            LOGGER.info("Updated logfile definition {}, in config {}", newLogFileDefinition, CONFIG_FILEPATH);
            setFeedbackMessage("Updated logfile definition", FeedbackColor.GREEN);
            clearFields();
            return "home?faces-redirect=true";
        } else {
            LOGGER.error("Failed to read config {}. Nothing updated.", CONFIG_FILEPATH);
            setFeedbackMessage("Failed to read config file. Try creating a new config.", FeedbackColor.RED);
            return null;
        }
    }

    public void deleteLogFileDefinition() {
        ArrayList<LogFileDefinition> logFileDefinitions = Utility.readConfigsFromFile(CONFIG_FILEPATH);
        if (logFileDefinitions != null) {
            List<LogFileDefinition> updatedLogfileDefinitionList = logFileDefinitions.stream()
                    .filter(logFileDefinition -> !logFileDefinition.getId().equals(logFileDefinitionToEdit.getId()))
                    .collect(Collectors.toList());
            updateLogFileDefinitionsInFileAndJsfView(updatedLogfileDefinitionList, CONFIG_FILEPATH);
            LOGGER.info("Deleted logfile definition: {}", logFileDefinitionToEdit);
            setFeedbackMessage("Deleted log file definition for " + logFileDefinitionToEdit.getFileName(), FeedbackColor.GREEN);
        } else {
            LOGGER.error("Failed to read config {}. Nothing deleted.", CONFIG_FILEPATH);
            setFeedbackMessage("Failed to read config file. Nothing was deleted", FeedbackColor.RED);
        }
    }

    public void deleteAllLogFileDefinitions() {
        updateLogFileDefinitionsInFileAndJsfView(Collections.emptyList(), CONFIG_FILEPATH);
        LOGGER.info("Cleared all log file definitions from config: {}", CONFIG_FILEPATH);
        setFeedbackMessage("Cleared all log file definitions from config.", FeedbackColor.GREEN);
    }

    private void updateLogFileDefinitionsInFileAndJsfView(List<LogFileDefinition> logFileDefinitions, String configFilepath) {
        Utility.writeConfigToFile(logFileDefinitions, configFilepath);
        this.holders = createLogDefinitionHolders(logFileDefinitions);
    }

    private void setFeedbackMessage(String actionFeedback, FeedbackColor feedbackColor) {
        this.actionFeedback = actionFeedback;
        this.color = feedbackColor.toString();
    }

    private enum FeedbackColor {
        GREEN,
        RED
    }

    private LogFileDefinition createNewLogFileDefinitionFromJsfViewInput() {
        return LogFileDefinition.builder()
                .id(UUID.randomUUID().toString())
                .fileName(this.fileName)
                .logPatternPreset(this.logPatternPreSetSelection)
                .logPattern(this.logPatternPreSetSelection.equals(LogPatternPresetKey.CUSTOM_PATTERN) ?
                        this.logPattern : logPatternPresets.get(this.logPatternPreSetSelection))
                .timeSkewSeconds(this.timeSkewSeconds)
                .frequencyPerMinute(this.frequencyPerMinute)
                .build();
    }

    private LogFileDefinition updateExistingLogFileDefinitionFromJsfViewInput() {
        return LogFileDefinition.builder()
                .id(this.logFileDefinitionId)
                .fileName(this.fileName)
                .logPatternPreset(this.logPatternPreSetSelection)
                .logPattern(this.logPatternPreSetSelection.equals(LogPatternPresetKey.CUSTOM_PATTERN) ?
                        this.logPattern : logPatternPresets.get(this.logPatternPreSetSelection))
                .timeSkewSeconds(this.timeSkewSeconds)
                .frequencyPerMinute(this.frequencyPerMinute)
                .build();
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

    public void logPatternPresetSelectionChanged() {
        this.disableFreeTextLogPattern = !logPatternPreSetSelection.equals(LogPatternPresetKey.CUSTOM_PATTERN);
    }

    public Set<LogPatternPresetKey> getPresetKeySet() {
        return logPatternPresets.keySet();
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

    public LogPatternPresetKey getLogPatternPreSetSelection() {
        return logPatternPreSetSelection;
    }

    public void setLogPatternPreSetSelection(LogPatternPresetKey logPatternPreSetSelection) {
        this.logPatternPreSetSelection = logPatternPreSetSelection;
    }

    public boolean isDisableFreeTextLogPattern() {
        return disableFreeTextLogPattern;
    }

    public List<LogfileDefinitionHolder> getHolders() {
        return holders;
    }

    public void setHolders(List<LogfileDefinitionHolder> holders) {
        this.holders = holders;
    }

    public LogFileDefinition getLogFileDefinitionToEdit() {
        return logFileDefinitionToEdit;
    }

    public void setLogFileDefinitionToEdit(LogFileDefinition logFileDefinitionToEdit) {
        this.logFileDefinitionToEdit = logFileDefinitionToEdit;
    }

    public String getLogFileDefinitionId() {
        return logFileDefinitionId;
    }

    public void setLogFileDefinitionId(String logFileDefinitionId) {
        this.logFileDefinitionId = logFileDefinitionId;
    }

    private List<LogfileDefinitionHolder> createLogDefinitionHoldersFromConfigFile() {
        ArrayList<LogFileDefinition> logFileDefinitions = Utility.readConfigsFromFile(CONFIG_FILEPATH);
        if (logFileDefinitions != null) {
            return createLogDefinitionHolders(logFileDefinitions);
        }
        return Collections.emptyList();
    }

    private List<LogfileDefinitionHolder> createLogDefinitionHolders(List<LogFileDefinition> logFileDefinitions) {
        if (!logFileDefinitions.isEmpty()) {
            return logFileDefinitions.stream()
                    .filter(Objects::nonNull)
                    .map(logFileDefinition1 ->
                            new LogfileDefinitionHolder(
                                    logFileDefinition1.getFileName() + " / " + logFileDefinition1.getLogPatternPreset().toString(),
                                    logFileDefinition1))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public String goToEdit() {
        setFieldsForEdit();
        return "edit";
    }

    public String goToHome() {
        clearFields();
        this.actionFeedback = "";
        return "home";
    }

    private void setFieldsForEdit() {
        this.logFileDefinitionId = logFileDefinitionToEdit.getId();
        this.fileName = logFileDefinitionToEdit.getFileName();
        this.logPattern = logFileDefinitionToEdit.getLogPattern();
        this.logPatternPreSetSelection = logFileDefinitionToEdit.getLogPatternPreset();
        this.timeSkewSeconds = (int) logFileDefinitionToEdit.getTimeSkewSeconds();
        this.frequencyPerMinute = (int) logFileDefinitionToEdit.getFrequencyPerMinute();
    }

    private void clearFields() {
        this.logFileDefinitionId = null;
        this.fileName = null;
        this.logPatternPreSetSelection = null;
        this.logPattern = null;
        this.timeSkewSeconds = 0;
        this.frequencyPerMinute = 0;
    }

    public String getActionFeedback() {
        return actionFeedback;
    }

    public String getColor() {
        return color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogfileDefinitionHolder {
        private String keyName;
        private LogFileDefinition logFileDefinition;
    }
}
