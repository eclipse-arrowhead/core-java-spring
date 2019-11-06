@ECHO OFF 

SET AH_CONF_DIR=%cd%

SET AH_CLOUDS_DIR=%AH_CONF_DIR%
SET AH_SYSTEMS_DIR=%AH_CONF_DIR%

SET AH_PASS_CERT=123457
SET AH_CLOUD_NAME=testcloud
SET AH_OPERATOR=aitia
SET AH_COMPANY=arrowhead
SET AH_COUNTRY=eu

SET SYSTEM_NAME=%1
SET SYSTEM_PASSWORD=%2
SET SYSTEM_HOSTNAME=%3
SET SYSTEM_IP=%4
SET CLOUD_PASSWORD=123456
SET CLOUD_NAME=%6
SET CLOUD_ALIAS=%7

IF [%7] == [] SET CLOUD_ALIAS=%CLOUD_NAME%

SET AH_CLOUD_NAME=%CLOUD_NAME%

SET OWN_IP=%SYSTEM_IP%

::   ============================
:: system certificate generation starts here
::   ============================

CALL :ah_cert_signed_system %SYSTEM_NAME% %SYSTEM_PASSWORD% %SYSTEM_HOSTNAME% %SYSTEM_IP%

EXIT /B 0

::   ============================
:: system certificate generation ends here
::   ============================

::   ============================
:: ah_cert defined here
::   ============================
:ah_cert
	ECHO ============================
	ECHO ah_cert started ...
	ECHO parameters: %*
	ECHO ============================
	
	SET dst_path=%1
	SET dst_name=%2
	SET cn=%3
	SET password=%SYSTEM_PASSWORD%
	
	IF [%4] == [] SET password=%AH_PASS_CERT%

	SET file=%dst_path%\%dst_name%.p12
	
	IF EXIST %file% (
		ECHO %file% allready exist
		EXIT /B 102
	) ELSE (
		keytool -genkeypair -alias %dst_name% -keyalg RSA -keysize 2048 -dname CN=%dst_name%.%CLOUD_NAME%.aitia.arrowhead.eu -validity 7300 -keypass %SYSTEM_PASSWORD%  -keystore %file% -storepass %SYSTEM_PASSWORD% -storetype PKCS12 -ext BasicConstraints=ca:true,pathlen:3 -ext  SubjectAlternativeName=IP:127.0.0.1,DNS:localhost,IP:%SYSTEM_IP%
	)
	

EXIT /B 0
::   ============================
:: ah_cert definition over
::   ============================

::   ============================
:: ah_cert_import defined here
::   ============================
:ah_cert_import
REM	ECHO ============================
REM	ECHO ah_cert_import started ...
REM	ECHO parameters: %*
REM	ECHO ============================

	SET src_import_path=%1
	SET src_import_name=%2
	SET dst_import_path=%3
	SET dst_import_name=%4
	SET password_import=%SYSTEM_PASSWORD%

	IF [%5] == [] SET password_import=%AH_PASS_CERT%

	SET import_src_file=%src_import_path%/%src_import_name%.crt
	SET import_dst_file=%dst_import_path%/%dst_import_name%.p12

	keytool -import -trustcacerts -file %import_src_file% -alias arrowhead.eu -keystore %import_dst_file% -keypass %password_import% -storepass %password_import% -storetype PKCS12 -noprompt

EXIT /B 0
::   ============================
:: ah_cert_import definition is over
::   ============================

::   ============================
:: ah_cert_signed_system defined here
::   ============================
:ah_cert_signed_system
	ECHO ============================
	ECHO ah_cert_signed_system started ...
	ECHO parameters: %*
	ECHO ============================
	SET name=%1
	SET passwd=%SYSTEM_PASSWORD%
	SET host=%3
	SET ip=%4
	SET base_path=%5
	
	IF [%2] == [] SET passwd=%SYSTEM_PASSWORD%
	IF [%3] == [] SET host=host
	IF [%4] == [] SET ip=0.0.0.0
	IF [%5] == [] SET base_path=%AH_CONF_DIR%
	
	SET src_file=%AH_CONF_DIR%\%CLOUD_NAME%.p12
	SET system_dst_file=%AH_CONF_DIR%\%name%.p12
	
	IF EXIST %system_dst_file% (
		
		ECHO %system_dst_file% allready exist
		EXIT /B 101
		
	) ELSE (

		CALL :ah_cert %base_path% %name% %name%.%CLOUD_NAME%.%AH_OPERATOR%.arrowhead.eu %SYSTEM_PASSWORD%

		keytool -export -alias %CLOUD_ALIAS% -storepass %CLOUD_PASSWORD% -keystore %src_file% ^
		| keytool -import -trustcacerts -alias %CLOUD_ALIAS% -keystore %system_dst_file% -keypass %SYSTEM_PASSWORD% -storepass %SYSTEM_PASSWORD% -storetype PKCS12 -noprompt

		keytool -certreq -alias %name% -keypass %SYSTEM_PASSWORD% -keystore %system_dst_file% -storepass %SYSTEM_PASSWORD% ^
		| keytool -gencert -rfc -alias %CLOUD_ALIAS% -keypass %CLOUD_PASSWORD% -keystore %src_file% -storepass %CLOUD_PASSWORD% -validity 3650 -ext SubjectAlternativeName=IP:127.0.0.1,DNS:localhost,DNS:%host%,IP:%ip% ^
		| keytool -importcert -alias %name% -keypass %SYSTEM_PASSWORD% -keystore %system_dst_file% -storepass %SYSTEM_PASSWORD% -noprompt

		CALL :ah_cert_import %AH_CONF_DIR% master %base_path% %name% %SYSTEM_PASSWORD%
	)
EXIT /B 0
::   ============================
:: ah_cert_signed_system definition is over
