package my.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.edc.spi.monitor.Monitor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


@Consumes({MediaType.APPLICATION_JSON})// La clase solo puede recibir peticiones de tipo application/json
@Produces({MediaType.APPLICATION_JSON})// La clase solo puede devolver peticiones de tipo application/json
@Path("/")
public class LoggerController {

    private final Monitor monitor; // Es el logger, se lo pasa la clase Extension 

    public LoggerController(Monitor monitor) {
        this.monitor = monitor;
    }

    @POST // Para obtener peticiones POST, tambien pueden ser GET, PUT, PATCH... 
    @Path("logger")
    public String  endppoint(String body) {// Creamos un endpoint que recibe un JSON como cadena de texto
        ObjectMapper mapper;
        JsonNode jsonNode;
        try {
            mapper = new ObjectMapper();
            jsonNode = mapper.readTree(body);//Aquí creamos los nodos del JSON
            //Ahora le quitamos las comillas a los campos del JSON que queremos utilizar, ya que sino
            //hiciesemos esto los valores con las comillas no serían utilies
            String endpoint=jsonNode.get("endpoint").toString().replaceAll("\"","");
            String authCode = jsonNode.get("authCode").toString().replaceAll("\"","");
            String id = jsonNode.get("id").toString().replaceAll("\"","");
            // Utilizamos el monitos para mostrar mensajes informativos
            monitor.info("El valor del auth code es: " + authCode);
            // Accedemos al endpoint que nos ha proporcionado el proveedor en el JSON con el authcode como cabecera de la petición, aceptando tanto input como output
            URL url = new URL(endpoint);
            System.out.println(url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Length", "" + authCode.getBytes().length);
            con.setRequestProperty("Authorization", authCode);

            try{
                DataInputStream rd = new DataInputStream(con.getInputStream()); // Aquí utilizo un DataInputStream para poder leer datos binarios

                OutputStream fos = new FileOutputStream("./"+id); // Creo un OutputStream (Que tambien admite binarios) que cree un fichero con el id de la transferencia como nombre 
                fos.write(rd.readAllBytes()); // Leemos la fuente de datos externa y la escribimos en el fichero interno
                fos.flush();

            }catch (Exception ignored){
                monitor.severe("Excepción leyendo los datos o escribiendolos", ignored);
            }

        } catch (JsonProcessingException e) {
            monitor.severe("El body no era un JSON con el formato correcto");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Si ha llegado hasta aquí y no ha saltado excepción devuelvo que ha sido correcto
        return "{\"respuesta\":\"descarga correcta\"}";
    }
}