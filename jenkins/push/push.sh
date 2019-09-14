#!/bin/bash

echo "****************************"
echo "******** Logging in ********"
echo "****************************"

docker login $DOCKER_REPO_URL -u $1 -p $P2

echo "****************************"
echo "****** Tagging images ******"
echo "****************************"

docker tag serviceregistry:$BUILD_TAG $NAMESPACE/serviceregistry:$BUILD_TAG
docker tag authorization:$BUILD_TAG $NAMESPACE/authorization:$BUILD_TAG
docker tag orchestrator:$BUILD_TAG $NAMESPACE/orchestrator:$BUILD_TAG
docker tag gatekeeper:$BUILD_TAG $NAMESPACE/gatekeeper:$BUILD_TAG
docker tag gateway:$BUILD_TAG $NAMESPACE/gateway:$BUILD_TAG
docker tag eventhandler:$BUILD_TAG $NAMESPACE/eventhandler:$BUILD_TAG

echo "****************************"
echo "** Pushing images to Repo **"
echo "****************************"

docker push $NAMESPACE/serviceregistry:$BUILD_TAG
docker push $NAMESPACE/authorization:$BUILD_TAG
docker push $NAMESPACE/orchestrator:$BUILD_TAG
docker push $NAMESPACE/gatekeeper:$BUILD_TAG
docker push $NAMESPACE/gateway:$BUILD_TAG
docker push $NAMESPACE/eventhandler:$BUILD_TAG