#!/bin/bash

# Copy the SR jar to the build location
rm -rf jenkins/build/serviceregistry
mkdir jenkins/build/serviceregistry
cp -f serviceregistry/target/*.jar jenkins/build/serviceregistry
# cp -f serviceregistry/target/*.properties jenkins/build/serviceregistry

# Copy the Auth jar to the build location
rm -rf jenkins/build/authorization
mkdir jenkins/build/authorization
cp -f authorization/target/*.jar jenkins/build/authorization
# cp -f authorization/target/*.properties jenkins/build/authorization

# Copy the ORCH jar to the build location
rm -rf jenkins/build/orchestrator
mkdir jenkins/build/orchestrator
cp -f orchestrator/target/*.jar jenkins/build/orchestrator
# cp -f orchestrator/target/*.properties jenkins/build/orchestrator

# Copy the Gatekeeper jar to the build location
rm -rf jenkins/build/gatekeeper
mkdir jenkins/build/gatekeeper
cp -f gatekeeper/target/*.jar jenkins/build/gatekeeper
# cp -f gatekeeper/target/*.properties jenkins/build/gatekeeper

# Copy the Gateway jar to the build location
rm -rf jenkins/build/gateway
mkdir jenkins/build/gateway
cp -f gateway/target/*.jar jenkins/build/gateway
# cp -f gateway/target/*.properties jenkins/build/gateway

# Copy the Event Handler jar to the build location
rm -rf jenkins/build/eventhandler
mkdir jenkins/build/eventhandler
cp -f eventhandler/target/*.jar jenkins/build/eventhandler
# cp -f eventhandler/target/*.properties jenkins/build/eventhandler

echo "****************************"
echo "** Building Docker Images **"
echo "****************************"

cd jenkins/build/ && docker-compose -f docker-compose-build.yml build --no-cache