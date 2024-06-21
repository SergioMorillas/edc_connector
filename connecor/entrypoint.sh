#!/bin/bash
parada=true
vueltas=10
actual=0
while  $parada || [ $actual -lq $vueltas ]; do
    if [ ! -r /configuracion/token ]; then
        echo "ERROR: no puedo leer el fichero con las claves o aun no existe, intentos restantes $((vueltas-actual))"
        sleep 2
        actual=$(($actual+1))
    else
        parada=false
    fi
done
token=$(cat /configuracion/token)
java -jar -Dedc.vault.hashicorp.token=${token} connector.jar