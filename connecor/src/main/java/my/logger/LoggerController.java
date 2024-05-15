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
import java.nio.charset.StandardCharsets;


@Consumes({MediaType.WILDCARD})
@Produces({MediaType.WILDCARD})
@Path("/")
public class LoggerController {

    private final Monitor monitor;

    public LoggerController(Monitor monitor) {
        this.monitor = monitor;
    }

    @POST
    @Path("logger")
    public String endppoint(String body) {
        ObjectMapper mapper;
        JsonNode jsonNode;
        StringBuilder datos = new StringBuilder();
        String aux="";
        try {
            mapper = new ObjectMapper();
            jsonNode = mapper.readTree(body);
            String endpoint=jsonNode.get("endpoint").toString().replaceAll("\"","");
            String authCode = jsonNode.get("authCode").toString().replaceAll("\"","");
            String id = jsonNode.get("id").toString().replaceAll("\"","");
            monitor.info("El valor del auth code es: " + authCode);


            URL url = new URL(endpoint);
            System.out.println(url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("GET");

            con.setRequestProperty("Content-Length", "" + authCode.getBytes().length);
            con.setRequestProperty("Authorization", authCode);
            try{
                DataInputStream rd = new DataInputStream(con.getInputStream());

                OutputStream fos = new FileOutputStream("./"+id);
                fos.write(rd.readAllBytes());
                fos.flush();

            }catch (Exception ignored){

            }

        } catch (JsonProcessingException e) {
            monitor.info("El code es: " +body);
            monitor.severe("El body no era un JSON con el formato correcto");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "{\"response\":\"download correctly\"}";
    }
}