package se.kits.stuff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String APPLOLOGOG = "/app/lologog/";
    private static final String FILE_NAME = "lologog.txt";
    private static final String LOGFILEPATH = APPLOLOGOG + FILE_NAME;
    private static Logger LOGGER = LoggerFactory.getLogger(FileController.class);

    @POST
    @Path("/file/{filename}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response writeToFile(@PathParam("filename") String filename, Text text) {
        String filepath = APPLOLOGOG + filename;
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
        String filepath = APPLOLOGOG + name;
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

