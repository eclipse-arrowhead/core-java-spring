@ECHO OFF

echo Shutting down Core Systems

SET time_to_sleep=5

FOR /F "tokens=1" %%p in ('"jps -v | find "choreographer""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "configuration""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "orchestrator""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "gatekeeper""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "eventhandler""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "datamanager""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "timemanager""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "gateway""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "authorization""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "certificate""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "plantdescriptionengine""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "onboarding""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "systemregistry""') DO taskkill /pid %%p > NUL 2>&1
FOR /F "tokens=1" %%p in ('"jps -v | find "deviceregistry""') DO taskkill /pid %%p > NUL 2>&1
timeout /t %time_to_sleep% /nobreak > NUL
FOR /F "tokens=1" %%p in ('"jps -v | find "serviceregistry""') DO taskkill /pid %%p > NUL 2>&1

timeout /t 2 /nobreak > NUL
SET STILL_THERE=""

FOR /F "tokens=1" %%p in ('"jps -v | find "serviceregistry""') DO set STILL_THERE=%%p

IF "%STILL_THERE%"=="""" (
  echo Core systems killed
) ELSE (
  FOR /F "tokens=1" %%p in ('"jps -v | find "choreographer""') DO taskkill /F /pid %%p 
  FOR /F "tokens=1" %%p in ('"jps -v | find "configuration""') DO taskkill /F /pid %%p 
  FOR /F "tokens=1" %%p in ('"jps -v | find "orchestrator""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "gatekeeper""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "eventhandler""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "datamanager""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "timemanager""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "gateway""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "authorization""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "certificate""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "plantdescriptionengine""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "onboarding""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "systemregistry""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "deviceregistry""') DO taskkill /F /pid %%p
  FOR /F "tokens=1" %%p in ('"jps -v | find "serviceregistry""') DO taskkill /F /pid %%p
  echo Core systems forcefully killed
)
