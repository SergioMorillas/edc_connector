package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.spi.monitor.Monitor;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


@Consumes({MediaType.APPLICATION_JSON}) // La clase solo puede recibir peticiones de tipo application/json
@Produces({MediaType.APPLICATION_JSON}) // La clase solo puede devolver peticiones de tipo application/json
@Path("/")
public class DownloadController {

    private final Monitor monitor; // Es el logger, se lo pasa la clase Extension 

    public DownloadController(Monitor monitor) {
        this.monitor = monitor;
    }

    @POST
    @Path("endpoint")
    public String endppoint(String body) { // Creamos un endpoint que recibe un JSON como cadena de texto
        ObjectMapper mapper;
        JsonNode jsonNode = null;

        try {
            mapper = new ObjectMapper();
            jsonNode = mapper.readTree(body); //Aquí creamos los nodos del JSON
            try (OutputStream fos = new FileOutputStream("./"+jsonNode.fieldNames().next()+".json")) {
                fos.write(body.getBytes(StandardCharsets.UTF_8));
            }

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