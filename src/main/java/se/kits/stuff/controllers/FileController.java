package se.kits.stuff.controllers;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;
import se.kits.stuff.model.LogFileDefinition;
import se.kits.stuff.tasks.WriteTask;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedThreadFactory;
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

@Stateless
@Path("")
public class FileController {

    private static final String APPLOLOGOG_DIR = "/app/lologog/";
    private static final String CONFIG_FILENAME = "logfile.config";
    private static final String CONFIG_FILEPATH = APPLOLOGOG_DIR + CONFIG_FILENAME;
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(FileController.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String LOGFILE_START_PATH = "/logfile/start";
    private static final String LOGFILE_DEFINITION_PATH = "/logfiledefinition";

    @Resource
    private ManagedThreadFactory managedThreadFactory;

    @POST
    @Path(LOGFILE_DEFINITION_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createLogFileDefinition(LogFileDefinition logFileDefinition) {
        LOGGER.info("POST {}", LOGFILE_DEFINITION_PATH);
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
        LOGGER.info("GET {}", LOGFILE_DEFINITION_PATH);
        LogFileDefinition logFileDefinition = getLogFileDefinition();
        return logFileDefinition == null ? Response.serverError().build() : Response.ok(logFileDefinition).build();
    }

    @GET
    @Path(LOGFILE_START_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startLogGen() {
        LOGGER.info("GET {}", LOGFILE_START_PATH);
        LogFileDefinition logFileDefinition = getLogFileDefinition();
        if (logFileDefinition != null) {
            writeLogInSeparateThread(logFileDefinition);
            LOGGER.info("Generating logs to: {}", APPLOLOGOG_DIR + logFileDefinition.getFileName());
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
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

    private static LogFileDefinition getLogFileDefinition() {
        try {
            return OBJECT_MAPPER.readValue(Files.readAllBytes(Paths.get(CONFIG_FILEPATH)), LogFileDefinition.class);
        } catch (IOException e) {
            LOGGER.error("IO Error, failed to read {}", CONFIG_FILEPATH);
            return null;
        }
    }

    private void writeLogInSeparateThread(LogFileDefinition logFileDefinition) {
        WriteTask writeTask = new WriteTask(logFileDefinition);
        Thread thread = managedThreadFactory.newThread(writeTask);
        thread.start();
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

