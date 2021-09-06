#!/bin/bash

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

cd ../../gateway/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-gateway-\*.jar | sort | tail -n1) &> sout_gateway.log &
echo Gateway started

cd ../../eventhandler/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-eventhandler-\*.jar | sort | tail -n1) &> sout_eventhandler.log &
echo Event Handler started

cd ../../datamanager/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-datamanager-\*.jar | sort | tail -n1) &> sout_datamanager.log &
echo DataManager started

cd ../../timemanager/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-timemanager-\*.jar | sort | tail -n1) &> sout_timemanager.log &
echo TimeManager started

cd ../../gatekeeper/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-gatekeeper-\*.jar | sort | tail -n1) &> sout_gk.log &
echo Gatekeeper started

cd ../../orchestrator/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-orchestrator-\*.jar | sort | tail -n1) &> sout_orch.log &
echo Orchestrator started

cd ../../choreographer/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-choreographer-\*.jar | sort | tail -n1) &> sout_choreographer.log &
echo Choreographer started

cd ../../configuration/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-configuration-\*.jar | sort | tail -n1) &> sout_configuration.log &
echo Configuration started

cd ../../certificate-authority/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-certificate-authority-\*.jar | sort | tail -n1) &> sout_ca.log &
echo Certificate Authority started

cd ../../deviceregistry/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-deviceregistry-\*.jar | sort | tail -n1) &> sout_deviceregistry.log &
echo Device Registry started

cd ../../systemregistry/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-systemregistry-\*.jar | sort | tail -n1) &> sout_systemregistry.log &
echo System Registry started

cd ../../onboarding/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-onboarding-\*.jar | sort | tail -n1) &> sout_onboarding.log &
echo Onboarding Controller started

cd ../../plantdescriptionengine/target
nohup java -jar $(find . -maxdepth 1 -name arrowhead-plantdescriptionengine-\*.jar | sort | tail -n1) &> sout_plantdescriptionengine.log &
echo Plant Description Engine started