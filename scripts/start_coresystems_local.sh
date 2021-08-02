#!/bin/bash

#Gatekeeper and Gateway are not started for local clouds

parent_path=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$parent_path"

time_to_sleep=10s

echo Starting Core Systems... Service initializations usually need around 20 seconds.

cd ../serviceregistry/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-serviceregistry-\*.jar | sort | tail -n1) &> sout_sr.log &
echo Service Registry started
sleep ${time_to_sleep} #wait for the Service Registry to fully finish loading up

cd ../../authorization/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-authorization-\*.jar | sort | tail -n1) &> sout_auth.log &
echo Authorization started

cd ../../orchestrator/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-orchestrator-\*.jar | sort | tail -n1) &> sout_orch.log &
echo Orchestrator started

