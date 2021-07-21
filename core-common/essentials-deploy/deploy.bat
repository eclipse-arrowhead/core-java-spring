rem TODO: create linux version too
@echo OFF
cls

:start
set /p VERSION=Type the version:
if [%VERSION%]==[] goto start

rmdir /Q /S work
md work
cd .\work

set ABORTED="false"
if not exist ..\pom.xml goto nopom
if not exist ..\..\target\arrowhead-core-common-essentials-java-spring.jar goto nojar
if not exist ..\..\target\arrowhead-core-common-essentials-java-spring-sources.jar goto nosource

>nul copy ..\pom.xml .\
call mvn versions:set -DnewVersion=%VERSION% || goto mvnerror
>nul copy .\pom.xml .\arrowhead-core-common-essentials-java-spring-%VERSION%.pom
>nul copy ..\..\target\arrowhead-core-common-essentials-java-spring.jar .\arrowhead-core-common-essentials-java-spring-%VERSION%.jar
>nul copy ..\..\target\arrowhead-core-common-essentials-java-spring-sources.jar .\arrowhead-core-common-essentials-java-spring-%VERSION%-sources.jar
>nul copy .\arrowhead-core-common-essentials-java-spring-%VERSION%-sources.jar .\arrowhead-core-common-essentials-java-spring-%VERSION%-javadoc.jar

call mvn gpg:sign-and-deploy-file -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=arrowhead-core-common-essentials-java-spring-%VERSION%.pom -Dfile=arrowhead-core-common-essentials-java-spring-%VERSION%.jar || goto mvnerror

call mvn gpg:sign-and-deploy-file -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh  -DpomFile=arrowhead-core-common-essentials-java-spring-%VERSION%.pom -Dfile=arrowhead-core-common-essentials-java-spring-%VERSION%-sources.jar -Dclassifier=sources || goto mvnerror

call mvn gpg:sign-and-deploy-file -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh  -DpomFile=arrowhead-core-common-essentials-java-spring-%VERSION%.pom -Dfile=arrowhead-core-common-essentials-java-spring-%VERSION%-javadoc.jar -Dclassifier=javadoc || goto mvnerror

cd..
goto finished

:nopom
echo ERROR: pom.xml not found
set ABORTED="true"
goto finished

:nojar
echo ERROR: arrowhead-core-common-essentials-java-spring.jar not found
set ABORTED="true"
goto finished

:nosource
echo ERROR: arrowhead-core-common-essentials-java-spring-sources.jar not found
set ABORTED="true"
goto finished

:mvnerror
echo ERROR: maven failure
set ABORTED="true"
goto finished

:finished
echo -------------------------------------------------------------------------------
if %ABORTED%=="false" ( echo [92mDEPLOYMENT SUCCESS[0m ) else ( echo [91mDEPLOYMENT FAILURE[0m )
echo -------------------------------------------------------------------------------