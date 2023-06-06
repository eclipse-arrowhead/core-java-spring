#!/bin/bash

cd /opt/arrowhead
if [ -z "$SYSTEM_NAME" ]; then
    echo "The environment variable is not set."
else
    echo "Arrowhead Core System: $SYSTEM_NAME"
	if [ -f "/opt/arrowhead/application.jar" ]; then
		echo "Container has been already initialized."
	else
		mv /opt/arrowhead-temp/$SYSTEM_NAME/target/arrowhead-$SYSTEM_NAME-$AH_VERSION.jar /opt/arrowhead/application.jar
		rm -rf /opt/arrowhead-temp
		echo "Container has been initialized."
	fi
	java -jar application.jar
fi


