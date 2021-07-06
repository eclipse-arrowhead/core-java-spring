#!/bin/bash

if [[ -z "$AH_CONF_DIR" ]]; then
  AH_CONF_DIR="/etc/arrowhead"
fi
if [[ -z "$AH_CLOUDS_DIR" ]]; then
  AH_CLOUDS_DIR="${AH_CONF_DIR}/clouds"
fi
if [[ -z "$AH_SYSTEMS_DIR" ]]; then
  AH_SYSTEMS_DIR="${AH_CONF_DIR}/systems"
fi
if [[ -z "$AH_CONF_FILE" ]]; then
  AH_CONF_FILE="${AH_CONF_DIR}/arrowhead.cfg"
fi
if [[ -z "$AH_RELAYS_DIR" ]]; then
  AH_RELAYS_DIR="${AH_CONF_DIR}/relays"
fi

err() {
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $*" >&2
}

# Get property from configuration file
ah_get_conf_prop() {
  echo "$(grep -Po "${AH_CONF_FILE}" -e "()(?<=^$1=).*")"
}

# Set property in configuration file
ah_set_conf_prop() {
  local out="$(echo "$2" | sed 's/[\\\/\&]/\\&/g')"
  if ! sed -i "/^$1=.*/{s//$1=${out}/;h};\${x;/./{x;q0};x;q1}" "${AH_CONF_FILE}"; then
    err "ah_set_conf_prop: Could not find $1"
  fi
}

AH_PASS_CERT="$(ah_get_conf_prop cert_password)"
AH_CLOUD_NAME="$(ah_get_conf_prop cloudname)"
AH_OPERATOR="$(ah_get_conf_prop operator)"
AH_COMPANY=arrowhead # hard-coded to the Arrowhead Framework
AH_COUNTRY=eu # hard-coded to the Arrowhead Framework

AH_RELAY_MASTER_CERT="$(ah_get_conf_prop relay_master_cert)"
AH_DOMAIN_NAME="$(ah_get_conf_prop domain_name)"

AH_SYSTEM_INTERFACE="$(ah_get_conf_prop system_interface)"

AH_NETWORK_INTERFACES="$(ah_get_conf_prop san_interfaces)"
SAN_IPS="$(ah_get_conf_prop san_ips)"
SAN_DNS="$(ah_get_conf_prop san_dns)"

OWN_IP="$(echo "${AH_SYSTEM_INTERFACE}" | awk ' { print $2 } ')"

readarray -t SAN_INTERFACE_IPS<<<"$(echo "${AH_NETWORK_INTERFACES}" | awk ' BEGIN { RS = "," } { print $2 } ')"

ah_subject_alternative_names() {
  ips="$(echo "${@}" | grep -o -P '(?<=-ips ).*?((?= -dns)|$)')"
  dns="$(echo "${@}" | grep -o -P '(?<=-dns ).*?((?= -ips)|$)')"
  out="IP:127.0.0.1,DNS:localhost"

  for ip in ${ips}
  do
    if [[ ${out} != *"${ip}"* ]]; then
      out="${out},IP:${ip}"
    fi
  done

  for domain in ${dns}
  do
    if [[ ${out} != *"${domain}"* ]]; then
      out="${out},DNS:${domain}"
    fi
  done

  echo "${out}"
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

  # Get a formatted subject alternative names
  sans="$(ah_subject_alternative_names -ips "${SAN_INTERFACE_IPS[@]}" "${SAN_IPS}" -dns `hostname` "${SAN_DNS}")"

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
      -ext SubjectAlternativeName=${sans}

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

    # Get a formatted subject alternative names
    sans="$(ah_subject_alternative_names -ips "${SAN_INTERFACE_IPS[@]}" "${SAN_IPS}" -dns `hostname` "${SAN_DNS}")"

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
      -ext SubjectAlternativeName=${sans} \
      | keytool -importcert \
      -alias ${dst_name} \
      -keypass ${AH_PASS_CERT} \
      -keystore ${dst_file} \
      -storepass ${AH_PASS_CERT} \
      -noprompt
  fi
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
    ah_cert ${path} ${name} "${name}.${AH_CLOUD_NAME}.${AH_OPERATOR}.arrowhead.eu" ${passwd}

    # Get a formatted subject alternative names
    sans="$(ah_subject_alternative_names -ips "${SAN_INTERFACE_IPS[@]}" "${SAN_IPS}" ${ip} -dns ${host} "${SAN_DNS}")"

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
      -ext SubjectAlternativeName=${sans} \
      | keytool -importcert \
      -alias ${name} \
      -keypass ${passwd} \
      -keystore ${file} \
      -storepass ${passwd} \
      -noprompt

    ah_cert_import "${AH_CONF_DIR}" "master" "${path}" ${name} ${passwd}
  fi
}

ah_cert_signed_relay () {
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
    path_dir=${AH_RELAYS_DIR}
  fi

  path="${path_dir}/${name}"
  file="${path}/${name}.p12"
  src_file="${AH_RELAYS_DIR}/relay.p12"

  if [ ! -f "${file}" ]; then
    ah_cert ${path} ${name} "${name}.relay.arrowhead.eu" ${passwd}

    # Get a formatted subject alternative names
    sans="$(ah_subject_alternative_names -ips "${SAN_INTERFACE_IPS[@]}" "${SAN_IPS}" ${ip} -dns ${host} "${SAN_DNS}")"

    keytool -export \
      -alias relay \
      -storepass ${AH_PASS_CERT} \
      -keystore ${src_file} \
      | keytool -import \
      -trustcacerts \
      -alias relay \
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
      -alias relay \
      -keypass ${AH_PASS_CERT} \
      -keystore ${src_file} \
      -storepass ${AH_PASS_CERT} \
      -validity 3650 \
      -ext SubjectAlternativeName=${sans} \
      | keytool -importcert \
      -alias ${name} \
      -keypass ${passwd} \
      -keystore ${file} \
      -storepass ${passwd} \
      -noprompt

    ah_cert_import "${AH_CONF_DIR}" "master" "${path}" ${name} ${passwd}
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

# Only use this in maintainer scripts.
# Do not call this in the `arrowhead` command
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
