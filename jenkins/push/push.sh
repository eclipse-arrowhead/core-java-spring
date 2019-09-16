#!/bin/bash

echo "****************************"
echo "******** Logging in ********"
echo "****************************"

docker login $DOCKER_REPO_URL -u $DOCKER_USER -p $DOCKER_PASS

echo "****************************"
echo "****** Tagging images ******"
echo "****************************"

docker tag serviceregistry:$BUILD_TAG $NAMESPACE/$DOCKER_USER/serviceregistry:$BUILD_TAG
docker tag authorization:$BUILD_TAG $NAMESPACE/$DOCKER_USER/authorization:$BUILD_TAG
docker tag orchestrator:$BUILD_TAG $NAMESPACE/$DOCKER_USER/orchestrator:$BUILD_TAG
docker tag gatekeeper:$BUILD_TAG $NAMESPACE/$DOCKER_USER/gatekeeper:$BUILD_TAG
docker tag gateway:$BUILD_TAG $NAMESPACE/$DOCKER_USER/gateway:$BUILD_TAG
docker tag eventhandler:$BUILD_TAG $NAMESPACE/$DOCKER_USER/eventhandler:$BUILD_TAG


docker tag serviceregistry:$BUILD_TAG $NAMESPACE/$DOCKER_USER/serviceregistry:latest
docker tag authorization:$BUILD_TAG $NAMESPACE/$DOCKER_USER/authorization:latest
docker tag orchestrator:$BUILD_TAG $NAMESPACE/$DOCKER_USER/orchestrator:latest
docker tag gatekeeper:$BUILD_TAG $NAMESPACE/$DOCKER_USER/gatekeeper:latest
docker tag gateway:$BUILD_TAG $NAMESPACE/$DOCKER_USER/gateway:latest
docker tag eventhandler:$BUILD_TAG $NAMESPACE/$DOCKER_USER/eventhandler:latest

echo "****************************"
echo "** Pushing images to Repo **"
echo "****************************"

docker push $NAMESPACE/$DOCKER_USER/serviceregistry:$BUILD_TAG
docker push $NAMESPACE/$DOCKER_USER/authorization:$BUILD_TAG
docker push $NAMESPACE/$DOCKER_USER/orchestrator:$BUILD_TAG
docker push $NAMESPACE/$DOCKER_USER/gatekeeper:$BUILD_TAG
docker push $NAMESPACE/$DOCKER_USER/gateway:$BUILD_TAG
docker push $NAMESPACE/$DOCKER_USER/eventhandler:$BUILD_TAG

docker push $NAMESPACE/$DOCKER_USER/serviceregistry:latest
docker push $NAMESPACE/$DOCKER_USER/authorization:latest
docker push $NAMESPACE/$DOCKER_USER/orchestrator:latest
docker push $NAMESPACE/$DOCKER_USER/gatekeeper:latest
docker push $NAMESPACE/$DOCKER_USER/gateway:latest
docker push $NAMESPACE/$DOCKER_USER/eventhandler:latest