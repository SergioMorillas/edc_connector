FROM gradle:8.7-jdk17 AS build

WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . /home/gradle/project
RUN gradle clean build

# Seleccionamos la imagen que vamos a utilizar para nuestro conector, la opcion -buster es necesaria para tener acceso al gestor de paquetes apt
FROM openjdk:17-slim-buster
# Argumentos que le pasaremos a la JVM, por ejemplo una instancia del depurador
ARG JVM_ARGS="-Xms8192m"
# Instalamos curl y borramos los registros de apt para ahorrar espacio
RUN apt update && apt install -y curl # rm -rf /var/cache/apt/archives /var/lib/apt/lists

    # && rm -rf /var/cache/apt/archives /var/lib/apt/lists # DESCOMENTAR 
# Marcamos el directorio de trabajo como /app dentro del conector
WORKDIR /app
# Copiamos el jar del conector en el directorio de trabajo
COPY --from=build /home/gradle/project/build/libs/connector.jar /app
# Seteamos la variable de entorno JVM_ARGS con el valor del argumento creado antes
ENV JVM_ARGS=$JVM_ARGS
# Solo para pruebas, actualiza apt, instala diversos paquetes de depuración, crea el alias ll y borramos los registros apt
RUN apt update; apt install -y vim tcpflow jq iproute2 tcpdump; echo "alias ll='ls -laiFh --color=always'">>~/.bashrc
# Punto de entrada de la imagen, comando que iniciará el funcionamiento de la «máquina»
ENTRYPOINT [ "sh", "-c", "exec java $JVM_ARGS -jar connector.jar"]  