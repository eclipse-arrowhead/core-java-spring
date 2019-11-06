@ECHO OFF 
::   ============================
::   GENERATE ARROWHEAD CORE SYSTEM CERTIFICATES
::   ============================

SET SYSTEM_NAME=name
SET SYSTEM_PASSWORD=%1
SET SYSTEM_HOSTNAME=hostname
SET IP=%2
SET CLOUD_PASSWORD=%3
SET CLOUD_NAME=%4
SET CLOUD_ALIAS=%5

IF [%CLOUD_ALIAS%] == [] SET CLOUD_ALIAS=%CLOUD_NAME%

SET CORE_SYSTEM[0]=service_registry
SET CORE_SYSTEM[1]=authorization
SET CORE_SYSTEM[2]=orchestrator
SET CORE_SYSTEM[3]=event_handler
SET CORE_SYSTEM[4]=gatekeeper
SET CORE_SYSTEM[5]=gateway
SET CORE_SYSTEM[6]=choreographer
SET CORE_SYSTEM[7]=sysop

FOR /L %%a IN (0,1,7) DO ( 

	start "%%CORE_SYSTEM[%%a]%% certgeneration" /wait cmd /c generate_system_certificate.bat %%CORE_SYSTEM[%%a]%% %SYSTEM_PASSWORD% %SYSTEM_HOSTNAME% %IP% %CLOUD_PASSWORD% %CLOUD_NAME% %CLOUD_ALIAS%
)

EXIT /B 0
