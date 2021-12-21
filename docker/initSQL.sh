#!/bin/bash

rm -rf sql

mkdir sql
cd sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/create_empty_arrowhead_db.sql
sed -i 's/source /& docker-entrypoint-initdb.d\/privileges\//g' create_empty_arrowhead_db.sql

mkdir privileges
cd privileges
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/authorization_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/certificate_authority_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/choreographer_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/create_arrowhead_tables.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/device_registry_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/event_handler_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/gatekeeper_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/gateway_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/onboarding_controller_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/orchestrator_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/qos_monitor_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/service_registry_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/system_registry_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/translator_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/datamanager_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/configuration_privileges.sql
wget https://raw.githubusercontent.com/eclipse-arrowhead/core-java-spring/master/scripts/plant_description_engine_privileges.sql
