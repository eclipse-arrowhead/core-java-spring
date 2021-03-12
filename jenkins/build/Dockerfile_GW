FROM openjdk:11-jre-slim

COPY gateway/*.jar /gateway/arrowhead-gateway.jar

# Application properties file will be mounted as a volume from now on.
# COPY gateway/*.properties /gateway/application.properties

CMD cd /gateway && java -jar arrowhead-gateway.jar