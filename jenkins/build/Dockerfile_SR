FROM openjdk:11-jre-slim

COPY serviceregistry/*.jar /serviceregistry/arrowhead-serviceregistry.jar

# Application properties file will be mounted as a volume from now on.
# COPY serviceregistry/*.properties /serviceregistry/application.properties

CMD cd /serviceregistry && java -jar arrowhead-serviceregistry.jar