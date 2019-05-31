#!/usr/bin/env bash

NODE_IP=$1
NODE_PORT=$2

java  "-Djavax.net.ssl.keyStore=keystore" "-Djavax.net.ssl.keyStorePassword=sdis1822" "-Djavax.net.ssl.trustStore=truststore" "-Djavax.net.ssl.trustStorePassword=sdis1822" Node $NODE_IP $NODE_PORT 