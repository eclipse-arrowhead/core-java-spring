FROM openjdk:11-jre-slim

COPY orchestrator/*.jar /orchestrator/arrowhead-orchestrator.jar

# Application properties file will be mounted as a volume from now on.
# COPY orchestrator/*.properties /orchestrator/application.properties

CMD cd /orchestrator && java -jar arrowhead-orchestrator.jar