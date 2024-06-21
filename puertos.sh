#!/bin/bash
set -e

RED_COLOR='\033[1;31m'
GREEN_COLOR='\033[1;32m'
YELLOW_COLOR='\033[1;33m'
NO_COLOR='\033[0m' # No Color

function ECHO_OK() { echo -e "${GREEN_COLOR}${1}${NO_COLOR}"; }
function ECHO_KO() { echo -e "${RED_COLOR}${1}${NO_COLOR}"; }
function ECHO_INFO() { echo -e "${YELLOW_COLOR}${1}${NO_COLOR}"; }

ip="localhost"
if [ $# -gt 0  ]; then
        ip=$1
fi

ECHO_OK "Comprobando puertos de la IP: ${ip}"

for i in {1..64000}
do
        if  echo "" >/dev/tcp/${ip}/${i} 2>/dev/null; then
                ECHO_INFO "Puerto ${i} abierto"
        fi
done