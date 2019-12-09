FROM openjdk:11-jre-slim

COPY eventhandler/*.jar /eventhandler/arrowhead-eventhandler.jar

# Application properties file will be mounted as a volume from now on.
# COPY eventhandler/*.properties /eventhandler/application.properties

CMD cd /eventhandler && java -jar arrowhead-eventhandler.jar