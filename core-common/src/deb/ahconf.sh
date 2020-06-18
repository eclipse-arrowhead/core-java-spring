#!/bin/sh

AH_CONF_DIR="/etc/arrowhead"
AH_CLOUDS_DIR="${AH_CONF_DIR}/clouds"
AH_SYSTEMS_DIR="${AH_CONF_DIR}/systems"

db_get arrowhead-core-common/cert_password; AH_PASS_CERT=$RET
db_get arrowhead-core-common/cloudname; AH_CLOUD_NAME=$RET
db_get arrowhead-core-common/operator; AH_OPERATOR=$RET
AH_COMPANY=arrowhead # hard-coded to the Arrowhead Framework
AH_COUNTRY=eu # hard-coded to the Arrowhead Framework

db_get arrowhead-core-common/relay_master_cert; AH_RELAY_MASTER_CERT=$RET
db_get arrowhead-core-common/domain_name; AH_DOMAIN_NAME=$RET

OWN_IP=`ip -o -4  address show  | awk ' NR==2 { gsub(/\/.*/, "", $4); print $4 } '`
echo $OWN_IP

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
            -dname "CN=${cn}, OU=${AH_OPERATOR}, O=${AH_COMPANY}, C=${AH_COUNTRY}" \
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

ah_cert_export () {
    src_path=${1}
    dst_name=${2}
    dst_path=${3}

    src_file="${src_path}/${dst_name}.p12"
    dst_file="${dst_path}/${dst_name}.crt"

    if [ ! -f "${dst_file}" ]; then
        keytool -exportcert \
            -rfc \
            -alias ${dst_name} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | openssl x509 \
            -out ${dst_file}

        chown :arrowhead ${dst_file}
        chmod 640 ${dst_file}
    fi
}

ah_cert_export_pub () {
    src_path=${1}
    dst_name=${2}
    dst_path=${3}

    src_file="${src_path}/${dst_name}.p12"
    dst_file="${dst_path}/${dst_name}.pub"

    if [ ! -f "${dst_file}" ]; then
        keytool -exportcert \
            -rfc \
            -alias ${dst_name} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | openssl x509 \
            -out ${dst_file} \
            -noout \
            -pubkey

        chown :arrowhead ${dst_file}
        chmod 640 ${dst_file}
    fi
}

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

ah_cert_signed () {
    dst_path=${1}
    dst_name=${2}
    cn=${3}
    src_path=${4}
    src_name=${5}

    src_file="${src_path}/${src_name}.p12"
    dst_file="${dst_path}/${dst_name}.p12"
    
    if [ ! -f "${dst_file}" ]; then
        ah_cert ${dst_path} ${dst_name} ${cn}

        keytool -export \
            -alias ${src_name} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | keytool -import \
            -trustcacerts \
            -alias ${src_name} \
            -keystore ${dst_file} \
            -keypass ${AH_PASS_CERT} \
            -storepass ${AH_PASS_CERT} \
            -storetype PKCS12 \
            -noprompt

        keytool -certreq \
            -alias ${dst_name} \
            -keypass ${AH_PASS_CERT} \
            -keystore ${dst_file} \
            -storepass ${AH_PASS_CERT} \
        | keytool -gencert \
            -alias ${src_name} \
            -keypass ${AH_PASS_CERT} \
            -keystore ${src_file} \
            -storepass ${AH_PASS_CERT} \
            -validity 3650 \
            -ext BasicConstraints=ca:true,pathlen:2 \
			-ext SubjectAlternativeName=IP:127.0.0.1,DNS:localhost,DNS:`hostname`,IP:${OWN_IP} \
        | keytool -importcert \
            -alias ${dst_name} \
            -keypass ${AH_PASS_CERT} \
            -keystore ${dst_file} \
            -storepass ${AH_PASS_CERT} \
            -noprompt
    fi
}

ah_ip_valid () {
	my_ip=${1}
	
	if expr "$my_ip" : '[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*$' >/dev/null; then
		OLD_IFS=${IFS}
		IFS=.
		set $my_ip
		for quad in 1 2 3 4; do
			if eval [ \$$quad -gt 255 ]; then
				IFS=OLD_IFS
				return 1
			fi
		done
		IFS=${OLD_IFS}
		return 0
	else
		return 1
	fi
}
 
ah_subject_alternative_names() {
    host=${1}
    ip=${2}

    local san="IP:127.0.0.1,DNS:localhost,DNS:${host},IP:${ip}"
    
    if [ ! -z ${AH_DOMAIN_NAME} ]; then
        if ah_ip_valid ${AH_DOMAIN_NAME}; then
            san=${san},IP:${AH_DOMAIN_NAME}
        else
            san=${san},DNS:${AH_DOMAIN_NAME}
        fi
    fi

    echo "${san}"
}

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

    path="${path_dir}/${name}"
    file="${path}/${name}.p12"
    src_file="${AH_CLOUDS_DIR}/${AH_CLOUD_NAME}.p12"

    if [ ! -f "${file}" ]; then
		san=$(ah_subject_alternative_names ${host} ${ip})
		
		ah_cert ${path} ${name} "${name}.${AH_CLOUD_NAME}.${AH_OPERATOR}.arrowhead.eu" ${passwd}

        keytool -export \
            -alias ${AH_CLOUD_NAME} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | keytool -import \
            -trustcacerts \
            -alias ${AH_CLOUD_NAME} \
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
            -alias ${AH_CLOUD_NAME} \
            -keypass ${AH_PASS_CERT} \
            -keystore ${src_file} \
            -storepass ${AH_PASS_CERT} \
            -validity 3650 \
			-ext SubjectAlternativeName=${san} \
        | keytool -importcert \
            -alias ${name} \
            -keypass ${passwd} \
            -keystore ${file} \
            -storepass ${passwd} \
            -noprompt
		
        ah_cert_import "${AH_CONF_DIR}" "master" "${path}" ${name} ${passwd}
    fi
}

# Create the keystore for the CA system
# Apart from being a regular Arrowhead compliant system keystore,
# it also has to contain the private key of the cloud
ah_ca_keystore () {
    ca_sys_name=${1}        # name of the CA system
    host=${2}               # hostname of the CA system (default: `hostname`)
    ip=${3}                 # IP address of the CA system (default: $OWN_IP)
    cloud_keystore=${4}     # path of the cloud keystore (default: "${AH_CLOUDS_DIR}/${AH_CLOUD_NAME}.p12")
    ca_password=${5}        # password used in the CA keystore (default: $AH_PASS_CERT)
    cloud_password=${6}     # password used in the cloud keystore (default: $AH_PASS_CERT)
    cloud_alias_src=${7}    # cloud cert alias in the cloud keystore (default: $AH_CLOUD_NAME)
    cloud_alias_dst=${8}    # cloud cert alias in the CA keystore (default: $AH_CLOUD_NAME)
    
    if [ -z ${host} ]; then
        host=`hostname`
    fi

    if [ -z ${ip} ]; then
        ip=${OWN_IP}
    fi

    if [ -z ${cloud_keystore} ]; then
        cloud_keystore="${AH_CLOUDS_DIR}/${AH_CLOUD_NAME}.p12"
    fi

    if [ -z ${ca_password} ]; then
        ca_password=${AH_PASS_CERT}
    fi

    if [ -z ${cloud_password} ]; then
        cloud_password=${ca_password}
    fi

    if [ -z ${cloud_alias_src} ]; then
        cloud_alias_src=${AH_CLOUD_NAME}
    fi

    if [ -z ${cloud_alias_dst} ]; then
        cloud_alias_dst=${cloud_alias_src}
    fi

    ca_sys_dir="${AH_SYSTEMS_DIR}/${ca_sys_name}"
    ca_keystore="${ca_sys_dir}/${ca_sys_name}.p12"

    if [ ! -f "${ca_keystore}" ]; then
        san=$(ah_subject_alternative_names ${host} ${ip})
        
        # generate CA system keypair
        ah_cert ${ca_sys_dir} ${ca_sys_name} "${ca_sys_name}.${AH_CLOUD_NAME}.${AH_OPERATOR}.arrowhead.eu" ${ca_password}

        # import cloud keypair
        # this is necessary, because CA signs system certificates with the cloud cert
        keytool -importkeystore \
                -srckeypass ${cloud_password} \
                -srcstorepass ${cloud_password} \
                -destkeypass ${ca_password} \
                -deststorepass  ${ca_password} \
                -srcalias ${cloud_alias_src} \
                -destalias ${cloud_alias_dst} \
                -srckeystore "${cloud_keystore}" \
                -destkeystore "${ca_keystore}" \
                -deststoretype PKCS12
        
        # sign CA system cert with the cloud cert
        keytool -certreq \
                -alias ${ca_sys_name} \
                -keypass ${ca_password} \
                -keystore "${ca_keystore}" \
                -storepass ${ca_password} \
        | keytool -gencert \
                -alias ${cloud_alias_dst} \
                -keypass ${ca_password} \
                -keystore "${ca_keystore}" \
                -storepass ${ca_password} \
                -validity 3650 \
                -ext SubjectAlternativeName=${san} \
        | keytool -importcert \
                -alias ${ca_sys_name} \
                -keypass ${ca_password} \
                -keystore "${ca_keystore}" \
                -storepass ${ca_password} \
                -noprompt

        # import root certificate
        ah_cert_import "${AH_CONF_DIR}" "master" "${ca_sys_dir}" ${ca_sys_name} ${ca_password}
    fi
}

ah_cert_trust () {
    dst_path=${1}
    src_path=${2}
    src_name=${3}
	passwd=${4}
	
	if [ -z ${passwd} ]; then
		passwd=${AH_PASS_CERT}
	fi

    src_file="${src_path}/${src_name}.p12"
    dst_file="${dst_path}/truststore.p12"
    
    if [ ! -f "${dst_file}" ]; then
        keytool -export \
            -alias ${src_name} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | keytool -import \
            -trustcacerts \
            -alias ${src_name} \
            -keystore ${dst_file} \
            -keypass ${passwd} \
            -storepass ${passwd} \
            -storetype PKCS12 \
            -noprompt
			
			if [ ! -z ${AH_RELAY_MASTER_CERT} ]; then
				keytool -import \
						-trustcacerts \
						-file ${AH_RELAY_MASTER_CERT} \
						-alias relay.arrowhead.eu \
						-keystore ${dst_file} \
						-keypass ${passwd} \
						-storepass ${passwd} \
						-storetype PKCS12 \
						-noprompt
			fi

        chown :arrowhead ${dst_file}
        chmod 640 ${dst_file}
    fi
}

ah_db_tables_and_user () {
	mysql_user_name=${1}
	priv_file_name=${2}
	system_passwd=${3}
	
	db_get arrowhead-core-common/db_host; db_host=$RET || true

    if mysql -u root -h ${db_host} -e "SHOW DATABASES" >/dev/null 2>/dev/null; then
        mysql -u root -h ${db_host} < /usr/share/arrowhead/conf/create_arrowhead_tables.sql
		mysql -u root -h ${db_host} <<EOF
DROP USER IF EXISTS '${mysql_user_name}'@'localhost';
DROP USER IF EXISTS '${mysql_user_name}'@'%';
CREATE USER	'${mysql_user_name}'@'localhost' IDENTIFIED BY '${system_passwd}';
CREATE USER '${mysql_user_name}'@'%' IDENTIFIED BY '${system_passwd}';
EOF
		mysql -u root -h ${db_host} < /usr/share/arrowhead/conf/${priv_file_name}
    else
        db_input critical arrowhead-core-common/mysql_password_root || true
        db_go || true
        db_get arrowhead-core-common/mysql_password_root; AH_MYSQL_ROOT=$RET

        OPT_FILE="$(mktemp -q --tmpdir "arrowhead-core-common.XXXXXX")"
        trap 'rm -f "${OPT_FILE}"' EXIT
        chmod 0600 "${OPT_FILE}"

        cat >"${OPT_FILE}" <<EOF
[client]
password="${AH_MYSQL_ROOT}"
EOF

        mysql --defaults-extra-file="${OPT_FILE}" -h ${db_host} -u root < /usr/share/arrowhead/conf/create_arrowhead_tables.sql
		mysql --defaults-extra-file="${OPT_FILE}" -h ${db_host} -u root <<EOF
DROP USER IF EXISTS '${mysql_user_name}'@'localhost';
DROP USER IF EXISTS '${mysql_user_name}'@'%';
CREATE USER	'${mysql_user_name}'@'localhost' IDENTIFIED BY '${system_passwd}';
CREATE USER '${mysql_user_name}'@'%' IDENTIFIED BY '${system_passwd}';
EOF
		mysql --defaults-extra-file="${OPT_FILE}" -h ${db_host} -u root < /usr/share/arrowhead/conf/${priv_file_name}
    fi
}

ah_transform_log_file () {
	log_path=${1}
	
	mv ${log_path}/log4j2.xml ${log_path}/log4j2.xml.orig
	sed -r '\|^.*<Property name=\"LOG_DIR\">|s|(.*)$|<Property name=\"LOG_DIR\">/var/log/arrowhead</Property>|' ${log_path}/log4j2.xml.orig > ${log_path}/log4j2.xml
	rm ${log_path}/log4j2.xml.orig
	chown :arrowhead ${log_path}/log4j2.xml
	chmod 640 ${log_path}/log4j2.xml
} 
