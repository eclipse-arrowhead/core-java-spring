#*******************************************************************************
# Copyright (c) 2020 Bosch.IO GmbH[ and others]

# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.

# SPDX-License-Identifier: EPL-2.0
#*******************************************************************************

# Use jre 11 from openjdk
FROM openjdk:11-jre-slim

# Execute application under a non root user for improved security
RUN adduser --system --group spring
USER spring:spring

# Include the fat-jar and run it in the docker container
ARG JAR_FILE=target/arrowhead-hawkbit-configuration-manager-4.4.0.jar
COPY --chown=spring:spring ${JAR_FILE} /home/spring/app.jar
ENTRYPOINT ["java","-jar","/home/spring/app.jar"]