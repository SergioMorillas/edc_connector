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