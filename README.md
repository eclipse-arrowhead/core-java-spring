### Service Registry, Authorization, Orchestration Core Systems are available for now.

# Arrowhead Framework 4.1.2

[Arrowhead](http://www.arrowhead.eu/) (and its continuation, [Productive4.0](https://productive40.eu/)) is an ambitious holistic innovation project,
 meant to open the doors to the potentials of Digital Industry and to maintain a leadership position of the industries in Europe. All partners involved will work on creating the capability to efficiently design and integrate hardware and software of Internet of Things (IoT) devices. Linking the real with the digital world takes more than just adding software to the hardware.
 
### How to contribute?
Check out ```development``` branch. Create a new branch from ```development```. Don't forget do write documentation, unit and integration tests. When finished, create a pull request back into ```development```. When accepted, your contribution will be in the next release. :)
 
 
### Requirements

The project has the following dependencies:
* JRE/JDK 11 [Download from here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
* Maven 3.5+ [Download from here](http://maven.apache.org/download.cgi) | [Install guide](https://www.baeldung.com/install-maven-on-windows-linux-mac)
* MySQL server 5.7+ (other SQL databases can work with Hibernate ORM, but the `common module pom.xml` has to include the appropriate connector 
dependency to use them)

Verify that you have Java (```java -version```), Maven (```mvn -version```), MySQL installed properly!

In MySQL create a database called `arrowhead` and create a user `arrowhead` with password `arrowhead`. Example SQL statement, that does this for you: 
```
CREATE DATABASE arrowhead;
CREATE USER 'arrowhead'@'%';
GRANT ALL PRIVILEGES ON arrowhead.* To 'arrowhead'@'%' IDENTIFIED BY 'arrowhead';
```
Pull this code and enter the directory. 
```git pull https://github.com/arrowhead-f/core-java-spring.git```


```cd core-java-spring```

Execute ```mvn install``` command. Wait until the build succeeds. 
This command builds all available projects.
After succeeding enter:
- serviceregistry/target directory.
```cd serviceregistry/target``` <br />and execute: ```java -jar arrowhead-serviceregistry-4.1.2.jar```
- authorization/target directory. ```cd authorization/target``` <br />and execute: ```java -jar arrowhead-authorization-4.1.2.jar``` 
- orchestrator/target directory. ```orchestrator/target``` <br />and execute: ```java -jar arrowhead-orchestrator-4.1.2.jar```


Wait until servers start...

Service Registry will be available on ```localhost:8443``` <br />
Authorization will be available on ```localhost:8445``` <br />
Ochestrator will be available on ```localhost:8441``` <br />

Enjoy! ;)