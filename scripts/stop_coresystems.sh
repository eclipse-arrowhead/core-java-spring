#!/bin/bash
#More sleep time between these commands might be needed on slower devices like a Raspberry Pi (because of the database accesses)
echo Shutting down Core Systems
pkill -f choreographer
pkill -f configuration
pkill -f orchestrator
pkill -f gatekeeper
pkill -f authorization
pkill -f eventhandler
pkill -f datamanager
pkill -f timemanager
pkill -f gateway
pkill -f certificate-authority
pkill -f plantdescriptionengine
pkill -f onboarding
pkill -f systemregistry
pkill -f deviceregistry
sleep 5s
pkill -f serviceregistry
sleep 2s

if pgrep -f serviceregistry
then
  kill -KILL $(ps aux | grep 'choreographer' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'configuration' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'orchestrator' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'gatekeeper' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'authorization' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'eventhandler' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'datamanager' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'timemanager' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'gateway' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'certificate-authority' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'plantdescriptionengine' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'onboarding' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'systemregistry' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'deviceregistry' | awk '{print $2}')
  kill -KILL $(ps aux | grep 'serviceregistry' | awk '{print $2}')
  echo Core systems forcefully killed
else
  echo Core systems killed
fi
