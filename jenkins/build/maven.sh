 #!/bin/bash

 echo "****************************"
 echo "******* Building JAR *******"
 echo "****************************"

WORKSPACE=/home/jenkins/jenkins-data/jenkins_home/workspace/pipeline-docker-maven

docker run -it -v /home/adminka/core-java-spring:/core-java-spring -v /root/.m2/:/root/.m2/ -w /core-java-spring maven:3.6.2-jdk-11-slim "$@"
