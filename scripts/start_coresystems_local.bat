@ECHO OFF

REM Gatekeeper and Gateway are not started for local clouds

SET parent_path=%~dp0
cd %parent_path%

SET time_to_sleep=10

echo Starting Core Systems... Service initializations usually need around 20 seconds.

cd ..\serviceregistry\target
START "serviceregistry" /B "cmd /c javaw -jar arrowhead-serviceregistry-4.2.0.jar > sout_sr.log 2>&1"
echo Service Registry started
timeout /t %time_to_sleep% /nobreak > NUL

cd ..\..\authorization\target
START "" /B "cmd /c javaw -jar arrowhead-authorization-4.2.0.jar > sout_auth.log 2>&1"
echo Authorization started

REM cd ..\..\eventhandler\target
REM START "" /B "cmd /c javaw -jar arrowhead-eventhandler-4.2.0.jar > sout_eventhandler.log 2>&1"
REM echo Event Handler started

REM cd ..\..\datamanager\target
REM START "" /B "cmd /c javaw -jar arrowhead-datamanager-4.1.3.jar > sout_datamanager.log 2>&1"
REM echo DataManager started

cd ..\..\orchestrator\target
START "" /B "cmd /c javaw -jar arrowhead-orchestrator-4.2.0.jar > sout_orch.log 2>&1"
echo Orchestrator started

cd ..\..\certificate-authority\target
START "" /B "cmd /c javaw -jar arrowhead-certificate-authority-4.2.0.jar > sout_ca.log 2>&1"
echo Certificate Authority started

cd %parent_path%

::Kill self
title=arrowheadSecureStarter
FOR /F "tokens=2" %%p in ('"tasklist /v /NH /FI "windowtitle eq arrowheadSecureStarter""') DO taskkill /pid %%p > NUL 2>&1
