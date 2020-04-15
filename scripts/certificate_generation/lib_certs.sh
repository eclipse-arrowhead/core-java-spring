# Created by Emanuel Palm (https://github.com/emanuelpalm)

# Uses `keytool`, typically bundled with Java installations, to generate and
# sign keystores, certificates and truststores for Arrowhead roots, clouds,
# systems and system operators.

# The environment variable PASSWORD is used to set the password of all
# created key stores. If it is not set, "123456" is used by default. The
# password can be changed for individual keystores and truststores later if
# desired.
if [[ -z "${PASSWORD}" ]]; then
  export PASSWORD="123456"
fi

# Creates a root certificate keystore and a corresponding PEM certificate.
#
# If the keystore already exists, the operation does nothing. If the PEM
# cerificate is missing, it will be created either from the already
# existing or new keystore.
#
# @param $1 Path to desired root certificate keystore.
# @param $2 Desired Common Name of root certificate.
create_root_keystore() {
  local ROOT_KEYSTORE=$1
  local ROOT_KEY_ALIAS=$2
  local ROOT_CERT_FILE="${ROOT_KEYSTORE%.*}.crt"

  if [ ! -f "${ROOT_KEYSTORE}" ]; then
    echo -e "\e[34mCreating \e[33m${ROOT_KEYSTORE}\e[34m ...\e[0m"
    mkdir -p "$(dirname "${ROOT_KEYSTORE}")"
    rm -f "${ROOT_CERT_FILE}"

    keytool -genkeypair -v \
      -keystore "${ROOT_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -keyalg "RSA" \
      -keysize "2048" \
      -validity "3650" \
      -alias "${ROOT_KEY_ALIAS}" \
      -keypass:env "PASSWORD" \
      -dname "CN=${ROOT_KEY_ALIAS}" \
      -ext "BasicConstraints=ca:true,pathlen:3"
  fi

  if [ ! -f "${ROOT_CERT_FILE}" ]; then
    echo -e "\e[34mCreating \e[33m${ROOT_CERT_FILE}\e[34m ...\e[0m"

    keytool -exportcert -v \
      -keystore "${ROOT_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -alias "${ROOT_KEY_ALIAS}" \
      -keypass:env "PASSWORD" \
      -file "${ROOT_CERT_FILE}" \
      -rfc
  fi
}

# Creates a cloud certificate keystore, containing a certificate signed by the
# specified root, and a corresponding PEM certificate.
#
# If the keystore already exists, the operation does nothing. If the PEM
# cerificate is missing, it will be created either from the already
# existing or new keystore. If the root keystore has changed since an
# existing cloud keystore was created, it is recreated.
#
# @param $1 Path to root certificate keystore.
# @param $2 Common Name of root certificate.
# @param $3 Path to desired cloud certificate keystore.
# @param $4 Desired Common Name of cloud certificate.
create_cloud_keystore() {
  local ROOT_KEYSTORE=$1
  local ROOT_KEY_ALIAS=$2
  local ROOT_CERT_FILE="${ROOT_KEYSTORE%.*}.crt"
  local CLOUD_KEYSTORE=$3
  local CLOUD_KEY_ALIAS=$4
  local CLOUD_CERT_FILE="${CLOUD_KEYSTORE%.*}.crt"

  if [ -f "${CLOUD_KEYSTORE}" ] && [ "${ROOT_KEYSTORE}" -nt "${CLOUD_KEYSTORE}" ]; then
    rm -f "${CLOUD_KEYSTORE}"
  fi

  if [ ! -f "${CLOUD_KEYSTORE}" ]; then
    echo -e "\e[34mCreating \e[33m${CLOUD_KEYSTORE}\e[34m ...\e[0m"
    mkdir -p "$(dirname "${CLOUD_KEYSTORE}")"
    rm -f "${CLOUD_CERT_FILE}"

    keytool -genkeypair -v \
      -keystore "${CLOUD_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -keyalg "RSA" \
      -keysize "2048" \
      -validity "3650" \
      -alias "${CLOUD_KEY_ALIAS}" \
      -keypass:env "PASSWORD" \
      -dname "CN=${CLOUD_KEY_ALIAS}" \
      -ext "BasicConstraints=ca:true,pathlen:2"

    keytool -importcert -v \
      -keystore "${CLOUD_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -alias "${ROOT_KEY_ALIAS}" \
      -file "${ROOT_CERT_FILE}" \
      -trustcacerts \
      -noprompt

    keytool -certreq -v \
      -keystore "${CLOUD_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -alias "${CLOUD_KEY_ALIAS}" \
      -keypass:env "PASSWORD" |
      keytool -gencert -v \
        -keystore "${ROOT_KEYSTORE}" \
        -storepass:env "PASSWORD" \
        -validity "3650" \
        -alias "${ROOT_KEY_ALIAS}" \
        -keypass:env "PASSWORD" \
        -ext "BasicConstraints=ca:true,pathlen:2" \
        -rfc |
      keytool -importcert \
        -keystore "${CLOUD_KEYSTORE}" \
        -storepass:env "PASSWORD" \
        -alias "${CLOUD_KEY_ALIAS}" \
        -keypass:env "PASSWORD" \
        -trustcacerts \
        -noprompt
  fi

  if [ ! -f "${CLOUD_CERT_FILE}" ]; then
    echo -e "\e[34mCreating \e[33m${CLOUD_CERT_FILE}\e[34m ...\e[0m"

    keytool -exportcert -v \
      -keystore "${CLOUD_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -alias "${CLOUD_KEY_ALIAS}" \
      -keypass:env "PASSWORD" \
      -file "${CLOUD_CERT_FILE}" \
      -rfc
  fi
}

# Creates a system certificate keystore, containing a certificate signed by
# the specified cloud, and a corresponding PEM certificate and public key
# file.
#
# If the keystore already exists, the operation does nothing. If either the
# PEM cerificate or public key file is missing, those will be created either
# from the already existing or new keystore. If the cloud keystore has changed
# since an existing system keystore was created, it is recreated.
#
# @param $1 Path to root certificate keystore.
# @param $2 Common Name of root certificate.
# @param $3 Path to cloud certificate keystore.
# @param $4 Common Name of cloud certificate.
# @param $5 Path to desired system certificate keystore.
# @param $6 Desired Common Name of system certificate.
create_system_keystore() {
  local ROOT_KEYSTORE=$1
  local ROOT_KEY_ALIAS=$2
  local ROOT_CERT_FILE="${ROOT_KEYSTORE%.*}.crt"
  local CLOUD_KEYSTORE=$3
  local CLOUD_KEY_ALIAS=$4
  local CLOUD_CERT_FILE="${CLOUD_KEYSTORE%.*}.crt"
  local SYSTEM_KEYSTORE=$5
  local SYSTEM_KEY_ALIAS=$6
  local SYSTEM_PUB_FILE="${SYSTEM_KEYSTORE%.*}.pub"
  local SAN=$7

  if [ -f "${SYSTEM_KEYSTORE}" ] && [ "${CLOUD_KEYSTORE}" -nt "${SYSTEM_KEYSTORE}" ]; then
    rm -f "${SYSTEM_KEYSTORE}"
  fi

  if [ ! -f "${SYSTEM_KEYSTORE}" ]; then
    echo -e "\e[34mCreating \e[33m${SYSTEM_KEYSTORE}\e[34m ...\e[0m"
    mkdir -p "$(dirname "${SYSTEM_KEYSTORE}")"
    rm -f "${SYSTEM_PUB_FILE}"

    keytool -genkeypair -v \
      -keystore "${SYSTEM_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -keyalg "RSA" \
      -keysize "2048" \
      -validity "3650" \
      -alias "${SYSTEM_KEY_ALIAS}" \
      -keypass:env "PASSWORD" \
      -dname "CN=${SYSTEM_KEY_ALIAS}" \
      -ext "SubjectAlternativeName=${SAN}"

    keytool -importcert -v \
      -keystore "${SYSTEM_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -alias "${ROOT_KEY_ALIAS}" \
      -file "${ROOT_CERT_FILE}" \
      -trustcacerts \
      -noprompt

    keytool -importcert -v \
      -keystore "${SYSTEM_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -alias "${CLOUD_KEY_ALIAS}" \
      -file "${CLOUD_CERT_FILE}" \
      -trustcacerts \
      -noprompt

    keytool -certreq -v \
      -keystore "${SYSTEM_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -alias "${SYSTEM_KEY_ALIAS}" \
      -keypass:env "PASSWORD" |
      keytool -gencert -v \
        -keystore "${CLOUD_KEYSTORE}" \
        -storepass:env "PASSWORD" \
        -validity "3650" \
        -alias "${CLOUD_KEY_ALIAS}" \
        -keypass:env "PASSWORD" \
        -ext "SubjectAlternativeName=${SAN}" \
        -rfc |
      keytool -importcert \
        -keystore "${SYSTEM_KEYSTORE}" \
        -storepass:env "PASSWORD" \
        -alias "${SYSTEM_KEY_ALIAS}" \
        -keypass:env "PASSWORD" \
        -trustcacerts \
        -noprompt
  fi

  if [ ! -f "${SYSTEM_PUB_FILE}" ]; then
    echo -e "\e[34mCreating \e[33m${SYSTEM_PUB_FILE}\e[34m ...\e[0m"

    keytool -list \
      -keystore "${SYSTEM_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -alias "${SYSTEM_KEY_ALIAS}" \
      -rfc |
      openssl x509 \
        -inform pem \
        -pubkey \
        -noout >"${SYSTEM_PUB_FILE}"
  fi
}

# Creates a system operator certificate keystore, containing a certificate
# signed by the specified cloud, a PEM certificate file, a CA (Certificate
# Authority) file, a public key file and a private key file.
#
# If the keystore already exists, the operation does nothing. If the PEM
# cerificate is missing, it will be created either from the already
# existing or new keystore. If the cloud keystore has changed since an
# existing system operator keystore was created, it is recreated.
#
# @param $1 Path to root certificate keystore.
# @param $2 Common Name of root certificate.
# @param $3 Path to cloud certificate keystore.
# @param $4 Common Name of cloud certificate.
# @param $5 Path to desired system operator certificate keystore.
# @param $6 Desired Common Name of system operator certificate.
create_sysop_keystore() {
  local ROOT_KEYSTORE=$1
  local ROOT_CERT_FILE="${ROOT_KEYSTORE%.*}.crt"
  local CLOUD_KEYSTORE=$3
  local CLOUD_CERT_FILE="${CLOUD_KEYSTORE%.*}.crt"
  local SYSOP_KEYSTORE=$5
  local SYSOP_KEY_ALIAS=$6
  local SYSOP_PUB_FILE="${SYSOP_KEYSTORE%.*}.pub"
  local SYSOP_CA_FILE="${SYSOP_KEYSTORE%.*}.ca"
  local SYSOP_CERT_FILE="${SYSOP_KEYSTORE%.*}.crt"
  local SYSOP_KEY_FILE="${SYSOP_KEYSTORE%.*}.key"

  local CREATE_KEYSTORE_OR_PUB_FILE=0

  if [ -f "${SYSOP_KEYSTORE}" ] && [ "${CLOUD_KEYSTORE}" -nt "${SYSOP_KEYSTORE}" ]; then
    rm -f "${SYSOP_KEYSTORE}"
  fi

  if [ ! -f "${SYSOP_KEYSTORE}" ]; then
    rm -f "${SYSOP_CA_FILE}"
    rm -f "${SYSOP_CERT_FILE}"
    rm -f "${SYSOP_KEY_FILE}"
    CREATE_KEYSTORE_OR_PUB_FILE=1
  fi

  if [ ! -f "${SYSOP_PUB_FILE}" ]; then
    CREATE_KEYSTORE_OR_PUB_FILE=1
  fi

  if [[ "${CREATE_KEYSTORE_OR_PUB_FILE}" == "1" ]]; then
    create_system_keystore "$1" "$2" "$3" "$4" "$5" "$6" "dns:localost,ip:127.0.0.1"
  fi

  if [ ! -f "${SYSOP_CA_FILE}" ]; then
    echo -e "\e[34mCreating \e[33m${SYSOP_CA_FILE}\e[34m ...\e[0m"

    cat "${ROOT_CERT_FILE}" >"${SYSOP_CA_FILE}"
    cat "${CLOUD_CERT_FILE}" >>"${SYSOP_CA_FILE}"
  fi

  if [ ! -f "${SYSOP_CERT_FILE}" ]; then
    echo -e "\e[34mCreating \e[33m${SYSOP_CERT_FILE}\e[34m ...\e[0m"

    keytool -exportcert -v \
      -keystore "${SYSOP_KEYSTORE}" \
      -storepass:env "PASSWORD" \
      -alias "${SYSOP_KEY_ALIAS}" \
      -keypass:env "PASSWORD" \
      -rfc >>"${SYSOP_CERT_FILE}"
  fi

  if [ ! -f "${SYSOP_KEY_FILE}" ]; then
    echo -e "\e[34mCreating \e[33m${SYSOP_KEY_FILE}\e[34m ...\e[0m"

    openssl pkcs12 \
      -in "${SYSOP_KEYSTORE}" \
      -passin env:PASSWORD \
      -out "${SYSOP_KEY_FILE}" \
      -nocerts \
      -nodes
  fi
}

# Creates truststore and populates it with identified certificates.
#
# If the truststore already exists, the operation does nothing. Unless,
# however, any of the identified certificate files are newer than the
# the truststore, in which case the truststore is recreated.
#
# $1        Path to desired truststore.
# $2,4,6... Paths to certificate file ".crt".
# $3,5,7... Common Names of certificates in ".crt" files.
create_truststore() {
  local TRUSTSTORE=$1
  local ARGC=$#
  local ARGV=("$@")

  if [ -f "${TRUSTSTORE}" ]; then
    for ((j = 1; j < ARGC; j = j + 2)); do
      local FILE="${ARGV[j]}"
      if [ -f "${FILE}" ] && [ "${TRUSTSTORE}" -nt "${FILE}" ]; then
        rm -f "${FILE}"
      fi
    done
  fi

  if [ ! -f "${TRUSTSTORE}" ]; then
    echo -e "\e[34mCreating \e[33m${TRUSTSTORE}\e[34m ...\e[0m"
    mkdir -p "$(dirname "${TRUSTSTORE}")"

    for ((j = 1; j < ARGC; j = j + 2)); do
      keytool -importcert -v \
        -keystore "${TRUSTSTORE}" \
        -storepass:env "PASSWORD" \
        -file "${ARGV[j]}" \
        -alias "${ARGV[j + 1]}" \
        -trustcacerts \
        -noprompt
    done
  fi
}

# Makes sure that interrupts are not ignored while generating certificates.
exit_script() {
  echo -e "\e[1;31mAborting ...\e[0m"
  trap - SIGINT SIGTERM
  kill -- -$$
}
trap exit_script SIGINT SIGTERM