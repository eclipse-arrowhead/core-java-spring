# Arrowhead Framework 4.1.3

[Arrowhead](http://www.arrowhead.eu/) (and its continuation, [Productive4.0](https://productive40.eu/)) is an ambitious holistic innovation project,
 meant to open the doors to the potentials of Digital Industry and to maintain a leadership position of the industries in Europe. All partners involved will work on creating the capability to efficiently design and integrate hardware and software of Internet of Things (IoT) devices. Linking the real with the digital world takes more than just adding software to the hardware.
 
 
## Table of Contents
[Quick Start Guide](#quickstart)<br />
[Migration Guide 4.1.2 -> 4.1.3](#migration)<br />
[How to Contribute](#howtocontribute)<br />
[Documentation](#documentation)<br /> 
 
 
<a name="quickstart" />

## Quick Start Guide

### Docker

Placeholder

### Compile source code and manually install MySQL and Maven.
#### Requirements

The project has the following dependencies:
* JRE/JDK 11 [Download from here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
* Maven 3.5+ [Download from here](http://maven.apache.org/download.cgi) | [Install guide](https://www.baeldung.com/install-maven-on-windows-linux-mac)
* MySQL server 5.7+ (other SQL databases can work with Hibernate ORM, but the `common module pom.xml` has to include the appropriate connector 
dependency to use them)

Verify that you have Java (```java -version```), Maven (```mvn -version```), MySQL installed properly!

Pull this code and enter the directory. 
```git clone https://github.com/arrowhead-f/core-java-spring.git```

Run the MySQL script which is in the ```scripts``` folder. If you won't run this script first, the project won't build. 

```cd core-java-spring```

Execute ```mvn install``` command. Wait until the build succeeds. 
This command builds all available projects. <br />

After succeeding enter the scripts folder and execute ```start_core_systems.sh``` or ```start_core_systems.bat``` depending on your operating system.
- serviceregistry/target directory.
```cd serviceregistry/target``` <br />and execute: ```java -jar arrowhead-serviceregistry-4.1.3.jar```
- authorization/target directory. ```cd authorization/target``` <br />and execute: ```java -jar arrowhead-authorization-4.1.3.jar``` 
- orchestrator/target directory. ```orchestrator/target``` <br />and execute: ```java -jar arrowhead-orchestrator-4.1.3.jar```


Wait until servers start...

Service Registry will be available on ```localhost:8443``` <br />
Authorization will be available on ```localhost:8445``` <br />
Ochestrator will be available on ```localhost:8441``` <br />
Event Handler will be available on ```localhost:8455``` <br />
Gatekeeper will be available on ```localhost:8449``` <br />
Gateway will be available on ```localhost:8453``` <br />

Swagger with API documentation is available in the root route.

Enjoy! ;)


<a name="migration" />

## Migration Guide 4.1.2 -> 4.1.3

4.1.3 is NOT backwards compatible with 4.1.2! The database and the endpoints were redesigned, it is more logical, easier to use.

You can migrate your existing database manually. See the [Quick Start Guide](#quickstart), how to deploy the Core Systems.

Major endpoint changes:<br />

### Service Registry:
 * __/register__ - data structure changed
 * ~~/remove~~ - removed, use __/unregister__ instead 
 
### Authorization
 * __/mgmt/intracloud__ - data structure changed
 * __/mgmt/intercloud__ - data structure changed
 
### Orchestration
 Store based orchestration is available for now.
 * __/mgmt/store__ - data structure changed
 

<a name="howtocontribute" />

## How to Contribute

### Open Development

All work on Arrowhead repositories happens directly on GitHub. Both core team members and external contributors send pull requests which go through the same review process.

### Branch Organization

The latest version of the core systems are available in the ```master``` branch. The code for the next release is merged in the ```development``` branch. If you would like to contribute, please check out the ```development``` branch. Create a new branch from ```development```. Don't forget do write documentation, unit and integration tests. When finished, create a pull request back into ```development```. If accepted, your contribution will be in the next release. :)

### Bugs

#### Where To Find Known Issues
We are using [GitHub Issues](https://github.com/arrowhead-f/core-java-spring/issues) for our public bugs. We keep a close eye on this and try to make it clear when we have an internal fix in progress. Before filing a new task, try to make sure your problem doesn’t already exist.

#### Reporting New Issues
The best way to get your bug fixed is to provide a reduced test case.

#### How to Get in Touch
Join our developer team on Slack. Write an email to: [szvetlin@aitia.ai](mailto:szvetlin@aitia.ai) for an invite. 

<a name="documentation" />

## Documentation 
 
 Placeholder