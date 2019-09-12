#!/bin/bash

# Copy the SR jar to the build location
rm -rf jenkins/build/serviceregistry
mkdir jenkins/build/serviceregistry
cp -f serviceregistry/target/*.jar jenkins/build/serviceregistry
cp -f serviceregistry/target/*.properties jenkins/build/serviceregistry

echo "****************************"
echo "** Building Docker Images **"
echo "****************************"

cd jenkins/build/ && docker-compose -f docker-compose-build.yml build --no-cache