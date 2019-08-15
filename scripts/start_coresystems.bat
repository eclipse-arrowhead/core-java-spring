@ECHO OFF

SET parent_path=%~dp0
cd %parent_path%

SET time_to_sleep=10

echo Starting Core Systems... Service initializations usually need around 20 seconds.

cd ..\serviceregistry\target
START "serviceregistry" /B "cmd /c javaw -jar arrowhead-serviceregistry-4.1.3.jar > sout_sr.log 2>&1"
echo Service Registry started
timeout /t %time_to_sleep% /nobreak > NUL

cd ..\..\authorization\target
START "" /B "cmd /c javaw -jar arrowhead-authorization-4.1.3.jar > sout_auth.log 2>&1"
echo Authorization started

REM cd ..\..\gateway\target
REM START "" /B "cmd /c javaw arrowhead-gateway-4.1.3.jar > sout_gateway.log 2>&1"
REM echo Gateway started

REM cd ..\..\eventhandler\target
REM START "" /B "cmd /c javaw -jar arrowhead-eventhandler-4.1.3.jar > sout_eventhandler.log 2>&1"
REM echo Event Handler started

cd ..\..\gatekeeper\target
START "" /B "cmd /c javaw -jar arrowhead-gatekeeper-4.1.3.jar > sout_gk.log 2>&1"
echo Gatekeeper started

cd ..\..\orchestrator\target
START "" /B "cmd /c javaw -jar arrowhead-orchestrator-4.1.3.jar > sout_orch.log 2>&1"
echo Orchestrator started

cd %parent_path%

::Kill self
title=arrowheadSecureStarter
FOR /F "tokens=2" %%p in ('"tasklist /v /NH /FI "windowtitle eq arrowheadSecureStarter""') DO taskkill /pid %%p > NUL 2>&1
