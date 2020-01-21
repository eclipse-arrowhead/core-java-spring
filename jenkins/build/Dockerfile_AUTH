FROM openjdk:11-jre-slim

COPY authorization/*.jar /authorization/arrowhead-authorization.jar

# Application properties file will be mounted as a volume from now on.
# COPY authorization/*.properties /authorization/application.properties

CMD cd /authorization && java -jar arrowhead-authorization.jar