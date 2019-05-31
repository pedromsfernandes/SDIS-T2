#!/usr/bin/env bash

NODE_IP=$1
NODE_PORT=$(($2 + 80))

PROTOCOL=$3
OPND_1=$4
OPND_2=""

if [ $# -eq 5 ]; then
    OPND_2=$5
fi

java "-Djavax.net.ssl.trustStore=truststore" "-Djavax.net.ssl.trustStorePassword=sdis1822" TestApp $NODE_IP $NODE_PORT $PROTOCOL $OPND_1 $OPND_2 