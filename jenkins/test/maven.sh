 #!/bin/bash

 echo "****************************"
 echo "******* Building JAR *******"
 echo "****************************"

WORKSPACE=/var/lib/docker/volumes/jenkins_home/_data/workspace/Arrowhead/

docker run --rm -v $WORKSPACE/Arrowhead_Core_Spring:/core-java-spring -v /root/.m2/:/root/.m2/ -w /core-java-spring --network="compose_default" maven:3.6.2-jdk-11-slim "$@"
