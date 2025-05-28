#!/bin/bash

# Created by Emanuel Palm (https://github.com/emanuelpalm)

cd "$(dirname "$0")" || exit
source "lib_certs.sh"
cd ..

# ROOT

create_root_keystore \
  "cloud-root/crypto/root.p12" "arrowhead.eu"

# RELAY "CLOUD" (Keep this as is, assuming you might interact with external relays)

create_cloud_keystore \
  "cloud-root/crypto/root.p12" "arrowhead.eu" \
  "cloud-relay/crypto/conet-demo-relay.p12" "conet-demo-relay.ltu.arrowhead.eu"

create_system_keystore \
  "cloud-root/crypto/root.p12" "arrowhead.eu" \
  "cloud-relay/crypto/conet-demo-relay.p12" "conet-demo-relay.ltu.arrowhead.eu" \
  "cloud-relay/crypto/alpha.p12" "alpha.conet-demo-relay.ltu.arrowhead.eu" \
  "dns:alpha.relay,ip:172.23.1.11,dns:localhost,ip:127.0.0.1"

create_truststore \
  "cloud-relay/crypto/truststore.p12" \
  "cloud-root/crypto/root.crt" "arrowhead.eu"

# CONSUMER CLOUD (Modified for Localhost Setup)

# Use a generic local cloud name for your local setup
LOCAL_CLOUD_NAME="local.arrowhead.eu" # <-- NEW: Define your local cloud name

create_cloud_keystore \
  "cloud-root/crypto/root.p12" "arrowhead.eu" \
  "cloud-data-consumer/crypto/${LOCAL_CLOUD_NAME}.p12" "${LOCAL_CLOUD_NAME}"

create_consumer_system_keystore() {
  SYSTEM_NAME=$1
  # Construct the full FQDN for the system based on the LOCAL_CLOUD_NAME
  SYSTEM_FQDN="${SYSTEM_NAME}.${LOCAL_CLOUD_NAME}"
  # Include localhost and arrowhead-core, plus the system's own FQDN
  SYSTEM_SAN="DNS:localhost,IP:127.0.0.1,DNS:arrowhead-core,DNS:${SYSTEM_FQDN}"

  create_system_keystore \
    "cloud-root/crypto/root.p12" "arrowhead.eu" \
    "cloud-data-consumer/crypto/${LOCAL_CLOUD_NAME}.p12" "${LOCAL_CLOUD_NAME}" \
    "cloud-data-consumer/crypto/${SYSTEM_NAME}.p12" "${SYSTEM_FQDN}" \
    "${SYSTEM_SAN}"
}

create_consumer_system_keystore "authorization"
create_consumer_system_keystore "contractproxy"
create_consumer_system_keystore "dataconsumer"
create_consumer_system_keystore "eventhandler"
create_consumer_system_keystore "datamanager"
create_consumer_system_keystore "gatekeeper"
create_consumer_system_keystore "gateway"
create_consumer_system_keystore "orchestrator"
create_consumer_system_keystore "serviceregistry"
create_consumer_system_keystore "plantdescriptionengine"
create_consumer_system_keystore "certificateauthority"

# Corrected create_sysop_keystore call for Consumer Cloud (uses LOCAL_CLOUD_NAME)
create_sysop_keystore \
  "cloud-root/crypto/root.p12" "arrowhead.eu" \
  "cloud-data-consumer/crypto/${LOCAL_CLOUD_NAME}.p12" "${LOCAL_CLOUD_NAME}" \
  "cloud-data-consumer/crypto/sysop.p12" "sysop.${LOCAL_CLOUD_NAME}"

# Corrected create_truststore call for Consumer Cloud (uses LOCAL_CLOUD_NAME.crt)
create_truststore \
  "cloud-data-consumer/crypto/truststore.p12" \
  "cloud-data-consumer/crypto/${LOCAL_CLOUD_NAME}.crt" "${LOCAL_CLOUD_NAME}" \
  "cloud-relay/crypto/conet-demo-relay.crt" "conet-demo-relay.ltu.arrowhead.eu"

# PRODUCER CLOUD (Keep as is, assuming you don't modify its functionality)

create_cloud_keystore \
  "cloud-root/crypto/root.p12" "arrowhead.eu" \
  "cloud-data-producer/crypto/conet-demo-producer.p12" "conet-demo-producer.ltu.arrowhead.eu"

create_producer_system_keystore() {
  SYSTEM_NAME=$1

  create_system_keystore \
    "cloud-root/crypto/root.p12" "arrowhead.eu" \
    "cloud-data-producer/crypto/conet-demo-producer.p12" "conet-demo-producer.ltu.arrowhead.eu" \
    "cloud-data-producer/crypto/${SYSTEM_NAME}.p12" "${SYSTEM_NAME}.conet-demo-producer.ltu.arrowhead.eu" \
    "dns:core.producer,ip:172.23.3.13,dns:localhost,ip:127.0.0.1"
}

create_producer_system_keystore "authorization"
create_producer_system_keystore "contractproxy"
create_producer_system_keystore "dataconsumer"
create_producer_system_keystore "eventhandler"
create_producer_system_keystore "datamanager"
create_producer_system_keystore "gatekeeper"
create_producer_system_keystore "gateway"
create_producer_system_keystore "orchestrator"
create_producer_system_keystore "serviceregistry"
create_producer_system_keystore "plantdescriptionengine"

create_sysop_keystore \
  "cloud-root/crypto/root.p12" "arrowhead.eu" \
  "cloud-data-producer/crypto/conet-demo-producer.p12" "conet-demo-producer.ltu.arrowhead.eu" \
  "cloud-data-producer/crypto/sysop.p12" "sysop.conet-demo-producer.ltu.arrowhead.eu"

create_truststore \
  "cloud-data-producer/crypto/truststore.p12" \
  "cloud-data-producer/crypto/conet-demo-producer.crt" "conet-demo-producer.ltu.arrowhead.eu" \
  "cloud-relay/crypto/conet-demo-relay.crt" "conet-demo-relay.ltu.arrowhead.eu"