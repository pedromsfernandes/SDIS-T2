#!/usr/bin/env bash

NODE_IP=$1
NODE_PORT=$2
RING_NODE_IP=$3
RING_NODE_PORT=$4

java "-Djavax.net.ssl.keyStore=keystore" "-Djavax.net.ssl.keyStorePassword=sdis1822" "-Djavax.net.ssl.trustStore=truststore" "-Djavax.net.ssl.trustStorePassword=sdis1822" Node $NODE_IP $NODE_PORT $RING_NODE_IP $RING_NODE_PORT