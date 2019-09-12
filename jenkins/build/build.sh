#!/bin/bash

#Copy the SR jar to the build location
cp -f serviceregistry/target/*.jar jenkins/build/

echo "****************************"
echo "*** Building Docker Image **"
echo "****************************"

cd jenkins/build/ && docker-compose -f docker-compose-build.yml