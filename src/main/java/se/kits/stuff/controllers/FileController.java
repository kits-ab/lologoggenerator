package se.kits.stuff.controllers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;
import se.kits.stuff.model.LogFileDefinition;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Path("")
public class FileController {

    private static final String APPLOLOGOG_DIR = "/app/lologog/";
    private static final String CONFIG_FILENAME = "logfile.config";
    private static final String CONFIG_FILEPATH = APPLOLOGOG_DIR + CONFIG_FILENAME;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FileController.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @POST
    @Path("/logfiledefinition")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createLogFileDefinition(LogFileDefinition logFileDefinition) {
        try {
            Files.write(Paths.get(CONFIG_FILEPATH), OBJECT_MAPPER.writeValueAsString(logFileDefinition).getBytes());
            LOGGER.info("Config file written to {}", CONFIG_FILEPATH);
            return Response.ok().build();
        } catch (JsonProcessingException jpe) {
            LOGGER.error("Failed at objectmapping: {}", jpe.toString());
            return Response.serverError().build();
        } catch (IOException ioe) {
            LOGGER.error("Failed to write to file: {}", ioe.toString());
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/logfiledefinition")
    @Produces(MediaType.APPLICATION_JSON)
    public Response returnLogFileDefinition() {
        try {
            LogFileDefinition logFileDefinition = getLogFileDefinition();
            return Response.ok(logFileDefinition).build();
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.toString());
            return Response.ok().build();
        }
    }

    private LogFileDefinition getLogFileDefinition() throws IOException {
        return OBJECT_MAPPER.readValue(Files.readAllBytes(Paths.get(CONFIG_FILEPATH)), LogFileDefinition.class);
    }

    @GET
    @Path("/logfile/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startLogGen() {
        try {
            LogFileDefinition logFileDefinition = getLogFileDefinition();
            Logger customLogger = createFileLogger(logFileDefinition);
            customLogger.info("this is a message to the log: {}", logFileDefinition.getLogPattern());
            return Response.ok().build();
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.toString());
            return Response.serverError().build();
        }
    }

    private static Logger createFileLogger(LogFileDefinition logFileDefinition) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(loggerContext);
        patternLayoutEncoder.setPattern("%date{ISO8601} [%thread] %-5level %logger{36} - %msg%n");
        patternLayoutEncoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setFile(APPLOLOGOG_DIR + logFileDefinition.getFileName());
        fileAppender.setEncoder(patternLayoutEncoder);
        fileAppender.setContext(loggerContext);
        fileAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger("fileLogger1");
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
//        logger.setAdditive(false);

        return logger;
    }

    @POST
    @Path("/file/{filename}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response writeToFile(@PathParam("filename") String filename, Text text) {
        String filepath = APPLOLOGOG_DIR + filename;
        try {
            java.nio.file.Path path = Files.write(Paths.get(filepath), text.getText().getBytes());
            LOGGER.info("Input written to file: {}", path.toString());
            return Response.ok().build();
        } catch (IOException e) {
            LOGGER.error("File write failed: {}", filepath);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/file/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response readTheFile(@PathParam("name") String name) {
        List<String> lines = null;
        String filepath = APPLOLOGOG_DIR + name;
        try {
            lines = Files.readAllLines(Paths.get(filepath), Charset.defaultCharset());
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line);
            }
            LOGGER.info("Return file data from: {}", filepath);
            return Response.ok(new Text(sb.toString())).build();
        } catch (IOException e) {
            LOGGER.error("File read failed: {}", filepath);
            return Response.serverError().build();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class Text {
        private String text;
    }
}

