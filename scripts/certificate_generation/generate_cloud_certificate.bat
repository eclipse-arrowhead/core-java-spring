@ECHO OFF 

SET AH_CONF_DIR=%cd%

SET AH_CLOUDS_DIR=%AH_CONF_DIR%
SET AH_SYSTEMS_DIR=%AH_CONF_DIR%

SET AH_PASS_CERT=123456
SET AH_CLOUD_NAME=testcloud
SET AH_OPERATOR=aitia
SET AH_COMPANY=arrowhead
SET AH_COUNTRY=eu

SET SYSTEM_NAME=%1
SET SYSTEM_PASSWORD=%2
SET SYSTEM_HOSTNAME=%3
SET SYSTEM_IP=%4
SET CLOUD_NAME=%5
SET CLOUD_ALIAS=%6

IF [%6] == [] SET CLOUD_ALIAS=%CLOUD_NAME%

SET AH_CLOUD_NAME=%CLOUD_NAME%

SET OWN_IP=%SYSTEM_IP%

::   ============================
:: system certificate generation starts here
::   ============================

CALL :ah_cert_signed_cloud %SYSTEM_NAME% %SYSTEM_PASSWORD% %SYSTEM_HOSTNAME% %SYSTEM_IP%

EXIT /B 0

::   ============================
:: system certificate generation ends here
::   ============================

::   ============================
:: ah_cert defined here
::   ============================
:ah_cert_cloud
REM	ECHO ============================
REM	ECHO ah_cert_cloud started ...
REM	ECHO parameters: %*
REM	ECHO ============================
	
	SET dst_path=%1
	SET dst_name=%2
	SET cn=%3
	SET password=%4
	
	IF [%4] == [] SET password=%AH_PASS_CERT%

	SET file=%dst_path%\%dst_name%.p12
	
	IF EXIST %file% (
		ECHO %file% allready exist
		EXIT /B 102
	) ELSE (
		keytool -genkeypair -alias %dst_name% -keyalg RSA -keysize 2048 -dname CN=%dst_name%.aitia.arrowhead.eu -validity 7300 -keypass 123456  -keystore %file% -storepass 123456 -storetype PKCS12 -ext BasicConstraints=ca:true,pathlen:2
	)
	

EXIT /B 0
::   ============================
:: ah_cert_cloud definition over
::   ============================

::   ============================
:: ah_cert_signed_system defined here
::   ============================
:ah_cert_signed_cloud
REM	ECHO ============================
REM	ECHO ah_cert_signed_cloud started ...
REM	ECHO parameters: %*
REM	ECHO ============================
	SET name=%1
	SET passwd=%2
	SET host=%3
	SET ip=%4
	SET base_path=%5
	
	IF [%2] == [] SET passwd=%AH_PASS_CERT%
	IF [%3] == [] SET host=host
	IF [%4] == [] SET ip=0.0.0.0
	IF [%5] == [] SET base_path=%AH_CONF_DIR%
	
	SET src_file=%AH_CONF_DIR%\%CLOUD_NAME%.p12
	SET system_dst_file=%AH_CONF_DIR%\%name%.p12
	
	IF EXIST %system_dst_file% (
		
		ECHO %system_dst_file% allready exist
		EXIT /B 101
		
	) ELSE (

		CALL :ah_cert_cloud %base_path% %name% %name%.%AH_OPERATOR%.arrowhead.eu %passwd%
		
		ECHO src_file %src_file%

		keytool -export -alias %CLOUD_ALIAS% -storepass %AH_PASS_CERT% -keystore %src_file% ^
		| keytool -import -trustcacerts -alias %CLOUD_ALIAS% -keystore %system_dst_file% -keypass %passwd% -storepass %passwd% -storetype PKCS12 -noprompt

		keytool -certreq -alias %name% -keypass %passwd% -keystore %system_dst_file% -storepass %passwd% ^
		| keytool -gencert -rfc -alias %CLOUD_ALIAS% -keypass %AH_PASS_CERT% -keystore %src_file% -storepass %AH_PASS_CERT% -validity 3650 -ext BasicConstraints=ca:true,pathlen:2 ^
		| keytool -importcert -alias %name% -keypass %passwd% -keystore %system_dst_file% -storepass %passwd% -noprompt

	)
EXIT /B 0
::   ============================
:: ah_cert_signed_cloud definition is over
