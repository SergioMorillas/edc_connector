package arsys.extension.download.push;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.edc.spi.monitor.Monitor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

@Consumes({ MediaType.APPLICATION_JSON }) // La clase solo puede recibir peticiones de tipo application/json
@Produces({ MediaType.APPLICATION_JSON }) // La clase solo puede devolver peticiones de tipo application/json
@Path("/")
public class PushController {

    private final Monitor monitor; // Es el logger, se lo pasa la clase Extension

    public PushController(Monitor monitor) {
        this.monitor = monitor;
    }

    @POST 
    @Path("push")
    public String endpoint(InputStream body) { // Creamos un endpoint que recibe un JSON como cadena de texto
        String response = "correct";   
        long empiezaTransferencia = System.currentTimeMillis();

        try {
            try (OutputStream fos = new FileOutputStream("./" + body.hashCode())) {
                fos.write(body.readAllBytes());
                fos.flush();
            }
            monitor.info("Transferencia terminada, ha tardado: " + (System.currentTimeMillis() - empiezaTransferencia ) + " ms");
        } catch (JsonProcessingException e) {
            monitor.severe("El body no era un JSON con el formato correcto" + Arrays.toString(e.getStackTrace()));
            response = "processing error";
        } catch (FileNotFoundException e) {
            response = "No se ha podido leer la conexion";
            throw new RuntimeException(e);
        } catch (IOException e) {
            response = "No se ha podido escribir en el disco";
            throw new RuntimeException(e);
        } finally {
            response = "{\"Tiempo de transferencia\":\" "+(System.currentTimeMillis() - empiezaTransferencia )+" \" ms\"\"}";
        }

        return """
        {
            "response":""" + response + """
        }""";
    }
}