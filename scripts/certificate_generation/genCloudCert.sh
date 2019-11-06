#!/bin/sh

CLOUD_NAME=${1}
CLOUD_PASSWORD="${2}"
MASTER_CERT_NAME="${3}"
MASTER_CERT_PATH="${4}"
MASTER_CERT_PASSWORD="${5}"

MASTER_CERT_ALIAS="arrowhead.eu"

ah_cert () {
    dst_path=${1}
    dst_name=${2}
    cn=${3}
	passwd=${4}
	
	if [ -z ${passwd} ]; then
		passwd=${AH_PASS_CERT}
	fi

    file="${dst_path}/${dst_name}.p12"

    # The command has been renamed in newer versions of keytool
    gen_cmd="-genkeypair"
    keytool ${gen_cmd} --help >/dev/null 2>&1 || gen_cmd='-genkey'

    if [ ! -f "${file}" ]; then
        keytool ${gen_cmd} \
            -alias ${dst_name} \
            -keyalg RSA \
            -keysize 2048 \
            -dname "CN=${dst_name}.aitia.arrowhead.eu" \
            -validity 7200 \
            -keypass ${CLOUD_PASSWORD} \
            -keystore ${file} \
            -storepass ${CLOUD_PASSWORD} \
            -storetype PKCS12 \
            -ext BasicConstraints=ca:true,pathlen:2 

        chown :arrowhead ${file}
        chmod 640 ${file}
    fi
}

ah_cert_signed () {
    dst_path=$PWD
    dst_name=${CLOUD_NAME}
    cn="${CLOUD_NAME}.aitia.arrowhead.eu"
    src_path=${MASTER_CERT_PATH}
    src_name=${MASTER_CERT_NAME}

    src_file="${src_path}/${src_name}.p12"
    dst_file="${dst_path}/${dst_name}.p12"
    
    if [ ! -f "${dst_file}" ]; then
        echo .........................................1........
	    ah_cert ${dst_path} ${CLOUD_NAME} "${CLOUD_NAME}.aitia.arrowhead.eu" ${passwd}

        echo .........................................2.........

        keytool -export \
            -alias ${MASTER_CERT_ALIAS} \
            -storepass ${MASTER_CERT_PASSWORD} \
            -keystore ${src_file} \
        | keytool -import \
            -trustcacerts \
            -alias ${MASTER_CERT_ALIAS} \
            -keystore ${dst_file} \
            -keypass ${CLOUD_PASSWORD} \
            -storepass ${CLOUD_PASSWORD}  \
            -storetype PKCS12 \
            -noprompt

        keytool -certreq \
            -alias ${dst_name} \
            -keypass ${CLOUD_PASSWORD} \
            -keystore ${dst_file} \
            -storepass ${CLOUD_PASSWORD} \
        | keytool -gencert \
            -alias ${MASTER_CERT_ALIAS} \
            -keypass ${MASTER_CERT_PASSWORD} \
            -keystore ${src_file} \
            -storepass ${MASTER_CERT_PASSWORD} \
            -validity 3650 \
            -ext BasicConstraints=ca:true,pathlen:2 \
        | keytool -importcert \
            -alias ${dst_name} \
            -keypass ${CLOUD_PASSWORD} \
            -keystore ${dst_file} \
            -storepass ${CLOUD_PASSWORD} \
            -noprompt
    fi
}
ah_cert_signed

