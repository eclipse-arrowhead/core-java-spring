#!/bin/sh

AH_CONF_DIR=$PWD

echo $AH_CONF_DIR

AH_CLOUDS_DIR="${AH_CONF_DIR}"
AH_SYSTEMS_DIR="${AH_CONF_DIR}"

AH_PASS_CERT="123456"

if [ -z ${1} ];  then
	 echo "Parameter 1 (system name) must not be empty";
	 exit 1;
fi

if [ -z ${2} ];  then
         echo "Parameter 2 (password) must not be empty";
         exit 1;
fi

if [ -z ${3} ];  then
         echo "Parameter 3 (hostname) must not be empty";
         exit 1;
fi

if [ -z ${4} ];  then
         echo "Parameter 4 (ip) must not be empty";
         exit 1;
fi

if [ -z ${5} ];  then
         echo "Parameter 5 (cloud name) must not be empty";
         exit 1;
fi



AH_CLOUD_NAME=${5}

if [ -z ${6} ];
	then
		AH_CLOUD_ALIAS=${5}
	else
		AH_CLOUD_ALIAS=${6}
fi

AH_OPERATOR="aitia"
AH_COMPANY="arrowhead"
AH_COUNTRY="eu"

OWN_IP=`ip -o -4  address show  | awk ' NR==2 { gsub(/\/.*/, "", $4); print $4 } '`
echo $OWN_IP

 SYSTEM_NAME=${1}
 SYSTEM_PASSWORD=${2}
 SYSTEM_HOSTNAME=${3}
 SYSTEM_IP=${4}


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
            -dname "CN=${cn}" \
            -validity 3650 \
            -keypass ${passwd} \
            -keystore ${file} \
            -storepass ${passwd} \
            -storetype PKCS12 \
            -ext BasicConstraints=ca:true,pathlen:3 \
			-ext SubjectAlternativeName=IP:127.0.0.1,DNS:localhost,DNS:`hostname`,IP:${OWN_IP}

        chown :arrowhead ${file}
        chmod 640 ${file}
    fi
}

#*****************************************************
#***    ah_cert_import
#*****************************************************
ah_cert_import () {
    src_path=${1}
    src_name=${2}
    dst_path=${3}
    dst_name=${4}
	passwd=${5}

	if [ -z ${passwd} ]; then
		passwd=${AH_PASS_CERT}
	fi

    src_file="${src_path}/${src_name}.crt"
    dst_file="${dst_path}/${dst_name}.p12"

    keytool -import \
        -trustcacerts \
        -file ${src_file} \
        -alias ${src_name} \
        -keystore ${dst_file} \
        -keypass ${passwd} \
        -storepass ${passwd} \
        -storetype PKCS12 \
        -noprompt
}

#***********************************************************************
#  ah_cert_signed_system
#***********************************************************************


ah_cert_signed_system () {
    name=${1}
	passwd=${2}
	host=${3}
	ip=${4}
	path_dir=${5}

	if [ -z ${passwd} ] ; then
		passwd=${AH_PASS_CERT}
	fi

	if [ -z ${host} ]; then
		host=`hostname`
	fi

	if [ -z ${ip} ]; then
		ip=${OWN_IP}
	fi

	if [ -z ${path_dir} ]; then
		path_dir=${AH_SYSTEMS_DIR}
	fi

    path="${path_dir}"
    file="${name}.p12"
    src_file="${AH_CLOUDS_DIR}/${AH_CLOUD_NAME}.p12"

    if [ ! -f "${file}" ]; then
		ah_cert ${path} ${name} "${name}.${AH_CLOUD_NAME}.${AH_OPERATOR}.arrowhead.eu" ${passwd}

        keytool -export \
            -alias ${AH_CLOUD_ALIAS} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | keytool -import \
            -trustcacerts \
            -alias ${AH_CLOUD_ALIAS} \
            -keystore ${file} \
            -keypass ${passwd} \
            -storepass ${passwd} \
            -storetype PKCS12 \
            -noprompt

        keytool -certreq \
            -alias ${name} \
            -keypass ${passwd} \
            -keystore ${file} \
            -storepass ${passwd} \
        | keytool -gencert \
            -alias ${AH_CLOUD_ALIAS} \
            -keypass ${AH_PASS_CERT} \
            -keystore ${src_file} \
            -storepass ${AH_PASS_CERT} \
            -validity 3650 \
			-ext SubjectAlternativeName=IP:127.0.0.1,DNS:localhost,DNS:${host},IP:${ip} \
        | keytool -importcert \
            -alias ${name} \
            -keypass ${passwd} \
            -keystore ${file} \
            -storepass ${passwd} \
            -noprompt

        ah_cert_import "${AH_CONF_DIR}" "master" "${path}" ${name} ${passwd}
    fi
}
ah_cert_signed_system ${SYSTEM_NAME} ${SYSTEM_PASSWORD} ${SYSTEM_HOSTNAME} ${SYSTEM_IP} 

