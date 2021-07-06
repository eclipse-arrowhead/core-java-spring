#!/bin/bash -e

if [ "$#" -lt 4 ]; then
  echo "Syntax: ${0} RELAY_NAME PASSWORD RELAY_MASTER_CERT RELAY_MASTER_PASSWORD [RELAY_HOST] [RELAY_IP]"
  exit 1
fi

RELAY_NAME=${1}
PASSWORD=${2}
RELAY_MASTER_CERT=${3}
RELAY_MASTER_PASSWORD=${4}
RELAY_HOST=${5}
RELAY_IP=${6}

RELAY_STORE="${RELAY_NAME}/${RELAY_NAME}.p12"
RELAY_TRUSTSTORE="${RELAY_NAME}/${RELAY_NAME}_truststore.p12"

echo "Generating certificate for '${RELAY_NAME}'" >&2
mkdir ${RELAY_NAME}

if [ -z ${RELAY_HOST} ]; then
  RELAY_HOST=`hostname`
fi

if [ -z ${RELAY_IP} ]; then
  RELAY_IP=`ip -o -4  address show  | awk ' NR==2 { gsub(/\/.*/, "", $4); print $4 } '`
fi

cn="${RELAY_NAME}.relay.arrowhead.eu"

gen_cmd="-genkeypair"
keytool ${gen_cmd} --help >/dev/null 2>&1 || gen_cmd='-genkey'

keytool ${gen_cmd} \
  -alias ${RELAY_NAME} \
  -keyalg RSA \
  -keysize 2048 \
  -dname "CN=${cn}, OU=relay, O=arrowhead, C=eu" \
  -validity 3650 \
  -keypass ${PASSWORD} \
  -keystore ${RELAY_STORE} \
  -storepass ${PASSWORD} \
  -storetype PKCS12 

chown :arrowhead ${RELAY_STORE}
chmod 640 ${RELAY_STORE}

RELAY_MASTER_ALIAS=relay.arrowhead.eu
keytool -export \
  -alias ${RELAY_MASTER_ALIAS} \
  -storepass ${RELAY_MASTER_PASSWORD} \
  -keystore ${RELAY_MASTER_CERT} \
  | keytool -import \
  -trustcacerts \
  -alias ${RELAY_MASTER_ALIAS} \
  -keystore ${RELAY_STORE} \
  -keypass ${PASSWORD} \
  -storepass ${PASSWORD} \
  -storetype PKCS12 \
  -noprompt

keytool -certreq \
  -alias ${RELAY_NAME} \
  -keypass ${PASSWORD} \
  -keystore ${RELAY_STORE} \
  -storepass ${PASSWORD} \
  | keytool -gencert \
  -alias ${RELAY_MASTER_ALIAS} \
  -keypass ${RELAY_MASTER_PASSWORD} \
  -keystore ${RELAY_MASTER_CERT} \
  -storepass ${RELAY_MASTER_PASSWORD} \
  -validity 3650 \
  -ext SubjectAlternativeName=IP:127.0.0.1,DNS:localhost,DNS:${RELAY_HOST},IP:${RELAY_IP} \
  | keytool -importcert \
  -alias ${RELAY_NAME} \
  -keypass ${PASSWORD} \
  -keystore ${RELAY_STORE} \
  -storepass ${PASSWORD} \
  -noprompt

#trust store
keytool -import \
  -trustcacerts \
  -file /etc/arrowhead/master.crt \
  -alias arrowhead.eu \
  -keystore ${RELAY_TRUSTSTORE} \
  -keypass ${PASSWORD} \
  -storepass ${PASSWORD} \
  -storetype PKCS12 \
  -noprompt

chown :arrowhead ${RELAY_TRUSTSTORE}
chmod 640 ${RELAY_TRUSTSTORE}

echo "Done"
