#!/bin/bash

exit_script() {
	echo -e "\e[1;31mExiting ...\e[0m"
	trap - INT TERM
	kill -$$
}
trap exit_script INT TERM

PIDS=()
for JAR in *.jar; do
	NAME=$(echo "${JAR}" | sed 's/arrowhead-\([^-]*\)-.*/\1/')
	PROPERTIES="${NAME}/application.properties"
	if [[ -f "${PROPERTIES}" ]]; then
		echo -e "\e[1;32mjava ${JVM_FLAGS} -jar ${JAR} &\e[0m"
		cd "${NAME}"
		java ${JVM_FLAGS} -jar "../${JAR}" &
		PIDS+=" $!"
		cd ..
	else
		echo -e "\e[1;31m${PROPERTIES} not provided; skipping ${JAR}"
	fi
done

for PID in ${PIDS[*]}; do
	wait "${PID}"
done
