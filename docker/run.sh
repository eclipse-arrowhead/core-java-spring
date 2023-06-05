#!/bin/bash

cd /opt/arrowhead
if [ -f "/opt/arrowhead/application.jar" ]; then
    echo "Executable exists."
else
    mv /opt/arrowhead-temp/$SYSTEM_NAME/target/arrowhead-$SYSTEM_NAME-$AH_VERSION.jar /opt/arrowhead/application.jar
    rm -rf /opt/arrowhead-temp
fi
java -jar application.jar

