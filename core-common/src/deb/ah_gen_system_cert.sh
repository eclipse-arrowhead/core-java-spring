#!/bin/bash -e

. /usr/share/debconf/confmodule
. /usr/share/arrowhead/conf/ahconf.sh

if [ "$#" -lt 2 ]; then
  echo "Syntax: ${0} SYSTEM_NAME PASSWORD [SYSTEM_HOST] [SYSTEM_IP] [ADDITIONAL_HOST_OR_IP]"
  exit 1
fi

SYSTEM_NAME=${1}
PASSWORD=${2}
SYSTEM_HOST=${3}
SYSTEM_IP=${4}
DOMAIN_NAME=${5}

if [ -z "${SYSTEM_HOST}" ]; then
  SYSTEM_HOST=`hostname`
fi

if [ -z "${SYSTEM_IP}" ]; then
  SYSTEM_IP=${OWN_IP}
fi

OLD_AH_DOMAIN_NAME=${AH_DOMAIN_NAME}
if [ -z "${DOMAIN_NAME}" ]; then
  AH_DOMAIN_NAME=${DOMAIN_NAME}
fi

SYSTEM_STORE="${SYSTEM_NAME}/${SYSTEM_NAME}.p12"

echo "Generating certificate for '${SYSTEM_NAME}'" >&2
mkdir ${SYSTEM_NAME}
ah_cert_signed_system ${SYSTEM_NAME} ${PASSWORD} ${SYSTEM_HOST} ${SYSTEM_IP} .

AH_DOMAIN_NAME=${OLD_AH_DOMAIN_NAME}

SYSTEM_64PUB=$(\
  sudo keytool -exportcert -rfc -keystore "${SYSTEM_STORE}" -storepass ${PASSWORD} -v -alias "${SYSTEM_NAME}" \
  | openssl x509 -pubkey -noout \
  | sed '1d;$d' \
  | tr -d '\n'\
)

echo "Authentication info:"
echo ${SYSTEM_64PUB}

ah_cert_trust ./${SYSTEM_NAME} ${AH_CLOUDS_DIR} ${AH_CLOUD_NAME} ${PASSWORD}

echo "Done"
