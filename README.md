- [Requisitos](#requisitos)
- [Creación del conector](#creación-del-conector)
  - [Creación del proyecto gradle](#creación-del-proyecto-gradle)
    - [Añadimos las dependencias](#añadimos-las-dependencias)
    - [Compilación](#compilación)
    - [Lanzar nuestro conector](#lanzar-nuestro-conector)
      - [Ficheros de configuración](#ficheros-de-configuración)
        - [Configuración](#configuración)
        - [Logs](#logs)
    - [Certificados](#certificados)
    - [Script de la base de datos](#script-de-la-base-de-datos)
    - [Dockerfile](#dockerfile)
    - [Docker-compose.yml](#docker-composeyml)
- [Extensión propia](#extensión-propia)
- [Despliegue final](#despliegue-final)
- [Pruebas](#pruebas)
  - [Creación del dataplane](#creación-del-dataplane)
  - [Ofrecimiento de datos](#ofrecimiento-de-datos)
    - [Creación del asset](#creación-del-asset)
    - [Creación de la politica](#creación-de-la-politica)
    - [Creación del contrato](#creación-del-contrato)
    - [Solicitar el catalogo](#solicitar-el-catalogo)
    - [Negociar el contrato](#negociar-el-contrato)
    - [Comprobar el estado de la negociación](#comprobar-el-estado-de-la-negociación)
    - [Pull](#pull)
    - [Push](#push)
      - [Mi script](#mi-script)
- [Estructura de directorios completa al finalizar](#estructura-de-directorios-completa-al-finalizar)


# Requisitos

Todas las pruebas estan realizadas sobre una maquina virtual con **Ubuntu 22.04.3 LTS**, que es donde he realizado todas las pruebas. Para montar tu conector necesitas varias dependencias en el sistema, estas son las versiones que yo he utilizado y se que funcionan correctamente:
- **Gradle** versión 7.3.2
- **Docker** versión 25.0.2
- **Docker Compose** versión v2.24.5
- **Java**: OpenJDK 17.0.10+7-Ubuntu-122.04.1

# Creación del conector

## Creación del proyecto gradle

Toda la información para la creacion de este proyecto se encuentra en la siguiente [URL](https://docs.gradle.org/current/userguide/part1_gradle_init.html), pero se le han agregado pruebas y comprobaciones.

1. Lo primero que debemos hacer es ejecutar el comando ``gradle`` para iniciar la sesión del gestor de proyectos.
2. Ahora crearemos una carpeta y entraremos dentro, ``mkdir prueba; cd $_``
3. Una vez estemos dentro de la carpeta que acabamos de crear debemos crear el proyecto como tal, ``gradle init --type java-application``, con el cual crearemos un proyecto de tipo aplicación java. Tendremos que ir añadiendo parámetros que ira pidiendo por consola, podemos darle a enter y simplemente utilizar los valores por defecto.
4. A partir de este punto podemos utilizar el wrapper de gradle si nos es mas cómodo.


```bash
gradle
mkdir prueba; cd $_
gradle init --type java-application
```

### Añadimos las dependencias

Una vez que tenemos el proyecto creado necesitamos añadir las dependencias, para lo cual debemos añadirlas en el fichero **build.gradle.kts**, en caso de que no exista deberas añadirlo en la raíz del proyecto gradle, por lo que en caso de que no exista con el comando ``touch build.gradle.kts`` se debería crear, y deberías añadirle el siguiente contenido: 

```ktln
plugins { // Aquí añadimos los plugins
    `java-library` // El plugin base
    id("application") // El plugin que utilizaremos para setear la clase main
    id("com.github.johnrengelman.shadow") version "7.1.2" // El plugin shadow, que sirve para para crear el jar autoejecutable con todas las dependenciar
}

repositories { // El repositorio del que vamos a sacar todas las librerias
    mavenCentral()
}
val edcVersion = "0.6.1" // Seteamos la version de los paquetes que vamos a utilizar
val edcGroup = "org.eclipse.edc" // Seteamos el grupo de Eclipse que vamos a utilizar
dependencies {
    implementation("${edcGroup}:data-plane-selector-api:${edcVersion}")             // Todas las extensiones basicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-selector-core:${edcVersion}")            // Todas las extensiones basicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-control-api:${edcVersion}")              // Todas las extensiones basicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-public-api:${edcVersion}")               // Todas las extensiones basicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-core:${edcVersion}")                     // Todas las extensiones basicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-http:${edcVersion}")                     // Todas las extensiones basicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    
    implementation("${edcGroup}:control-plane-core:${edcVersion}")                  // Todas las extensiones basicas ==> Nucleo del control de transferencias
    implementation("${edcGroup}:control-plane-api:${edcVersion}")                   // Todas las extensiones basicas ==> Nucleo del control de transferencias
    implementation("${edcGroup}:control-plane-api-client:${edcVersion}")            // Todas las extensiones basicas ==> API para clientes del control de transferencias
    implementation("${edcGroup}:management-api:${edcVersion}")                      // Todas las extensiones basicas ==> API de administración
    implementation("${edcGroup}:dsp:${edcVersion}")                                 // Todas las extensiones basicas ==> Implementación del DataSpaceProtocol
    implementation("${edcGroup}:configuration-filesystem:${edcVersion}")            // Todas las extensiones basicas ==> Para mantener la configuración en local sin necesidad de Vault
    implementation("${edcGroup}:iam-mock:${edcVersion}")                            // Todas las extensiones basicas ==> Implementación del servicio de identificación
    implementation("${edcGroup}:http:${edcVersion}")                                // Todas las extensiones basicas ==> Control de comunicaciones HTTP
    implementation("${edcGroup}:transfer-data-plane:${edcVersion}")                 // Todas las extensiones basicas ==> Control de comunicaciones HTTP
    implementation("${edcGroup}:transfer-pull-http-receiver:${edcVersion}")         // Todas las extensiones basicas ==> Control de comunicaciones HTTP

    implementation("${edcGroup}:asset-index-sql:${edcVersion}")                     // Todas las extensiones de SQL -==> Indice de assets
    implementation("${edcGroup}:policy-definition-store-sql:${edcVersion}")         // Todas las extensiones de SQL -==> Definición de politicas
    implementation("${edcGroup}:contract-definition-store-sql:${edcVersion}")       // Todas las extensiones de SQL -==> Definición de contratos
    implementation("${edcGroup}:contract-negotiation-store-sql:${edcVersion}")      // Todas las extensiones de SQL -==> Negociación de contratos
    implementation("${edcGroup}:transfer-process-store-sql:${edcVersion}")          // Todas las extensiones de SQL -==> Proceso de transferencias
    implementation("${edcGroup}:sql-pool-apache-commons:${edcVersion}")             // Todas las extensiones de SQL -==> Herramientas de SQL
    implementation("${edcGroup}:transaction-local:${edcVersion}")                   // Todas las extensiones de SQL -==> Transacciones locales
    implementation("${edcGroup}:transaction-datasource-spi:${edcVersion}")          // Todas las extensiones de SQL -==> Transacciones locales
    implementation("${edcGroup}:control-plane-sql:${edcVersion}")                   // Todas las extensiones de SQL -==> Transacciones locales
    implementation("org.postgresql:postgresql:42.6.0")                              // Plugin estandar de postgres

    // Plugin propio
    // implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")                     // Manejo del API de jakarta
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.shadowJar {
    isZip64 = true
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> { // Aquí creamos el jar autoejecutable que utilizaremos, como el shadow plugin de mvn
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
}

```

### Compilación

Para compilarlo ahora lo que tendremos que hacer será ir a la raíz del proyecto, (Carpeta que hemos creado en el punto 2 de la lista incluida en [Creación del proyecto gradle](#creación-del-proyecto-gradle)), y ejecutar el comando ``./gradlew clean build`` para compilar el proyecto. Este comando nos generará dos ficheros .jar en la carpeta ``build/libs/``, pero solo nos interesa el que tiene como nombre **connector.jar**

Con esta parte ya tendríamos el .jar que contiene el conector como tal, por lo que ahora podríamos ejecutarlo con todos los parámetros que necesita (``java -jar <parámetros> connector.jar``) o ejecutarlo desde un Dockerfile, con un fichero de configuración ``.properties`` para poder lanzar de forma automática todos los que veas necesarios. Yo explicaré como lanzarlo a través de un docker-compose.yml, para poder lanzar un conector «consumidor» y otro «proveedor».

### Lanzar nuestro conector

Para lanzar el conector crearemos a la misma altura que se encuentra ahora «conector» (Que al ejecutar el comando ll/ls solo se vea esa carpeta) una carpeta raíz de la configuración (Yo la he llamado **docker**, pero revisa la [esctructura de carpetas](#estructura-de-directorios-completa-al-finalizar)) que contenga:

- Los ficheros de configuración del conector.
- Los certificados del conector.
- El script de creación de la base de datos
- El «Dockerfile».
- Un docker compose para lanzar el conector junto a la base de datos de PostgreSQL.

Se puede generar toda la estructura de carpetas con el siguiente comando:

Dentro de cada conf va la configuración de un conector diferente, por lo que aunque aquí haya dos puede haber solo 1 o 100, depende de lo que necesites.
``mkdir -p docker/configuracion/{cert,conf,conf2,db}``


#### Ficheros de configuración
Necesitaremos dos ficheros, el configuration.properties (Configuración) y el vault.properties (Logs), que se encuentran dentro de "docker/configuracion/conf".

La estructura de ficheros de la configuración se encuentra completa junto a la documentación, para que se pueda utilizar sin necesidad de crear todos los ficheros uno a uno.

En esta documentación solo mostraré los ficheros de configuración de uno de los dos conectores, puesto que la configuración es muy parecida en ambos y solo cambian los valores que tendran, por ejemplo uno tendrá el puerto «29191» para el API y el otro el puerto «19191»... Ademas, como ya he adelantado el otro fichero de configuración se encuentra junto a esta documentación.

##### Configuración
Este fichero se debería llamar configuration.properties, ya que es el nombre con el que lo referenciaremos, en caso de que quieras poner otro nombre deberas cambiar las referencias.

1. El ID del participante, "su nombre" en el sistema federado          
2. La dirección de callback                                            
3. La contraseña del API                                               
4. Los datos del KeyStore, ruta, alias y contraseña                    
5. La URL de validación de los tokens                                  
6. Los puertos que exposearemos y su endpoint                          
7. Las conexiones a los data sources de postgres (assets, politicas...)

Hay que tener en cuenta que todas las rutas serán relativas al contenedor de docker, por lo que si has definido el volumen **«.configuracion:/configuracion»**, todas las rutas tendrán que surgir de **/configuracion**. Un ejemplo del fichero sería el siguiente:

```sh
# Id del participante, "nombre"
edc.participant.id=consumer
# Direccion de callback, a la cual enviarán los datos los otros conectores
edc.dsp.callback.address=http://consumer:29194/protocol
# Direccion del logger, utilizado para transferencias pull
edc.receiver.http.endpoint=http://localhost:4000/receiver/urn:connector:consumer/callback

# Contraseña del API, va en la cabecera de las peticiones
edc.api.auth.key=password
# El keystore con el certificado y la clave privada en formato JKS
edc.keystore=/configuracion/cert/sergio-keystore.jks
edc.keystore.password=sergio
edc.keystore.alias=sergio

# URL de validación de tokens
edc.dataplane.token.validation.endpoint=http://localhost:29192/control/token

# Puertos
web.http.port=29191
web.http.path=/api
web.http.management.port=29193
web.http.management.path=/management
web.http.protocol.port=29194
web.http.protocol.path=/protocol
web.http.public.port=29291
web.http.public.path=/public
web.http.control.port=29192
web.http.control.path=/control

# PostgreSQL
# Diferentes conexiones a las fuentes de datos, segun si es para hacer uso de assets, politicas, contratos... 

edc.datasource.asset.name=asset
edc.datasource.asset.user=postgres
edc.datasource.asset.password=edcpassword
edc.datasource.asset.url=jdbc:postgresql://postgres2:5432/postgres

edc.datasource.policy.name=policy
edc.datasource.policy.user=postgres
edc.datasource.policy.password=edcpassword
edc.datasource.policy.url=jdbc:postgresql://postgres2:5432/postgres

edc.datasource.contractdefinition.name=contractdefinition
edc.datasource.contractdefinition.user=postgres
edc.datasource.contractdefinition.password=edcpassword
edc.datasource.contractdefinition.url=jdbc:postgresql://postgres2:5432/postgres

edc.datasource.contractnegotiation.name=contractnegotiation
edc.datasource.contractnegotiation.user=postgres
edc.datasource.contractnegotiation.password=edcpassword
edc.datasource.contractnegotiation.url=jdbc:postgresql://postgres2:5432/postgres

edc.datasource.transferprocess.name=transferprocess
edc.datasource.transferprocess.user=postgres
edc.datasource.transferprocess.password=edcpassword
edc.datasource.transferprocess.url=jdbc:postgresql://postgres2:5432/postgres

edc.datasource.default.name=default
edc.datasource.default.user=postgres
edc.datasource.default.password=edcpassword
edc.datasource.default.url=jdbc:postgresql://postgres2:5432/postgres
```

##### Logs

Al utilizar el sistema de «azurite» o «hashicorp vault» hay que añadir en ese fichero el usuario y la contraseña del conector en ese sistema, aquí al tratar con postgres no es necesario añadir nada de información (Que yo haya visto por ahora)

### Certificados

Para los certificados lo mejor es crear una carpeta por cada conector que vayas a crear, por ejemplo «cert consumer», en este caso al ser solo para ilustrar el funcionamiento utilizaré una sola carpeta «cert» que contendrá los certificados y todos los conectores los utilizarán.

Para crear los certificados lo principal será crear la clave privada, para lo cual utilizaremos OpenSSL, con el siguiente comando ``openssl ecparam -name prime256v1 -genkey -noout -out private-key.pem``, una vez que tengamos la clave privada ya podemos generar la clave publica y el certificado, con los comandos: ``openssl ec -in private-key.pem -pubout -out public-key.pem``  y ``openssl req -new -x509 -key private-key.pem -subj '/CN=Some-State/C=AU/O=Internet Widgits Pty Ltd'  -out cert.pem -days 365``, en la generación del certificado utilizamos el parámetro **-subj** para poder crearlos de manera automática y desatendida. 

**//TODO: Comprobar si los certificados creados con java keytool funcionan o si sigue utilizando bouncy castle de manera interna**

Una vez que ya tengamos los certificados creados generaremos el JKS, para ello utilizaremos la herramienta del generador de certificados, el cual es un programa .jar al que le podemos pasar 3 parámetros: La ruta con los certificados, el alias y la contraseña, o podemos pasarle la ruta de un fichero que contenga varios datos en formato CSV, pero la herramienta muestra una ayuda si no introducimos bien los parámetros (El generador tambien se encuentra junto a esta documentación). 

Una vez que ya sabemos como funciona la herramienta podemos utilizarla, desde la raíz de los ficheros de configuración ejecutaremos el comando ``java -jar generadorCertificados.jar <ruta hasta la carpeta con los certificados> <alias> <contraseña>``, y cuando acabe mostrará el mensaje de que se ha realizado con exito o ha habido algun error en la generación. 

En este caso teniendo en cuenta la estructura de directorios actual el comando sería ``java -jar generadorCertificados.jar configuracion/cert sergio sergio`` 

```sh
├── configuracion                   
│   ├── cert                        
│   │   ├── cert.pem                
│   │   ├── private-key.pem         
│   │   ├── public-key.pem          
│   │   └── sergio-keystore.jks     
│   ├── conf                        
│   │   ├── configuration.properties
│   │   └── vault.properties        
│   ├── conf2                       
│   │   ├── configuration.properties
│   │   └── vault.properties        
│   └── db                          
│       └── init.sql                
├── connector.jar                   
├── docker-compose.yml              
├── Dockerfile                      
├── generadorCertificados.jar       

```

### Script de la base de datos

El script de la base de datos ya esta creado, solo hay que añadirlo a un fichero en la configuración e indicar a postgres que debe utilizarlo para lanzar las bases de datos, se encuentra en un fichero llamado **«init.sql»** junto a esta documentación. En este ejemplo se encuentra en «configuracion/db/init.sql», pero puedes utilizar cualquier ruta dentro de la carpeta de configuración.

### Dockerfile

Ahora crearemos el dockerfile que nos lanzará el conector, dentro del propio fichero esta explicado que hace cada linea.
El jar del conector deberá estar en la misma carpeta que tengamos el Dockerfile, para poder acceder a el con la ruta **./connector.jar**

```Dockerfile
# Seleccionamos la imagen que vamos a utilizar para nuestro conector, la opcion -buster es necesaria para tener acceso al gestor de paquetes apt
FROM openjdk:17-slim-buster
# Argumentos que le pasaremos a la JVM, por ejemplo una instancia del depurador
ARG JVM_ARGS=""
# Instalamos curl y borramos los registros de apt para ahorrar espacio
RUN apt update \
    && apt install -y curl 
    # && rm -rf /var/cache/apt/archives /var/lib/apt/lists # DESCOMENTAR 
# Marcamos el directorio de trabajo como /app dentro del conector
WORKDIR /app
# Copiamos el jar del conector en el directorio de trabajo
COPY ./connector.jar /app
# Seteamos la variable de entorno JVM_ARGS con el valor del argumento creado antes
ENV JVM_ARGS=$JVM_ARGS
# Solo para pruebas, actualiza apt, instala diversos paquetes de depuración, crea el alias ll y borramos los registros apt
RUN apt update; apt install -y vim tcpflow jq iproute2 tcpdump; echo "alias ll='ls -laiFh --color=always'">>~/.bashrc; rm -rf /var/cache/apt/archives /var/lib/apt/lists

# Punto de entrada de la imagen, comando que iniciará el funcionamiento de la «máquina»
ENTRYPOINT [ "sh", "-c", "exec java $JVM_ARGS -jar connector.jar"]
```

### Docker-compose.yml

El docker compose es el fichero que nos permitirá desplegar instancias del conector de manera conjunta con la base de datos, en este caso el conector que estamos lanzando durante la documentación es el llamado «company2», al igual que el dockerfile estan todas las lineas explicadas.  En este fichero si que se ven de manerá conjunta ambos conectores, ya que este fichero no se encuentra junto a los ficheros que muestro junto a la documentación

```yml 
version: "2.3"
services:
  # Conector
  company1:
    container_name: provider # Nombre del contenedor
    build:
      context: ./ # Donde se encuentra la raíz
    environment: 
      EDC_CONNECTOR_NAME: provider # Nombre del conector
      EDC_VAULT: /configuracion/conf/vault.properties # Ruta hasta el vault, es donde está el certificado
      EDC_FS_CONFIG: /configuracion/conf/configuration.properties # Ruta hasta las propiedades de configuración
      EDC_IAM_DID_WEB_USE_HTTPS: "true" # Forzar el uso de https
     # Puertos, la "explicación" de cada uno se encuentra en sus propiedades especificas
    ports:
      - "19191:19191"
      - "19193:19193"
      - "19194:19194"
      - "19291:19291"
      - "19192:19192"
    volumes:
      - ./configuracion:/configuracion # Volumen añadido al contenedor de docker, para tener acceso a toda la configuración

  company2:
    container_name: consumer # Nombre del contenedor
    build:
      context: ./ # Donde se encuentra la raíz
    environment: 
      EDC_CONNECTOR_NAME: consumer # Nombre del conector
      EDC_VAULT: /configuracion/conf2/vault.properties # Ruta hasta el vault, es donde está el certificado
      EDC_FS_CONFIG: /configuracion/conf2/configuration.properties # Ruta hasta las propiedades de configuración
      EDC_IAM_DID_WEB_USE_HTTPS: "true" # Forzar el uso de https

     # Puertos, la "explicación" de cada uno se encuentra en sus propiedades especificas
    ports:
      - "29191:29191"       
      - "29193:29193"       
      - "29194:29194"       
      - "29291:29291"       
      - "29192:29192"       
    volumes:
      - ./configuracion:/configuracion # Volumen añadido al contenedor de docker, para tener acceso a toda la configuración

  # Base de datos
  postgres1:
    container_name: postgres1 # Nombre del contenedor de postgres
    image: postgres:13.4 # Imagen y version a utilizar
    ports:
      - "5432:5432" # Puerto para acceder desde la maquina anfitiron
    environment:
      POSTGRES_USER: postgres # Usuario para conectarse
      POSTGRES_PASSWORD: edcpassword # Contraseña para conectarse
    volumes:
      - ./configuracion/db:/docker-entrypoint-initdb.d/ # Ruta hasta la carpeta que contiene el script «init.sql»

  postgres2:
    container_name: postgres2 # Nombre del contenedor de postgres
    image: postgres:13.4 # Imagen y version a utilizar
    ports:
      - "5433:5432" # Puerto para acceder desde la maquina anfitiron
    environment:
      POSTGRES_USER: postgres # Usuario para conectarse
      POSTGRES_PASSWORD: edcpassword # Contraseña para conectarse
    volumes:
      - ./configuracion/db:/docker-entrypoint-initdb.d/ # Ruta hasta la carpeta que contiene el script «init.sql»

  logger:
    build:
      context: ../http-request-logger
    ports:
      - "4000:4000"
```
# Extensión propia

Para poder evitar tener que pasar por un servidor externo cada vez que quiera pasar mis datos o tener que montar un servidor de fichero propio con ese fin he desarrollado una pequeña extension, la cual se encuentra en el mismo github que el conector (No se puede compilar desde VSCode porque no detecta las librerias externas), pero el codigo es el siguiente:

Extension: Es la parte principal, posee la injeccion del web service y el metodo que inicializa la extension

```java
package org.example;

import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;

public class DownloadExtension implements ServiceExtension {

    @Inject
    WebService webService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        webService.registerResource(new DownloadController(context.getMonitor()));

    }
}
```

Controller: Es la clase que implementa la funcionalidad de la extensión, funciona en base a Jax-RS 

```java
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
```

Despues de la creación de las clases tenemos que añadir la extension a nuestro conector, para ello iremos a la carpeta de resources/META-INF/services y crearemos un fichero, con el nombre: \<org.eclipse.edc.spi.system.ServiceExtension\> y dentro de ese fichero la ruta de paquetes hasta nuestra extensión, en mi caso solamente ``org.example.DownloadExtension``, y esto nos creará el endpoint en la ruta de /api/.

La estructura de carpetas sería la siguiente:

```sh
.
├── java
│   └── org
│       └── example
│           ├── DownloadController.java
│           └── DownloadExtension.java
└── resources
    └── META-INF
        └── services
            └── org.eclipse.edc.spi.system.ServiceExtension
```


# Despliegue final

Una vez que ya has seguido todos los pasos y has creado todos los ficheros puedes desplegar el docker compose, para ello se hace con el siguiente comando ``docker compose -f <ruta hasta el docker-compose.yml> up --build``, o si estas desde VSCode con la extensión de Docker puedes desplegarlo con click derecho sobre el fichero y en el menú contextual click sobre \<Compose Up\>

# Pruebas

## Creación del dataplane

Creamos en el proveedor un «dataplane», que sirve para que pueda servir datos.
```sh
curl -H 'Content-Type: application/json' \
     -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@id": "http-pull-provider-dataplane",  "url": "http://provider:19192/control/transfer",  "allowedSourceTypes": [    "HttpData"  ],  "allowedDestTypes": [    "HttpProxy",    "HttpData"  ],  "properties": {    "https://w3id.org/edc/v0.0.1/ns/publicApiUrl": "http://provider:19291/public/"  }}' \
     -X POST "http://localhost:19193/management/v2/dataplanes" -s | jq
```

## Ofrecimiento de datos

Ahora tenemos que crear los assets en el proveedor, y disponerlos para las transferencias, para ello tenemos que crear los 3 artefactos que muestro a continuación.

### Creación del asset

Para crear el asset tenemos que darle los datos propios del asset (Metadata almacenada en properties) y donde se encuentra el contenido de ese asset, en este caso es una URL externa que contiene la información de todo el pan vendido por el alcampo, pero puede ser de un sistema de almacenamiento propio.

```sh
curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@id": "assetId",  "properties": {    "name": "product description",    "contenttype": "application/json"  },  "dataAddress": {    "type": "HttpData",    "name": "Test asset",    "baseUrl": "https://www.compraonline.alcampo.es/api/v5/products/search?limit=100&offset=0&term=pan",    "proxyPath": "true"  }}' \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
-s | jq
```

### Creación de la politica

Las politicas sirven para «definir» que conectores tienen acceso a ver el artefacto y quienes a descargarlo, por ejemplo, puedes crear una politica para que todas las personas de Europa lo puedan ver pero solo puedan descargarlo desde España. En este caso estamos creando una politica que permite ver y descargar por todo el mundo, sin ninguna restricción.

```sh
curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",    "odrl": "http://www.w3.org/ns/odrl/2/"  },  "@id": "aPolicy",  "policy": {    "@context": "http://www.w3.org/ns/odrl.jsonld",    "@type": "Set",    "permission": [],    "prohibition": [],    "obligation": []  }}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/policydefinitions \
-s | jq

```

### Creación del contrato

Ahora crearemos el contrato, que es lo que realmente van a ver los conectores, es un apartado que contiene el/los metadatos de el/los artefacto/s que quieras ofrecer junto a una politica, en caso de que te «gusten» esos metadatos y cumplas la politica podrás acceder al asset.

```sh
curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@id": "1",  "accessPolicyId": "aPolicy",  "contractPolicyId": "aPolicy",  "assetsSelector": []}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/contractdefinitions \
-s | jq
```

### Solicitar el catalogo

Ahora tenemos que pedir desde el consumidor el catalogo del proveedor, que nos muestra la metadata de los artefactos que hemos visto antes, en este caso solo va a tener un artefacto, pero puede tener muchos.

```sh
curl -X POST "http://localhost:29193/management/v2/catalog/request" \
  -H 'Content-Type: application/json' \
  -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "counterPartyAddress": "http://provider:19194/protocol",  "protocol": "dataspace-protocol-http"}' \
-s | jq
```

### Negociar el contrato

Ahora negociaremos con el proveedor el contrato para ver si cumplimos esas politicas previamente definidas, en este caso si, ya que es imposible no cumplirlas pues es una politica que permite todo.

Para este apartado necesitamos el id del contrato que queremos negociar, es el apartado del catalogo identificado como: «dcat:dataset.odrl:hasPolicy.@id», nombrado aquí con: {{Id_descargar}}. Es una cadena en B64 con la siguiente estructura '\<contractId\>:\<assetId\>:\<Identificador de la negociación\>'

```sh
curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@type": "ContractRequest",  "counterPartyAddress": "http://provider:19194/protocol",  "protocol": "dataspace-protocol-http",  "policy": {    "@context": "http://www.w3.org/ns/odrl.jsonld",    "@id": "MQ==:YXNzZXRJZA==:YzQ1ZmI5NDMtNDFiMy00MGJjLWIwZDItMTY3YTc1ZWM3Yzdh",    "@type": "Offer",    "assigner": "provider",    "target": "assetId"  }}' \
  -X POST -H 'content-type: application/json' http://localhost:29193/management/v2/contractnegotiations \
-s | jq
```

### Comprobar el estado de la negociación

Una vez que ya hayamos negociado el contrato hay varios estados por los que puede pasar desde nuestro consumidor: INITIAL, REQUESTING, REQUESTED, AGREED, VERIFYING, VERIFIED, FINALIZED. O los mensajes de error, que no los trataré ahora. Para comprobar en cual de esos puntos del contrato estamos podemos utilizar el siguiente comando, el cual nos devolverá un JSON con el estado en el que nos encontramos, y una vez en finalized con el «contractAgreementId», que es lo que necesitaremos para la transferencia de datos.

Para este apartado necesitamos el ID de la negociación que nos ha devuelto el apartado anterior, identificado como: «id», nombrado aqí con: {{Id}}

```sh
curl -X GET "http://localhost:29193/management/v2/contractnegotiations/bdf56e71-6d0e-40fa-998c-4b3d0c7ae604" \
  --header 'Content-Type: application/json' \
-s | jq
```

### Pull

Por ahora me da un error que no comprendo bien

```sh
curl -X POST "http://localhost:29193/management/v2/transferprocesses" \
  -H "Content-Type: application/json" \
  -d '{   "@context": {     "@vocab": "https://w3id.org/edc/v0.0.1/ns/"   },   "@type": "TransferRequestDto",   "connectorId": "provider",   "counterPartyAddress": "http://provider:19194/protocol",   "contractId": "2994c548-3d8f-49e1-9fd0-ab5002c02e6f",   "assetId": "assetId",   "protocol": "dataspace-protocol-http",   "dataDestination": {     "type": "HttpProxy"   } } ' \
  -s | jq


```

```sh
curl http://localhost:29193/management/v2/transferprocesses/fcaff37b-96b8-4b76-9cfd-fa775c56fc18
```

```sh 
curl --location --request GET 'http://localhost:19291/public/' --header 'Authorization: <auth code>'
```
### Push

Para hacer este tipo de transferencia de datos necesitas un endpoint en el cual se van a almacenar los datos, en el cual escribirá directamente el proveedor, para nosotros ese endpoint ahora mismo pertenece a webhook, un servidor publico que recibe peticiones HTTP por nosotros. 

Para esté apartado necesitamos 2 variables, una que será el Id que hemos visto antes con el resultado positivo de la negociación, y otro que será el endpoint sobre el cual se volcarán los datos, estan identificados de la siguiente manera: {{contractAgreementId}}, para el identificador correcto del contrato, y {{endpointURL}} para la URL sobre la cual realizaremos las peticiones de tipo HTTP

```sh
curl -X POST "http://localhost:29193/management/v2/transferprocesses" \
  -H "Content-Type: application/json" \
  -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@type": "TransferRequestDto",  "connectorId": "provider",  "counterPartyAddress": "http://provider:19194/protocol",  "contractId": "a926663c-d6c8-4137-bf32-fa0f5607d836",  "assetId": "assetId",  "protocol": "dataspace-protocol-http",  "dataDestination": {    "type": "HttpData",    "baseUrl": "http://consumer:29191/api/endpoint"  }}' \
-s | jq
```

#### Mi script

Para facilitar todas estas llamadas lo que he hecho ha sido crear una serie de scripts agrupados en un script final, los cuales son los siguientes, por orden

```sh
#!/bin/bash

curl -H 'Content-Type: application/json' \
     -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@id": "http-pull-provider-dataplane",  "url": "http://provider:19192/control/transfer",  "allowedSourceTypes": [    "HttpData"  ],  "allowedDestTypes": [    "HttpProxy",    "HttpData"  ],  "properties": {    "https://w3id.org/edc/v0.0.1/ns/publicApiUrl": "http://provider:19291/public/"  }}' \
     -X POST "http://localhost:19193/management/v2/dataplanes" -s | jq

curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@id": "assetId",  "properties": {    "name": "product description",    "contenttype": "application/json"  },  "dataAddress": {    "type": "HttpData",    "name": "Test asset",    "baseUrl": "https://www.compraonline.alcampo.es/api/v5/products/search?limit=100&offset=0&term=pan",    "proxyPath": "true"  }}' \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
-s | jq

curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",    "odrl": "http://www.w3.org/ns/odrl/2/"  },  "@id": "aPolicy",  "policy": {    "@context": "http://www.w3.org/ns/odrl.jsonld",    "@type": "Set",    "permission": [],    "prohibition": [],    "obligation": []  }}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/policydefinitions \
-s | jq

curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "@id": "1",  "accessPolicyId": "aPolicy",  "contractPolicyId": "aPolicy",  "assetsSelector": []}' \
  -H 'content-type: application/json' http://localhost:19193/management/v2/contractdefinitions \
-s | jq

```

```sh
#!/bin/bash

curl -X POST "http://localhost:29193/management/v2/catalog/request" \
  -H 'Content-Type: application/json' \
  -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },  "counterPartyAddress": "http://provider:19194/protocol",  "protocol": "dataspace-protocol-http"}' \
-s | jq '."dcat:dataset"."odrl:hasPolicy"."@id"' | tr -d \"

```

```sh
#!/bin/bash

curl -d "{  \"@context\": {    \"@vocab\": \"https://w3id.org/edc/v0.0.1/ns/\"  },  \"@type\": \"ContractRequest\",  \"counterPartyAddress\": \"http://provider:19194/protocol\",  \"protocol\": \"dataspace-protocol-http\",  \"policy\": {    \"@context\": \"http://www.w3.org/ns/odrl.jsonld\",    \"@id\": \"$1\",    \"@type\": \"Offer\",    \"assigner\": \"provider\",    \"target\": \"assetId\"  }}" \
  -X POST -H 'content-type: application/json' http://localhost:29193/management/v2/contractnegotiations \
-s | jq '."@id"'  | tr -d \" 
```

```sh
#!/bin/bash

if [ $2 = "estado" ]; then
  curl -X GET "http://localhost:29193/management/v2/contractnegotiations/$1" \
    --header 'Content-Type: application/json' \
  -s  | jq '.state' |  tr -d \" 
else
    curl -X GET "http://localhost:29193/management/v2/contractnegotiations/$1" \
    --header 'Content-Type: application/json' \
  -s  | jq '.contractAgreementId' |  tr -d \" 
fi
```

```sh
#!/bin/bash

curl -X POST "http://localhost:29193/management/v2/transferprocesses" \
  -H "Content-Type: application/json" \
  -d "{  \"@context\": {    \"@vocab\": \"https://w3id.org/edc/v0.0.1/ns/\"  },  \"@type\": \"TransferRequestDto\",  \"connectorId\": \"provider\",  \"counterPartyAddress\": \"http://provider:19194/protocol\",  \"contractId\": \"$1\",  \"assetId\": \"assetId\",  \"protocol\": \"dataspace-protocol-http\",  \"dataDestination\": {    \"type\": \"HttpData\",    \"baseUrl\": \"http://consumer:29191/api/endpoint\"  }}" \
-s | jq
```

```sh
#!/bin/bash
finalizado=false
/home/sergio/prueba/crearArtefacto

artefacto=$(/home/sergio/prueba/obtenerCatalogo)
echo "ID del artefacto a negociar: $artefacto"

contrato=$(/home/sergio/prueba/negociarContrato $artefacto)
echo "ID del contrato: $contrato"
while ! $finalizado; do 
  estado=$(/home/sergio/prueba/comprobarEstado $contrato "estado")
  if [ $estado = "FINALIZED" ]; then 
    finalizado=true
  fi
done
contratoExitoso=$(/home/sergio/prueba/comprobarEstado $contrato "id")

/home/sergio/prueba/pushDatos $contratoExitoso
```

# Estructura de directorios completa al finalizar 

```sh
sergio@sergio:~/prueba$ tree
.
├── conector
│   ├── app
│   │   ├── build
│   │   │   ├── classes
│   │   │   │   └── java
│   │   │   │       ├── main
│   │   │   │       │   └── conector
│   │   │   │       │       └── App.class
│   │   │   │       └── test
│   │   │   │           └── conector
│   │   │   │               └── AppTest.class
│   │   │   ├── distributions
│   │   │   │   ├── app.tar
│   │   │   │   └── app.zip
│   │   │   ├── generated
│   │   │   │   └── sources
│   │   │   │       ├── annotationProcessor
│   │   │   │       │   └── java
│   │   │   │       │       ├── main
│   │   │   │       │       └── test
│   │   │   │       └── headers
│   │   │   │           └── java
│   │   │   │               ├── main
│   │   │   │               └── test
│   │   │   ├── libs
│   │   │   │   └── app.jar
│   │   │   ├── reports
│   │   │   │   └── tests
│   │   │   │       └── test
│   │   │   │           ├── classes
│   │   │   │           │   └── conector.AppTest.html
│   │   │   │           ├── css
│   │   │   │           │   ├── base-style.css
│   │   │   │           │   └── style.css
│   │   │   │           ├── index.html
│   │   │   │           ├── js
│   │   │   │           │   └── report.js
│   │   │   │           └── packages
│   │   │   │               └── conector.html
│   │   │   ├── resources
│   │   │   ├── scripts
│   │   │   │   ├── app
│   │   │   │   └── app.bat
│   │   │   ├── test-results
│   │   │   │   └── test
│   │   │   │       ├── binary
│   │   │   │       │   ├── output.bin
│   │   │   │       │   ├── output.bin.idx
│   │   │   │       │   └── results.bin
│   │   │   │       └── TEST-conector.AppTest.xml
│   │   │   └── tmp
│   │   │       ├── compileJava
│   │   │       │   └── previous-compilation-data.bin
│   │   │       ├── compileTestJava
│   │   │       │   └── previous-compilation-data.bin
│   │   │       ├── jar
│   │   │       │   └── MANIFEST.MF
│   │   │       └── test
│   │   ├── build.gradle
│   │   └── src
│   │       ├── main
│   │       │   ├── java
│   │       │   │   └── conector
│   │       │   │       └── App.java
│   │       │   └── resources
│   │       └── test
│   │           ├── java
│   │           │   └── conector
│   │           │       └── AppTest.java
│   │           └── resources
│   ├── build
│   │   ├── distributions
│   │   │   ├── conector-shadow.tar
│   │   │   ├── conector-shadow.zip
│   │   │   ├── conector.tar
│   │   │   └── conector.zip
│   │   ├── libs
│   │   │   ├── conector.jar
│   │   │   └── connector.jar
│   │   ├── scripts
│   │   │   ├── conector
│   │   │   └── conector.bat
│   │   ├── scriptsShadow
│   │   │   ├── conector
│   │   │   └── conector.bat
│   │   └── tmp
│   │       ├── jar
│   │       │   └── MANIFEST.MF
│   │       └── shadowJar
│   │           └── MANIFEST.MF
│   ├── build.gradle.kts
│   ├── gradle
│   │   └── wrapper
│   │       ├── gradle-wrapper.jar
│   │       └── gradle-wrapper.properties
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle
└── docker
    ├── configuracion
    │   ├── cert
    │   │   ├── cert.pem
    │   │   ├── private-key.pem
    │   │   ├── public-key.pem
    │   │   └── sergio-keystore.jks
    │   ├── conf
    │   │   ├── configuration.properties
    │   │   └── vault.properties
    │   ├── conf2
    │   │   ├── configuration.properties
    │   │   └── vault.properties
    │   └── db
    │       └── init.sql
    ├── connector.jar
    ├── docker-compose.yml
    ├── Dockerfile
    ├── generadorCertificados.jar
    └── resources
        └── db-scripts
```