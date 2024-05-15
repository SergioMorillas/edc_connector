package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpContext;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;


@Consumes({MediaType.WILDCARD})
@Produces({MediaType.WILDCARD})
@Path("/")
public class DownloadController {

    private final Monitor monitor;

    public DownloadController(Monitor monitor) {
        this.monitor = monitor;
    }

    @POST
    @Path("endpoint")
    public String endppoint(String body) {
        ObjectMapper mapper;
        JsonNode jsonNode = null;

        try {
            mapper = new ObjectMapper();
            jsonNode = mapper.readTree(body);
            OutputStream fos = new FileOutputStream("./"+jsonNode.fieldNames().next()+".json");
            fos.write(body.getBytes(StandardCharsets.UTF_8));

        } catch (JsonProcessingException e) {
            monitor.severe("El body no era un JSON con el formato correcto" + Arrays.toString(e.getStackTrace()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "{\"response\":\"download correctly\"}";
    }
}