1) Copy the arrowhead-core-common-essentials-java-spring.jar and its source jar into this folder and add the version to its names.
	E.g.: arrowhead-core-common-essentials-java-spring-4.4.0.0.jar
	
2) Duplicate the source jar and change "source" to "javadoc"

2) Set the same version in the pom file and change the file name accordingly

4) You have to have these files with the actual version:

	arrowhead-core-common-essentials-java-spring-4.4.0.0.jar
	arrowhead-core-common-essentials-java-spring-4.4.0.0.pom
	arrowhead-core-common-essentials-java-spring-4.4.0.0-sources.pom
	arrowhead-core-common-essentials-java-spring-4.4.0.0-javadoc.pom

5) Make sure you have ossrh server is added to your maven settings.xml file

6) Make sure you have gpg installed, key pair generated and your public key is uploded to the keyserver.
   see: https://central.sonatype.org/publish/requirements/gpg/#installing-gnupg

7) run: mvn gpg:sign-and-deploy-file -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=arrowhead-core-common-essentials-java-spring-4.4.0.0.pom -Dfile=arrowhead-core-common-essentials-java-spring-4.4.0.0.jar

8) run: mvn gpg:sign-and-deploy-file -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=arrowhead-core-common-essentials-java-spring-4.4.0.0.pom -Dfile=arrowhead-core-common-essentials-java-spring-4.4.0.0-sources.jar -Dclassifier=sources

9) run: mvn gpg:sign-and-deploy-file -Durl=https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=ossrh -DpomFile=arrowhead-core-common-essentials-java-spring-4.4.0.0.pom -Dfile=arrowhead-core-common-essentials-java-spring-4.4.0.0-javadoc.jar -Dclassifier=javadoc