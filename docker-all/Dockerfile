FROM openjdk:11-jre-slim

WORKDIR /opt/arrowhead-core

COPY target/*.jar ./
COPY run.sh run.sh

CMD ["./run.sh"]