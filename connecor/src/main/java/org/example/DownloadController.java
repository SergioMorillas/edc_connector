package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.spi.monitor.Monitor;
import java.io.*;
import java.util.Arrays;


@Consumes({MediaType.WILDCARD}) // La clase solo puede recibir peticiones de tipo application/json
@Produces({MediaType.WILDCARD}) // La clase solo puede devolver peticiones de tipo application/json
@Path("/")
public class DownloadController {

    private final Monitor monitor; // Es el logger, se lo pasa la clase Extension 

    public DownloadController(Monitor monitor) {
        this.monitor = monitor;
    }

    @POST
    @Path("endpoint")
    public String endppoint(InputStream body) { // Creamos un endpoint que recibe un JSON como cadena de texto

        try {
            try (OutputStream fos = new FileOutputStream("./"+body.hashCode())) {
                fos.write(body.readAllBytes());
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