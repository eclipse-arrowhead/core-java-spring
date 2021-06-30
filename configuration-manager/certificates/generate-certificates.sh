#!/bin/bash
#*******************************************************************************
# Copyright (c) 2020 Bosch.IO GmbH[ and others]

# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.

# SPDX-License-Identifier: EPL-2.0
#*******************************************************************************

CLOUD_DOMAIN="example.corp.arrowhead.eu"
CLIENT_ID="configuration"
CLIENT_SERIAL=1
CA_KEYSTORE="example.corp.arrowhead.eu.p12"
CA_KEYSTORE_PASS="aeloim9do8hoe7AaWo7johpe"
CA_CERT_NAME="ca-cert.pem"
CA_KEY_NAME="ca-key.key"
VALIDITY=3650

# Use the password from application.yaml
CLIENT_KEYSTORE_PASS="conf-test-pw"
KEY_PASS=${CLIENT_KEYSTORE_PASS}


# Generate rsa key
openssl genrsa -aes256 -passout pass:${KEY_PASS} -out ${CLIENT_ID}.pass.key 2048
openssl rsa -passin pass:${KEY_PASS} -in ${CLIENT_ID}.pass.key -out ${CLIENT_ID}.key
rm ${CLIENT_ID}.pass.key

# generate CSR
openssl req -new -key ${CLIENT_ID}.key -out ${CLIENT_ID}.csr -subj "/C=DE/ST=Berlin/L=Berlin/O=Arrowhead/OU=AHT/CN=${CLIENT_ID}.${CLOUD_DOMAIN}" -config openssl.cnf

# Extract private key and certificate from CA
openssl pkcs12 -password pass:${CA_KEYSTORE_PASS} -in ${CA_KEYSTORE} -nodes -nocerts -out ${CA_KEY_NAME}
openssl pkcs12 -password pass:${CA_KEYSTORE_PASS} -in ${CA_KEYSTORE} -nodes -clcerts -out ${CA_CERT_NAME}

# sign new certificate using CSR
openssl x509 -req -days ${VALIDITY} -in ${CLIENT_ID}.csr -CA ${CA_CERT_NAME} -CAkey ${CA_KEY_NAME} -set_serial ${CLIENT_SERIAL} -extfile openssl.cnf -extensions req_ext -out ${CLIENT_ID}.pem

# combine to create PKCS
openssl pkcs12 -export -out ${CLIENT_ID}.full.p12 -name "${CLIENT_ID}.${CLOUD_DOMAIN}" -inkey ${CLIENT_ID}.key -in ${CLIENT_ID}.pem -certfile ${CA_CERT_NAME} -password pass:${CLIENT_KEYSTORE_PASS}

echo "Successfully generated client keystore, use the following password to open:"
echo ${CLIENT_KEYSTORE_PASS}

# delete byproducts
rm ${CA_KEY_NAME}
rm ${CA_CERT_NAME}
rm ${CLIENT_ID}.pem
rm ${CLIENT_ID}.csr
rm ${CLIENT_ID}.key