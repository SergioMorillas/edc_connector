plugins { // Aquí añadimos los plugins
    `java-library` // El plugin base
    id("application") // El plugin que utilizaremos para setear la clase main
    id("com.github.johnrengelman.shadow") version "7.1.2" // El plugin shadow, que sirve para para crear el jar autoejecutable con todas las dependenciar
}

repositories { // El repositorio del que vamos a sacar todas las librerias
    mavenCentral()
}
val edcGroup = "org.eclipse.edc"                                                    // Seteamos el grupo de Eclipse que vamos a utilizar
val edcVersion = "0.7.0"                                                            // Seteamos la version de los paquetes de Eclipse que vamos a utilizar
val postgresGroup = "org.postgresql"                                                // Seteamos el grupo de postgres que vamos a utilizar
val postgresVersion = "42.6.0"                                                      // Seteamos la version de los paquetes de postgres que vamos a utilizar
val jakartaGroup ="jakarta.ws.rs"                                                   // Seteamos el grupo de jakarta que vamos a utilizar
val jakartaVersion ="3.1.0"                                                         // Seteamos la version de los paquetes de jakarta que vamos a utilizar

dependencies {
    implementation("${edcGroup}:data-plane-selector-api:${edcVersion}")             // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-selector-core:${edcVersion}")            // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-control-api:${edcVersion}")              // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-public-api:${edcVersion}")               // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-core:${edcVersion}")                     // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector
    implementation("${edcGroup}:data-plane-http:${edcVersion}")                     // Todas las extensiones básicas ==> Extensiones para la transferencia de datos y el nucleo del conector

    implementation("${edcGroup}:control-plane-core:${edcVersion}")                  // Todas las extensiones básicas ==> Nucleo del control de transferencias
    implementation("${edcGroup}:control-plane-api:${edcVersion}")                   // Todas las extensiones básicas ==> Nucleo del control de transferencias
    implementation("${edcGroup}:control-plane-api-client:${edcVersion}")            // Todas las extensiones básicas ==> API para clientes del control de transferencias
    implementation("${edcGroup}:management-api:${edcVersion}")                      // Todas las extensiones básicas ==> API de administración
    implementation("${edcGroup}:dsp:${edcVersion}")                                 // Todas las extensiones básicas ==> Implementación del DataSpaceProtocol
    implementation("${edcGroup}:configuration-filesystem:${edcVersion}")            // Todas las extensiones básicas ==> Para mantener la configuración en local
    implementation("${edcGroup}:vault-hashicorp:${edcVersion}")                     // Todas las extensiones básicas ==> Para mantener la configuración en local
    implementation("${edcGroup}:iam-mock:${edcVersion}")                            // Todas las extensiones básicas ==> Implementación del servicio de identificación
    implementation("${edcGroup}:http:${edcVersion}")                                // Todas las extensiones básicas ==> Control de comunicaciones HTTP
    implementation("${edcGroup}:transfer-data-plane:${edcVersion}")                 // Todas las extensiones básicas ==> Control de comunicaciones HTTP
    implementation("${edcGroup}:jwt-spi:${edcVersion}")                             // Todas las extensiones básicas ==> Control de comunicaciones HTTP
    implementation("${edcGroup}:transfer-pull-http-receiver:${edcVersion}")         // Todas las extensiones básicas ==> Control de comunicaciones HTTP


    implementation("${edcGroup}:edr-cache-api:${edcVersion}")                    // EDR
    implementation("${edcGroup}:edr-store-receiver:${edcVersion}")               // EDR
    implementation("${edcGroup}:edr-store-core:${edcVersion}")                   // EDR


    implementation("${edcGroup}:asset-index-sql:${edcVersion}")                     // Todas las extensiones de SQL -==> Indice de assets
    implementation("${edcGroup}:policy-definition-store-sql:${edcVersion}")         // Todas las extensiones de SQL -==> Definición de politicas
    implementation("${edcGroup}:contract-definition-store-sql:${edcVersion}")       // Todas las extensiones de SQL -==> Definición de contratos
    implementation("${edcGroup}:contract-negotiation-store-sql:${edcVersion}")      // Todas las extensiones de SQL -==> Negociación de contratos
    implementation("${edcGroup}:transfer-process-store-sql:${edcVersion}")          // Todas las extensiones de SQL -==> Proceso de transferencias
    implementation("${edcGroup}:sql-pool-apache-commons:${edcVersion}")             // Todas las extensiones de SQL -==> Herramientas de SQL
    implementation("${edcGroup}:transaction-local:${edcVersion}")                   // Todas las extensiones de SQL -==> Transacciones locales
    implementation("${edcGroup}:transaction-datasource-spi:${edcVersion}")          // Todas las extensiones de SQL -==> Transacciones locales
    implementation("${edcGroup}:control-plane-sql:${edcVersion}")                   // Todas las extensiones de SQL -==> Transacciones locales
    implementation("${edcGroup}:edr-index-sql:${edcVersion}")                    // Todas las extensiones de SQL -==> Transacciones locales
    implementation("${postgresGroup}:postgresql:${postgresVersion}")                // Plugin estandar de postgres

    // Plugin propio
    implementation("${jakartaGroup}:jakarta.ws.rs-api:${jakartaVersion}")           // Manejo del API de jakarta
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.shadowJar {
    isZip64 = true
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {        // Aquí creamos el jar autoejecutable que utilizaremos, como el shadow plugin de mvn
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
}