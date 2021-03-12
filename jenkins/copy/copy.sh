#!/bin/bash

 echo "****************************"
 echo "** Copying property files **"
 echo "****************************"

# Copy the preconfigured core system property files to their location
WORKSPACE=/var/jenkins_home/workspace/Arrowhead/Arrowhead_Core_Spring

# Service Registry
rm -f $WORKSPACE/serviceregistry/src/main/resources/application.properties
cp -f /var/core_system_config/service_registry.properties $WORKSPACE/serviceregistry/src/main/resources/application.properties

# Authorization
rm -f $WORKSPACE/authorization/src/main/resources/application.properties
cp -f /var/core_system_config/authorization.properties $WORKSPACE/authorization/src/main/resources/application.properties

# Orchestration
rm -f $WORKSPACE/orchestrator/src/main/resources/application.properties
cp -f /var/core_system_config/orchestrator.properties $WORKSPACE/orchestrator/src/main/resources/application.properties

# Gatekeeper
rm -f $WORKSPACE/gatekeeper/src/main/resources/application.properties
cp -f /var/core_system_config/gatekeeper.properties $WORKSPACE/gatekeeper/src/main/resources/application.properties

# Gateway
rm -f $WORKSPACE/gateway/src/main/resources/application.properties
cp -f /var/core_system_config/gateway.properties $WORKSPACE/gateway/src/main/resources/application.properties

# Event Handler
rm -f $WORKSPACE/eventhandler/src/main/resources/application.properties
cp -f /var/core_system_config/eventhandler.properties $WORKSPACE/eventhandler/src/main/resources/application.properties
