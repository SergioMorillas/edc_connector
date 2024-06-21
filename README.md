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
- **Docker** versión 25.0.2
- **Docker Compose** versión v2.24.5
- **JQ** version 1.6

# Creación del conector

## Creación del proyecto gradle

Toda la información para la creacion de este proyecto se encuentra en la siguiente [URL](https://docs.gradle.org/current/userguide/part1_gradle_init.html), pero se le han agregado pruebas y comprobaciones.

1. Lo primero que debemos hacer es ejecutar el comando ``gradle`` para iniciar la sesión del gestor de proyectos.
2. Ahora crearemos una carpeta y entraremos dentro, ``mkdir connector; cd $_``
3. Una vez estemos dentro de la carpeta que acabamos de crear debemos crear el proyecto como tal, ``gradle init --type java-application``, con el cual crearemos un proyecto de tipo aplicación java. Tendremos que ir añadiendo parámetros que ira pidiendo por consola, podemos darle a enter y simplemente utilizar los valores por defecto, excepto en la opción «Select build script DSL:», que seleccionaremos **kotlin**.
4. A partir de este punto podemos utilizar el wrapper de gradle si nos es mas cómodo.


```bash
gradle
mkdir connector; cd $_
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
      maven(url = "https://repo1.maven.org/maven2/")
}
val edcGroup = "org.eclipse.edc"        // Seteamos el grupo de Eclipse que vamos a utilizar
val edcVersion = "0.7.0"                // Seteamos la version de los paquetes de Eclipse que vamos a utilizar
val postgresGroup = "org.postgresql"    // Seteamos el grupo de postgres que vamos a utilizar
val postgresVersion = "42.6.0"          // Seteamos la version de los paquetes de postgres que vamos a utilizar
val jakartaGroup ="jakarta.ws.rs"       // Seteamos el grupo de jakarta que vamos a utilizar
val jakartaVersion ="3.1.0"             // Seteamos la version de los paquetes de jakarta que vamos a utilizar

dependencies {
    implementation("${edcGroup}:transfer-data-plane-signaling:${edcVersion}");
    implementation("${edcGroup}:data-plane-selector-control-api:${edcVersion}");
    implementation("${edcGroup}:data-plane-self-registration:${edcVersion}");
    implementation("${edcGroup}:transfer-pull-http-dynamic-receiver:${edcVersion}");

    implementation("${edcGroup}:edr-cache-api:${edcVersion}");
    implementation("${edcGroup}:edr-store-receiver:${edcVersion}");
    implementation("${edcGroup}:edr-store-core:${edcVersion}");
    implementation("${edcGroup}:edr-index-sql:${edcVersion}");

    implementation("${edcGroup}:data-plane-selector-api:${edcVersion}")         // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-selector-core:${edcVersion}")        // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-control-api:${edcVersion}")          // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-public-api-v2:${edcVersion}")           // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-core:${edcVersion}")                 // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-http:${edcVersion}")                 // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector

    implementation("${edcGroup}:control-plane-core:${edcVersion}")              // Todas las extensiones básicas ==> Nucleo del control de transferencias
    implementation("${edcGroup}:control-plane-api:${edcVersion}")               // Todas las extensiones básicas ==> Nucleo del control de transferencias
    implementation("${edcGroup}:control-plane-api-client:${edcVersion}")        // Todas las extensiones básicas ==> API para clientes del control de transferencias
    implementation("${edcGroup}:management-api:${edcVersion}")                  // Todas las extensiones básicas ==> API de administración
    implementation("${edcGroup}:dsp:${edcVersion}")                             // Todas las extensiones básicas ==> Implementación del DataSpaceProtocol
    implementation("${edcGroup}:configuration-filesystem:${edcVersion}")        // Todas las extensiones básicas ==> Para mantener la configuración en local
    // implementation("${edcGroup}:vault-hashicorp:${edcVersion}")                 // Todas las extensiones básicas ==> Para mantener la configuración en local
    implementation("${edcGroup}:iam-mock:${edcVersion}")                        // Todas las extensiones básicas ==> Implementación del servicio de identificación
    implementation("${edcGroup}:http:${edcVersion}")                            // Todas las extensiones básicas ==> Control de comunicaciones HTTP
    implementation("${edcGroup}:transfer-data-plane-signaling:${edcVersion}")   // Todas las extensiones básicas ==> Control de comunicaciones HTTP
    implementation("${edcGroup}:transfer-pull-http-receiver:${edcVersion}")     // Todas las extensiones básicas ==> Control de comunicaciones HTTP

    implementation("${edcGroup}:asset-index-sql:${edcVersion}")                 // Todas las extensiones de SQL -==> Indice de assets
    implementation("${edcGroup}:policy-definition-store-sql:${edcVersion}")     // Todas las extensiones de SQL -==> Definición de politicas
    implementation("${edcGroup}:contract-definition-store-sql:${edcVersion}")   // Todas las extensiones de SQL -==> Definición de contratos
    implementation("${edcGroup}:contract-negotiation-store-sql:${edcVersion}")  // Todas las extensiones de SQL -==> Negociación de contratos
    implementation("${edcGroup}:transfer-process-store-sql:${edcVersion}")      // Todas las extensiones de SQL -==> Proceso de transferencias
    implementation("${edcGroup}:sql-pool-apache-commons:${edcVersion}")         // Todas las extensiones de SQL -==> Herramientas de SQL
    implementation("${edcGroup}:transaction-local:${edcVersion}")               // Todas las extensiones de SQL -==> Transacciones locales
    implementation("${edcGroup}:transaction-datasource-spi:${edcVersion}")      // Todas las extensiones de SQL -==> Transacciones locales
    implementation("${edcGroup}:control-plane-sql:${edcVersion}")               // Todas las extensiones de SQL -==> Transacciones locales
    implementation("${postgresGroup}:postgresql:${postgresVersion}")            // Plugin estandar de postgres

    // Plugin propio
    implementation("${jakartaGroup}:jakarta.ws.rs-api:${jakartaVersion}")           // Manejo del API de jakarta

    implementation("org.apache.commons:commons-lang3:3.14.0")
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
- El «Dockerfile», el cual compilará de manera interna el código del conector.
- Un docker compose para lanzar el conector junto a la base de datos de PostgreSQL.

Se puede generar toda la estructura de carpetas con el siguiente comando:

Dentro de cada conf va la configuración de un conector diferente, por lo que aunque aquí haya dos puede haber solo 1 o 100, depende de lo que necesites.
``mkdir -p docker/configuracion/{cert,conf,conf2,db,vault}``


#### Ficheros de configuración
Necesitaremos dos ficheros, el configuration.properties (Configuración), que se encuentra dentro de "docker/configuracion/conf".

La estructura de ficheros de la configuración se encuentra completa junto a la documentación, para que se pueda utilizar sin necesidad de crear todos los ficheros uno a uno.

En esta documentación solo mostraré los ficheros de configuración de uno de los dos conectores, puesto que la configuración es muy parecida en ambos y solo cambian los valores que tendran, por ejemplo uno tendrá el puerto «29191» para el API y el otro el puerto «19191»... Ademas, como ya he adelantado el otro fichero de configuración se encuentra junto a esta documentación.

##### Configuración
Este fichero se debería llamar configuration.properties, ya que es el nombre con el que lo referenciaremos, en caso de que quieras poner otro nombre deberas cambiar las referencias.

1. El ID del participante, "su nombre" en el sistema federado
2. La dirección de callback
3. La contraseña del API
4. Los datos del vault de hashicorp, ruta, alias y contraseña
5. La URL de validación de los tokens
6. Los puertos que exposearemos y su endpoint
7. Las conexiones a los data sources de postgres (assets, politicas...)

Hay que tener en cuenta que todas las rutas serán relativas al contenedor de docker, por lo que si has definido el volumen **«.configuracion:/configuracion»**, todas las rutas tendrán que surgir de **/configuracion**. Un ejemplo del fichero sería el siguiente:

```sh
# Id del participante, "nombre"
edc.participant.id=provider
edc.runtime.id=provider
edc.hostname=localhost

# Direccion de callback 
edc.dsp.callback.address=https://sergio.arlabdevelopments.com:19194/protocol
# Dirección del endpoint al que van a apuntar las peticiones de tipo pull, anteriormente el logging house
edc.receiver.http.endpoint=https://sergio.arlabdevelopments.com:19191/api/pull

# Contraseña del API, va en la cabecera de las peticiones
edc.api.auth.key=password
# Conexiones al vault de hashicorp, con la URL HTTPS y el token de acceso
edc.vault.hashicorp.url=https://sergio.arlabdevelopments.com:8200
edc.vault.hashicorp.token=hvs.MjqhQhMgM52HTOWy0ekHij6G
edc.vault.hashicorp.timeout.seconds=36000
# Alias tanto del certificado como de la clave privada en el vault
edc.public.key.alias=certificate
edc.transfer.dataplane.token.signer.privatekey.alias=private_key
edc.transfer.proxy.token.signer.privatekey.alias=private_key
edc.transfer.proxy.token.verifier.publickey.alias=certificate
# Clave publica y privada
private_key=-----BEGIN PRIVATE KEY-----\nMIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgeAeM1P97OP0VnsId\nlSLbPGvfp1VuUkCzEwJQlYNrdA6hRANCAAT4ESIxyYp5dOTHczptRFLs60lo+i5O\nqUju0zsaerQNUWwO0njjmdOY6GNf+NWNexiUsisZa1OpNAJYkbdCp9JB\n-----END PRIVATE KEY-----
certificate=-----BEGIN CERTIFICATE-----\nMIIEOzCCAyOgAwIBAgISBBm92KouRlD74OCTIBTWen2FMA0GCSqGSIb3DQEBCwUA\nMDIxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MQswCQYDVQQD\nEwJSMzAeFw0yNDA1MjQwNzEyNTNaFw0yNDA4MjIwNzEyNTJaMCcxJTAjBgNVBAMT\nHHNlcmdpby5hcmxhYmRldmVsb3BtZW50cy5jb20wWTATBgcqhkjOPQIBBggqhkjO\nPQMBBwNCAAT4ESIxyYp5dOTHczptRFLs60lo+i5OqUju0zsaerQNUWwO0njjmdOY\n6GNf+NWNexiUsisZa1OpNAJYkbdCp9JBo4ICHzCCAhswDgYDVR0PAQH/BAQDAgeA\nMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAMBgNVHRMBAf8EAjAAMB0G\nA1UdDgQWBBR+MRgxiSBAv65zUDdyCurFIZrq/DAfBgNVHSMEGDAWgBQULrMXt1hW\ny65QCUDmH6+dixTCxjBVBggrBgEFBQcBAQRJMEcwIQYIKwYBBQUHMAGGFWh0dHA6\nLy9yMy5vLmxlbmNyLm9yZzAiBggrBgEFBQcwAoYWaHR0cDovL3IzLmkubGVuY3Iu\nb3JnLzAnBgNVHREEIDAeghxzZXJnaW8uYXJsYWJkZXZlbG9wbWVudHMuY29tMBMG\nA1UdIAQMMAowCAYGZ4EMAQIBMIIBBQYKKwYBBAHWeQIEAgSB9gSB8wDxAHYAPxdL\nT9ciR1iUHWUchL4NEu2QN38fhWrrwb8ohez4ZG4AAAGPqaj1sQAABAMARzBFAiEA\n5/bXkivyTPESoXAQdGqEuaCxp6BR/ueK90C9dMEkP4oCIAtHiE63n7WNx/mig7JV\nKEQYUyVMKD0VL6pkI5oba8uhAHcAdv+IPwq2+5VRwmHM9Ye6NLSkzbsp3GhCCp/m\nZ0xaOnQAAAGPqaj12wAABAMASDBGAiEA2uWE8w7MHbjxPSQYctQ5DnkxfTSkzlG2\nGHk4ekAi2FwCIQD+V49yKdq5M9POktlYLV6CpRH/h+wWdlK+u42fKLZobzANBgkq\nhkiG9w0BAQsFAAOCAQEAcGxzUqJpK+fk2LQ8JJvkxQaamJ3yChwfv71omPn4ueMS\nNtP6/eyZPyQwZ6PNgKK4tDEyo9A1AlYPpvH76VL49MqT1u0KOtf6xPO1wivj9R2V\npFci4y867Ol+Qy+qJEgJTMP9PZh3UZ67qrH6CV9OOnjAGAl12wFl8+u97jmw+eBi\n47FcZVH/MVvSB5+p3SnO8MDB/kIMddKkkoWvVC5UW26KDA6Wu/9K3BFkEPpoIuEr\nRmmOtvhq7tvRNUaQdyRSX4B31X4jFMuV0S1OikbSIUJZvizdK9U5YYR4yxEsf6g0\nK5K9k9ouzSRdXufJ5h9jz6156EoF5Fw4YUAcgZnaSg==\n-----END CERTIFICATE-----

# edc.http.client.https.enforce=true
# edc.jsonld.https.enabled=true
# edc.iam.did.web.use.https=true

# URL de validación de tokens
edc.dataplane.token.validation.endpoint=https://sergio.arlabdevelopments.com:19192/control/token

# Puertos
web.http.port=19191
web.http.path=/api
web.http.management.port=19193
web.http.management.path=/management
web.http.protocol.port=19194
web.http.protocol.path=/protocol
web.http.public.port=19291
web.http.public.path=/public
web.http.control.port=19192
web.http.control.path=/control

# PostgreSQL
# Diferentes conexiones a las fuentes de datos, segun si es para hacer uso de assets, politicas, contratos... 
edc.datasource.asset.name=asset
edc.datasource.asset.user=postgres
edc.datasource.asset.password=edcpassword
edc.datasource.asset.url=jdbc:postgresql://postgres1:5432/postgres

edc.datasource.policy.name=policy
edc.datasource.policy.user=postgres
edc.datasource.policy.password=edcpassword
edc.datasource.policy.url=jdbc:postgresql://postgres1:5432/postgres

edc.datasource.contractdefinition.name=contractdefinition
edc.datasource.contractdefinition.user=postgres
edc.datasource.contractdefinition.password=edcpassword
edc.datasource.contractdefinition.url=jdbc:postgresql://postgres1:5432/postgres

edc.datasource.contractnegotiation.name=contractnegotiation
edc.datasource.contractnegotiation.user=postgres
edc.datasource.contractnegotiation.password=edcpassword
edc.datasource.contractnegotiation.url=jdbc:postgresql://postgres1:5432/postgres

edc.datasource.transferprocess.name=transferprocess
edc.datasource.transferprocess.user=postgres
edc.datasource.transferprocess.password=edcpassword
edc.datasource.transferprocess.url=jdbc:postgresql://postgres1:5432/postgres

edc.datasource.edr.name=edr_entry
edc.datasource.edr.user=postgres
edc.datasource.edr.password=edcpassword
edc.datasource.edr.url=jdbc:postgresql://postgres1:5432/postgres

edc.datasource.default.name=default
edc.datasource.default.user=postgres
edc.datasource.default.password=edcpassword
edc.datasource.default.url=jdbc:postgresql://postgres1:5432/postgres

# JKS para que el conector funcione sobre TLS
edc.web.https.keymanager.password=password
edc.web.https.keystore.password=password
edc.web.https.keystore.path=/configuracion/certificados/keyStore.p12
edc.web.https.keystore.type=PKCS12
```

# Certificados

Necesitamos certificados tanto para que los conectores funcionen sobre TLS como para la generación de los proxys de las transferencias de datos. Para ello he utilizado la herramienta de Let`s Encrypt con certbot para generar el certificado y la clave privada, toda la documentación sobre como he generado los certificados se encuentra en el siguiente enlace, pero una vez instalas el cliente de certbot es muy intuitivo. Pero para generar el certificado he utilizado el siguiente comando ``sudo certbot certonly --standalone``, al ejecutarlo te preguntara por el nombre de dominio, y al introducirlo te creará el certificado

## Vault

Como se puede ver el fichero de configuración el vault tiene una dirección https, para conseguir eso hay que seguir varios pasos.

1. Crear en el docker compose un servicio con la imagen del vault:1.13.3, un nombre y los valores de entorno necesarios. Tambien le he añadido un entrypoint
2. Configuración del vault. Para configurarlo hay que crear un archivo .hcl, que se le pasará como parametro al constructor del servidor durante la instanciación del mismo, el contenido debe ser algo como lo que muestro a continuación
3. Entrypoint. Para lanzar el vault de manera automática lo he hecho a través de un shell script, que hace de forma automatica el unseal y carga la clave privada, el certificado en caso de que sea la primera vez que se levanta.

### Configuración del .hcl
```
storage "file" {
  path = "/mnt/vault/data"
}
listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_cert_file = "/configuracion/vault/fullchain.pem"
  tls_key_file  = "/configuracion/vault/privkey1.pem"
}

api_addr = "https://localhost:8200"
cluster_addr = "https://localhost:8201"
ui = true
disable_mlock = true
```

### Entrypoint
```
#!/bin/sh
set -u
set -o pipefail



RED_COLOR='\033[1;31m'
GREEN_COLOR='\033[1;32m'
YELLOW_COLOR='\033[1;33m'
NO_COLOR='\033[0m' # No Color

function ECHO_OK() { echo -e "${GREEN_COLOR}${1}${NO_COLOR}"; }
function ECHO_KO() { echo -e "${RED_COLOR}${1}${NO_COLOR}"; }
function ECHO_INFO() { echo -e "${YELLOW_COLOR}${1}${NO_COLOR}"; }


if [ ! -r /configuracion/vault/vault.hcl ] || [ ! -r /configuracion/vault/privkey1.pem ] || [ ! -r /configuracion/vault/fullchain.pem ]; then
    ECHO_KO "ERROR: no puedo leer fichero de configuración o certificado"
    exit 2
fi
private_key=$(cat /configuracion/vault/privkey1.pem)
certificate=$(cat /configuracion/vault/fullchain.pem)

vault server -config=/configuracion/vault/vault.hcl & # Creamos el servidor de vault y lo dejamos en segundo plano
sleep 2 # Esperamos 2 segundos para que el servidor se levante
vault operator init > /tmp/temp.tmp # Guardamos el contenido del init (Unseal keys y root token) en un fichero temporal
if [ $? -eq 0 ]; then
    # Leemos el fichero del operator init linea a linea con el separador : y las variables name y key
    if [ ! -w /configuracion/vault/keys ]; then
        ECHO_KO "ERROR: no puedo escribir en el fichero de las claves"
        exit 2
    fi
    echo "" > /configuracion/vault/keys # Vaciamos el fichero
    while IFS=: read -r name key; do
        if [[ "$name" == *"Key"* ]]; then
            # Si el nombre (Lado izquierdo de los :) contiene <Key> se utiliza para hacer el unseal
            vault operator unseal $key >/dev/null
            echo "Unseal Key: $key" >>/configuracion/vault/keys
        elif [[ "$name" == *"Token"* ]]; then
            # Si el nombre contiene <Token> mostramos cual es el token root
            ECHO_INFO "Root token: $key"
            echo "Root Token: $key" >>/configuracion/vault/keys
            vault login $key >/dev/null
        fi
    done < /tmp/temp.tmp
    # Creamos el secreto secret y añadimos al clave privada y el certificado
    vault secrets enable -path=secret kv >/dev/null
    vault kv put secret/private_key content="$private_key" >/dev/null
    vault kv put secret/certificate content="$certificate" >/dev/null
    ECHO_OK "Certificado almacenado correctamente"

    vault token create -ttl=10h  -policy=root | head -n 3 | tail -n 1 | awk -F ' ' '{print $2}' > /configuracion/token
else # Si el servidor ya habia sido inicializado utilizaremos las claves para hacer el unseal
    while IFS=: read -r name key; do
        if [[ "$name" == *"Key"* ]]; then
            # Si el nombre (Lado izquierdo de los :) contiene <Key> se utiliza para hacer el unseal
            vault operator unseal $key >/dev/null
        elif [[ "$name" == *"Token"* ]]; then
            # Si el nombre contiene <Token> mostramos cual es el token root
            ECHO_INFO "Root token: $key"
            vault login $key >/dev/null
        fi
    done < /configuracion/vault/keys
fi
rm -rf /tmp/temp.tmp
wait
```

### Script de la base de datos

El script de la base de datos ya esta creado, solo hay que añadirlo a un fichero en la configuración e indicar a postgres que debe utilizarlo para lanzar las bases de datos, se encuentra en un fichero llamado **«init.sql»** junto a esta documentación. En este ejemplo se encuentra en «configuracion/db/init.sql», pero puedes utilizar cualquier ruta dentro de la carpeta de configuración. Pero su contenido es el siguiente:

```
--
--  Copyright (c) 2022 Daimler TSS GmbH
--
--  This program and the accompanying materials are made available under the
--  terms of the Apache License, Version 2.0 which is available at
--  https://www.apache.org/licenses/LICENSE-2.0
--
--  SPDX-License-Identifier: Apache-2.0
--
--  Contributors:
--       Daimler TSS GmbH - Initial SQL Query
--

-- THIS SCHEMA HAS BEEN WRITTEN AND TESTED ONLY FOR POSTGRES

-- table: edc_asset


CREATE TABLE IF NOT EXISTS edc_asset
(
    asset_id           VARCHAR NOT NULL,
    created_at         BIGINT  NOT NULL,
    properties         JSON    DEFAULT '{}',
    private_properties JSON    DEFAULT '{}',
    data_address       JSON    DEFAULT '{}',
    PRIMARY KEY (asset_id)
);

COMMENT ON COLUMN edc_asset.properties IS 'Asset properties serialized as JSON';
COMMENT ON COLUMN edc_asset.private_properties IS 'Asset private properties serialized as JSON';
COMMENT ON COLUMN edc_asset.data_address IS 'Asset DataAddress serialized as JSON';


CREATE TABLE IF NOT EXISTS edc_edr_entry
(
   transfer_process_id           VARCHAR NOT NULL PRIMARY KEY,
   agreement_id                  VARCHAR NOT NULL,
   asset_id                      VARCHAR NOT NULL,
   provider_id                   VARCHAR NOT NULL,
   contract_negotiation_id       VARCHAR,
   created_at                    BIGINT  NOT NULL
);

--
--  Copyright (c) 2022 Daimler TSS GmbH
--
--  This program and the accompanying materials are made available under the
--  terms of the Apache License, Version 2.0 which is available at
--  https://www.apache.org/licenses/LICENSE-2.0
--
--  SPDX-License-Identifier: Apache-2.0
--
--  Contributors:
--       Daimler TSS GmbH - Initial SQL Query
--       Microsoft Corporation - refactoring
--

-- table: edc_contract_definitions
-- only intended for and tested with H2 and Postgres!

CREATE TABLE IF NOT EXISTS edc_contract_definitions
(
    created_at             BIGINT  NOT NULL,
    contract_definition_id VARCHAR NOT NULL,
    access_policy_id       VARCHAR NOT NULL,
    contract_policy_id     VARCHAR NOT NULL,
    assets_selector        JSON    NOT NULL,
    private_properties     JSON,
    PRIMARY KEY (contract_definition_id)
);




-- Statements are designed for and tested with Postgres only!


CREATE TABLE IF NOT EXISTS edc_lease
(
    leased_by      VARCHAR               NOT NULL,
    leased_at      BIGINT,
    lease_duration INTEGER DEFAULT 60000 NOT NULL,
    lease_id       VARCHAR               NOT NULL
        CONSTRAINT lease_pk
            PRIMARY KEY
);

COMMENT ON COLUMN edc_lease.leased_at IS 'posix timestamp of lease';

COMMENT ON COLUMN edc_lease.lease_duration IS 'duration of lease in milliseconds';


CREATE UNIQUE INDEX IF NOT EXISTS lease_lease_id_uindex
    ON edc_lease (lease_id);




CREATE TABLE IF NOT EXISTS edc_contract_agreement
(
    agr_id            VARCHAR NOT NULL
        CONSTRAINT contract_agreement_pk
            PRIMARY KEY,
    provider_agent_id VARCHAR,
    consumer_agent_id VARCHAR,
    signing_date      BIGINT,
    start_date        BIGINT,
    end_date          INTEGER,
    asset_id          VARCHAR NOT NULL,
    policy            JSON
);



CREATE TABLE IF NOT EXISTS edc_contract_negotiation
(
    id                   VARCHAR           NOT NULL
        CONSTRAINT contract_negotiation_pk
            PRIMARY KEY,
    created_at           BIGINT            NOT NULL,
    updated_at           BIGINT            NOT NULL,
    correlation_id       VARCHAR,
    counterparty_id      VARCHAR           NOT NULL,
    counterparty_address VARCHAR           NOT NULL,
    protocol             VARCHAR           NOT NULL,
    type                 VARCHAR           NOT NULL,
    state                INTEGER DEFAULT 0 NOT NULL,
    state_count          INTEGER DEFAULT 0,
    state_timestamp      BIGINT,
    error_detail         VARCHAR,
    agreement_id         VARCHAR
        CONSTRAINT contract_negotiation_contract_agreement_id_fk
            REFERENCES edc_contract_agreement,
    contract_offers      JSON,
    callback_addresses   JSON,
    trace_context        JSON,
    pending              BOOLEAN DEFAULT FALSE,
    protocol_messages    JSON,
    lease_id             VARCHAR
        CONSTRAINT contract_negotiation_lease_lease_id_fk
            REFERENCES edc_lease
            ON DELETE SET NULL
);

COMMENT ON COLUMN edc_contract_negotiation.agreement_id IS 'ContractAgreement serialized as JSON';

COMMENT ON COLUMN edc_contract_negotiation.contract_offers IS 'List<ContractOffer> serialized as JSON';

COMMENT ON COLUMN edc_contract_negotiation.trace_context IS 'Map<String,String> serialized as JSON';


CREATE INDEX IF NOT EXISTS contract_negotiation_correlationid_index
    ON edc_contract_negotiation (correlation_id);

CREATE UNIQUE INDEX IF NOT EXISTS contract_negotiation_id_uindex
    ON edc_contract_negotiation (id);

CREATE UNIQUE INDEX IF NOT EXISTS contract_agreement_id_uindex
    ON edc_contract_agreement (agr_id);




--
--  Copyright (c) 2022 ZF Friedrichshafen AG
--
--  This program and the accompanying materials are made available under the
--  terms of the Apache License, Version 2.0 which is available at
--  https://www.apache.org/licenses/LICENSE-2.0
--
--  SPDX-License-Identifier: Apache-2.0
--
--  Contributors:
--       ZF Friedrichshafen AG - Initial SQL Query
--

-- Statements are designed for and tested with Postgres only!

-- table: edc_policydefinitions

CREATE TABLE IF NOT EXISTS edc_policydefinitions
(
    policy_id             VARCHAR NOT NULL,
    created_at            BIGINT  NOT NULL,
    permissions           JSON,
    prohibitions          JSON,
    duties                JSON,
    extensible_properties JSON,
    inherits_from         VARCHAR,
    assigner              VARCHAR,
    assignee              VARCHAR,
    target                VARCHAR,
    policy_type           VARCHAR NOT NULL,
    private_properties    JSON,
    PRIMARY KEY (policy_id)
);

COMMENT ON COLUMN edc_policydefinitions.permissions IS 'Java List<Permission> serialized as JSON';
COMMENT ON COLUMN edc_policydefinitions.prohibitions IS 'Java List<Prohibition> serialized as JSON';
COMMENT ON COLUMN edc_policydefinitions.duties IS 'Java List<Duty> serialized as JSON';
COMMENT ON COLUMN edc_policydefinitions.extensible_properties IS 'Java Map<String, Object> serialized as JSON';
COMMENT ON COLUMN edc_policydefinitions.policy_type IS 'Java PolicyType serialized as JSON';

CREATE UNIQUE INDEX IF NOT EXISTS edc_policydefinitions_id_uindex
    ON edc_policydefinitions (policy_id);


CREATE TABLE IF NOT EXISTS edc_transfer_process
(
    transferprocess_id       VARCHAR           NOT NULL
        CONSTRAINT transfer_process_pk
            PRIMARY KEY,
    type                       VARCHAR           NOT NULL,
    state                      INTEGER           NOT NULL,
    state_count                INTEGER DEFAULT 0 NOT NULL,
    state_time_stamp           BIGINT,
    created_at                 BIGINT            NOT NULL,
    updated_at                 BIGINT            NOT NULL,
    trace_context              JSON,
    error_detail               VARCHAR,
    resource_manifest          JSON,
    provisioned_resource_set   JSON,
    content_data_address       JSON,
    deprovisioned_resources    JSON,
    private_properties         JSON,
    callback_addresses         JSON,
    pending                    BOOLEAN  DEFAULT FALSE,
    transfer_type              VARCHAR,
    protocol_messages          JSON,
    data_plane_id              VARCHAR,
    correlation_id             VARCHAR,
    counter_party_address      VARCHAR,
    protocol                   VARCHAR,
    asset_id                   VARCHAR,
    contract_id                VARCHAR,
    data_destination           JSON,
    lease_id                   VARCHAR
            CONSTRAINT transfer_process_lease_lease_id_fk
                REFERENCES edc_lease
                ON DELETE SET NULL
);

COMMENT ON COLUMN edc_transfer_process.trace_context IS 'Java Map serialized as JSON';

COMMENT ON COLUMN edc_transfer_process.resource_manifest IS 'java ResourceManifest serialized as JSON';

COMMENT ON COLUMN edc_transfer_process.provisioned_resource_set IS 'ProvisionedResourceSet serialized as JSON';

COMMENT ON COLUMN edc_transfer_process.content_data_address IS 'DataAddress serialized as JSON';

COMMENT ON COLUMN edc_transfer_process.deprovisioned_resources IS 'List of deprovisioned resources, serialized as JSON';


CREATE UNIQUE INDEX IF NOT EXISTS transfer_process_id_uindex
    ON edc_transfer_process (transferprocess_id);

CREATE UNIQUE INDEX IF NOT EXISTS lease_lease_id_uindex
    ON edc_lease (lease_id);


CREATE TABLE IF NOT EXISTS edc_data_request
(
    datarequest_id      VARCHAR NOT NULL
        CONSTRAINT data_request_pk
            PRIMARY KEY,
    process_id          VARCHAR NOT NULL,
    connector_address   VARCHAR NOT NULL,
    protocol            VARCHAR NOT NULL,
    connector_id        VARCHAR,
    asset_id            VARCHAR NOT NULL,
    contract_id         VARCHAR NOT NULL,
    data_destination    JSON    NOT NULL,
    transfer_process_id VARCHAR NOT NULL
        CONSTRAINT data_request_transfer_process_id_fk
            REFERENCES edc_transfer_process
            ON UPDATE RESTRICT ON DELETE CASCADE
);


COMMENT ON COLUMN edc_data_request.data_destination IS 'DataAddress serialized as JSON';

CREATE UNIQUE INDEX IF NOT EXISTS data_request_id_uindex
    ON edc_data_request (datarequest_id);

CREATE UNIQUE INDEX IF NOT EXISTS lease_lease_id_uindex
    ON edc_lease (lease_id);
``` 

### Dockerfile

Ahora crearemos el dockerfile que nos lanzará el conector, dentro del propio fichero esta explicado que hace cada linea.
El jar del conector deberá estar en la misma carpeta que tengamos el Dockerfile, para poder acceder a el con la ruta **./connector.jar**

```
FROM gradle:8.7-jdk17 AS build

WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . /home/gradle/project
RUN gradle clean build

# Seleccionamos la imagen que vamos a utilizar para nuestro conector, la opcion -buster es necesaria para tener acceso al gestor de paquetes apt
FROM openjdk:17-slim-buster
# Marcamos el directorio de trabajo como /app dentro del conector
WORKDIR /app
# Copiamos el jar del conector en el directorio de trabajo
COPY --from=build /home/gradle/project/build/libs/connector.jar /app
# Seteamos la variable de entorno JVM_ARGS con el valor del argumento creado antes
ENV JVM_ARGS=$JVM_ARGS
# Solo para pruebas, actualiza apt, instala diversos paquetes de depuración, crea el alias ll y borramos los registros apt
# Punto de entrada de la imagen, comando que iniciará el funcionamiento de la «máquina»
ENTRYPOINT [ "sh", "-c", "exec java $JVM_ARGS -jar connector.jar"]  
```

### Docker-compose.yml

El docker compose es el fichero que nos permitirá desplegar instancias del conector de manera conjunta con la base de datos, en este caso el conector que estamos lanzando durante la documentación es el llamado «company2», al igual que el dockerfile estan todas las lineas explicadas.  En este fichero si que se ven de manerá conjunta ambos conectores, ya que este fichero no se encuentra junto a los ficheros que muestro junto a la documentación

```yml 
version: "2.4"
services:
  # Conectores
  company1:
    container_name: provider # Nombre del contenedor
    build:
      context: ../connecor # Donde se encuentra la raíz
      # args:
        # JVM_ARGS:  -Djava.net.preferIPv4Stack=true -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=0.0.0.0:1044
    environment: 
      EDC_FS_CONFIG: /configuracion/conf/configuration.properties # Ruta hasta las propiedades de configuración
      EDC_DATAPLANE_TOKEN_VALIDATION_ENDPOINT: https://sergio.arlabdevelopments.com:19192/control/token
    # Puertos, la "explicación" de cada uno se encuentra en sus propiedades especificas
    ports:
      - "19191:19191"
      - "19193:19193"
      - "19194:19194"
      - "19291:19291"
      - "19192:19192"
      - "1044:1044"

    volumes:
      - ./configuracion:/configuracion # Volumen añadido al contenedor de docker, para tener acceso a toda la configuración
    secrets:
      - unseal_keys

  company2:
    container_name: consumer # Nombre del contenedor
    build: 
      context: ../connecor # Donde se encuentra la raíz
      # args:
        # JVM_ARGS:  -Djava.net.preferIPv4Stack=true -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=0.0.0.0:1045
    environment: 
      EDC_FS_CONFIG: /configuracion/conf2/configuration.properties # Ruta hasta las propiedades de configuración
      EDC_DATAPLANE_TOKEN_VALIDATION_ENDPOINT: https://sergio.arlabdevelopments.com:29192/control/token
    # Puertos, la "explicación" de cada uno se encuentra en sus propiedades especificas
    ports:
      - "29191:29191"
      - "29193:29193"
      - "29194:29194"
      - "29291:29291"
      - "29192:29192"
      - "1045:1045"
    volumes:
      - ./configuracion:/configuracion # Volumen añadido al contenedor de docker, para tener acceso a toda la configuración
    secrets:
      - unseal_keys

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
    depends_on:
      - company2
  
  # Vault para almacenar los certificados
  hashicorp-vault:
    container_name: vault-server 
    image: vault:1.13.3 # Imagen del vaul que vamos a utilizar
    environment:
      VIRTUAL_HOST: vault-server.com
    ports:
      - "8200:8200" # Puerto tanto de la interfaz gráfica como del API
    volumes:
      - ./configuracion:/configuracion
    entrypoint: /configuracion/vault/unseal.sh




secrets:
  unseal_keys:
    file: ./configuracion/vault/keys
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

Controller: Es la clase que implementa la funcionalidad de la extensión, funciona en base a Jax-RS y esta es la estructura.

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

Con el objetivo de facilitar esta transferencia he creado un pequeño script que realiza la transferencia completa, para hacerlo de forma mas modular y cómoda he utilizado un script para cada uno de los apartados anteriores, el script sería el siguiente:

```sh
#!/bin/bash
finalizado=false

/«ruta_hasta_demas_scripts»/crearArtefacto

artefacto=$(/«ruta_hasta_demas_scripts»/obtenerCatalogo)
echo "ID del artefacto a negociar: $artefacto"

contrato=$(/«ruta_hasta_demas_scripts»/negociarContrato $artefacto)
echo "ID del contrato: $contrato"
while ! $finalizado; do 
  estado=$(/«ruta_hasta_demas_scripts»/comprobarEstado $contrato "estado")
  if [ $estado = "FINALIZED" ]; then 
    finalizado=true
  fi
done
contratoExitoso=$(/«ruta_hasta_demas_scripts»/comprobarEstado $contrato "id")

echo "El contract agreement id: $contratoExitoso"

# Aquí puedo poner el pull o el push, según necesite
# /«ruta_hasta_demas_scripts»/transferenciaPush $contratoExitoso
/«ruta_hasta_demas_scripts»/transferenciaPull $contratoExitoso
```

### crearArtefacto
Este script engloba la creación del dataplane, del asset, de la política y del contrato
```sh
#!/bin/bash

curl -H 'Content-Type: application/json' \
    --header 'x-api-key: password' \
     -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },
       "@id": "http-pull-provider-dataplane",
       "url": "https://sergio.arlabdevelopments.com:19192/control/transfer",
       "allowedSourceTypes": [    "HttpData"  ],
       "allowedDestTypes": [    "HttpProxy",
         "HttpData"  ],
       "properties": {    "https://w3id.org/edc/v0.0.1/ns/publicApiUrl": "https://sergio.arlabdevelopments.com:19291/public/"  }}' \
     -X POST "https://sergio.arlabdevelopments.com:19193/management/v2/dataplanes" -s | jq

curl -H 'Content-Type: application/json' \
    --header 'x-api-key: password' \
     -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },
       "@id": "http-pull-consumer-dataplane",
       "url": "https://sergio.arlabdevelopments.com:29192/control/transfer",
       "allowedSourceTypes": [    "HttpData"  ],
       "allowedDestTypes": [    "HttpProxy",
         "HttpData"  ],
       "properties": {    "https://w3id.org/edc/v0.0.1/ns/publicApiUrl": "https://sergio.arlabdevelopments.com:29291/public/"  }}' \
     -X POST "https://sergio.arlabdevelopments.com:29193/management/v2/dataplanes" -s | jq

curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },
  "@id": "assetId",
  "properties": {    "name": "product description",
    "contenttype": "application/json"  },
  "dataAddress": {    "type": "HttpData",
    "name": "Test asset",
    "baseUrl": "https://www.compraonline.alcampo.es/api/v5/products/search?term=pan",
    "proxyPath": "true"  }}' \
  --header 'x-api-key: password' \
  -H 'content-type: application/json' https://sergio.arlabdevelopments.com:19193/management/v3/assets \
-s | jq

curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
    "odrl": "http://www.w3.org/ns/odrl/2/"  },
  "@id": "aPolicy",
  "policy": {    "@context": "http://www.w3.org/ns/odrl.jsonld",
    "@type": "Set",
    "permission": [],
    "prohibition": [],
    "obligation": []  }}' \
  --header 'x-api-key: password' \
  -H 'content-type: application/json' https://sergio.arlabdevelopments.com:19193/management/v2/policydefinitions \
-s | jq

curl -d '{  "@context": {    "@vocab": "https://w3id.org/edc/v0.0.1/ns/"  },
  "@id": "1",
  "accessPolicyId": "aPolicy",
  "contractPolicyId": "aPolicy",
  "assetsSelector": []}' \
  --header 'x-api-key: password' \
  -H 'content-type: application/json' https://sergio.arlabdevelopments.com:19193/management/v2/contractdefinitions \
-s | jq
```

### obtenerCatalogo

```sh
#!/bin/bash

curl -X POST "https://sergio.arlabdevelopments.com:29193/management/v2/catalog/request" \
  --header 'x-api-key: password' \
  -H 'Content-Type: application/json' \
  -d '{
    "@context": {
      "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
    },
    "counterPartyAddress": "https://sergio.arlabdevelopments.com:19194/protocol",
    "protocol": "dataspace-protocol-http"
    }' \
-s | jq '."dcat:dataset"."odrl:hasPolicy"."@id"' | tr -d \"

```

### negociarContrato

```sh
#!/bin/bash

curl -d "{  \"@context\": {    \"@vocab\": \"https://w3id.org/edc/v0.0.1/ns/\"  },
  \"@type\": \"ContractRequest\",
  \"counterPartyAddress\": \"https://sergio.arlabdevelopments.com:19194/protocol\",
  \"protocol\": \"dataspace-protocol-http\",
  \"policy\": {    \"@context\": \"http://www.w3.org/ns/odrl.jsonld\",
    \"@id\": \"$1\",
    \"@type\": \"Offer\",
    \"assigner\": \"provider\",
    \"target\": \"assetId\"  }}" \
  -X POST -H 'content-type: application/json' https://sergio.arlabdevelopments.com:29193/management/v2/contractnegotiations \
  --header 'x-api-key: password' \
-s | jq '."@id"'  | tr -d \" 
```

### comprobarEstado

En este script comprobamos que el contrato ya este aceptado para poder hacer la transferencia

```sh
#!/bin/bash
set -e

if [ $2 = "estado" ]; then
  curl -X GET "https://sergio.arlabdevelopments.com:29193/management/v2/contractnegotiations/$1" \
    --header 'x-api-key: password' \
    --header 'Content-Type: application/json' \
  -s  | jq '.state' |  tr -d \"
else
  curl -X GET "https://sergio.arlabdevelopments.com:29193/management/v2/contractnegotiations/$1" \
    --header 'x-api-key: password' \
    --header 'Content-Type: application/json' \
  -s  | jq '.contractAgreementId' |  tr -d \"
fi
```

### transferenciaPull

```sh
#!/bin/bash

curl -X POST "https://sergio.arlabdevelopments.com:29193/management/v2/transferprocesses" \
  --header 'x-api-key: password' \
  -H "Content-Type: application/json" \
  -d "{
    \"@context\": {
      \"@vocab\": \"https://w3id.org/edc/v0.0.1/ns/\"
    },
    \"@type\": \"TransferRequestDto\",
    \"connectorId\": \"provider\",
    \"counterPartyAddress\": \"https://sergio.arlabdevelopments.com:19194/protocol\",
    \"contractId\": \"$1\",
    \"assetId\": \"assetId\",
    \"protocol\": \"dataspace-protocol-http\",
    \"transferType\":\"HttpData-PULL\"
  }" \
  -s | jq
```

### transferenciaPush

```sh
#!/bin/bash

curl -X POST "https://sergio.arlabdevelopments.com:29193/management/v2/transferprocesses" \
  --header 'x-api-key: password' \
  -H "Content-Type: application/json" \
  -d "{
    \"@context\": {
      \"@vocab\": \"https://w3id.org/edc/v0.0.1/ns/\"
    },
    \"@type\": \"TransferRequestDto\",
    \"connectorId\": \"provider\",
    \"counterPartyAddress\": \"https://sergio.arlabdevelopments.com:19194/protocol\",
    \"contractId\": \"$1\",
    \"assetId\": \"assetId\",
    \"protocol\": \"dataspace-protocol-http\",
    \"transferType\":\"HttpData-PUSH\",
    \"dataDestination\": {
      \"type\": \"HttpData\",
      \"baseUrl\": \"https://sergio.arlabdevelopments.com:29191/api/push\"
    }
  }" \
-s | jq
```

