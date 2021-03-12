FROM openjdk:11-jre-slim

COPY gatekeeper/*.jar /gatekeeper/arrowhead-gatekeeper.jar

# Application properties file will be mounted as a volume from now on.
# COPY gatekeeper/*.properties /gatekeeper/application.properties

CMD cd /gatekeeper && java -jar arrowhead-gatekeeper.jar