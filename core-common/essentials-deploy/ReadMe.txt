REQUIREMENTS

* Maven installed | https://maven.apache.org/download.cgi
* GnuPG installed, keypair generated and publiy key is published | https://central.sonatype.org/publish/requirements/gpg/#installing-gnupg
* Having access to ai.aitia ossrh repository
* Respository added to your local maven settings.xml file with id of 'ossrh' | https://maven.apache.org/settings.html#servers

DEPLOY WITH BATCH SCRIPT

1) run deploy.bat script

2) Close and release in Nexus | https://s01.oss.sonatype.org/#stagingRepositories

DEPLOY MANUAL

1) Copy the arrowhead-core-common-essentials-java-spring.jar and its source jar into this folder and add the version to its names.
	E.g.: arrowhead-core-common-essentials-java-spring-4.4.0.0.jar
	
2) Duplicate the source jar and change "source" to "javadoc"

3) Set the same version in the pom file and change the file name accordingly

4) You have to have these files with the actual version:

	arrowhead-core-common-essentials-java-spring-4.4.0.0.jar
	arrowhead-core-common-essentials-java-spring-4.4.0.0.pom
	arrowhead-core-common-essentials-java-spring-4.4.0.0-sources.pom
	arrowhead-core-common-essentials-java-spring-4.4.0.0-javadoc.pom

5) run: mvn gpg:sign-and-deploy-file -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=arrowhead-core-common-essentials-java-spring-4.4.0.0.pom -Dfile=arrowhead-core-common-essentials-java-spring-4.4.0.0.jar

6) run: mvn gpg:sign-and-deploy-file -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=arrowhead-core-common-essentials-java-spring-4.4.0.0.pom -Dfile=arrowhead-core-common-essentials-java-spring-4.4.0.0-sources.jar -Dclassifier=sources

7) run: mvn gpg:sign-and-deploy-file -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=arrowhead-core-common-essentials-java-spring-4.4.0.0.pom -Dfile=arrowhead-core-common-essentials-java-spring-4.4.0.0-javadoc.jar -Dclassifier=javadoc

8) Close and release in Nexus | https://s01.oss.sonatype.org/#stagingRepositories