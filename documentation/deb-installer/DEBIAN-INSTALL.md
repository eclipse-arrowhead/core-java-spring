## Installing Arrowhead on a Debian based Linux distribution

Currently, this guide is known to work on Ubuntu Server 18.04, but it will likely work on other
version and Debian based Linux distribution also. The following is a quick guide on the essentials.

### 1. Install Linux

Do a normal installation of Linux, and remember to update afterwards:

`sudo apt update && sudo apt dist-upgrade`

### 2. Install MySQL

Pick one of the options below.

#### 2a. MySQL 5.x (Ubuntu)

Install:

`sudo apt install mysql-server`

Check if running:

`sudo netstat -tap | grep mysql`

#### 2b. MySQL 8.x (Oracle)

First, get the latest repository package from <https://dev.mysql.com/downloads/repo/apt/>, eg.:

```bash
wget https://dev.mysql.com/get/mysql-apt-config_0.8.10-1_all.deb
sudo dpkg -i mysql-apt-config_0.8.10-1_all.deb
sudo apt update
```

To install the MySQL server, run:

```bash
sudo apt install mysql-server
```

**NOTE:** The Arrowhead installers use MySQL root user to create database tables, users and privileges for the Arrowhead core systems, so if the database
and the core system is not on the same machine you have to make sure that MySQL root user has remote access. 

Login to the machine where the database is located and start the mysql command line interface:

`mysql -uroot -p`

After typing the password use the following SQL commands to enable remote access:

```
CREATE USER 'root'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON * . * TO 'root'@'%' IDENTIFIED BY 'password' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

Exposing root user to every host is a HUGE SECURITY RISK so we recommend revoking the remote root access after the installation is done:

```
DROP USER 'root'@'%';
```

### 3. Install Java

Ubuntu users (and others?):

`sudo apt install openjdk-11-jre-headless`

**NOTE:** Install JDK version instead of JRE version, if you plan to build the latest Debian Packages from source, using Maven. You just need to 
change the "-jre-" part to "-jdk-" in the package name. JDK needs more disk space. JRE versions can only run packaged Java applications, but can 
not build them from source code.

Check Java version:

`java -version`

### 4. Download/install Arrowhead 

Pick one of the options below.

#### 4a. Download Arrowhead Debian Packages 

Check the GitHub site <https://github.com/arrowhead-f/core-java-spring-installers/tree/master/packages> for the latest packages and download
them: 

```bash
wget -c  https://github.com/arrowhead-f/core-java-spring-installers/raw/master/packages/arrowhead-core-common_4.1.3.deb
wget -c  https://github.com/arrowhead-f/core-java-spring-installers/raw/master/packages/arrowhead-authorization_4.1.3.deb
wget -c  https://github.com/arrowhead-f/core-java-spring-installers/raw/master/packages/arrowhead-choreographer_4.1.3.deb
wget -c  https://github.com/arrowhead-f/core-java-spring-installers/raw/master/packages/arrowhead-eventhandler_4.1.3.deb
wget -c  https://github.com/arrowhead-f/core-java-spring-installers/raw/master/packages/arrowhead-gatekeeper_4.1.3.deb
wget -c  https://github.com/arrowhead-f/core-java-spring-installers/raw/master/packages/arrowhead-gateway_4.1.3.deb
wget -c  https://github.com/arrowhead-f/core-java-spring-installers/raw/master/packages/arrowhead-orchestrator_4.1.3.deb
wget -c  https://github.com/arrowhead-f/core-java-spring-installers/raw/master/packages/arrowhead-serviceregistry_4.1.3.deb
```

**NOTE:** The arrowhead-core-common package is a requirement for all other packages.

#### 4b. Build Arrowhead Debian Packages

**NOTE:** To compile Arrowhead yourself, you should have both the JDK and Maven installed. 

To build the Debian packages yourself, start by cloning the repository:

`git clone https://github.com/arrowhead-f/core-java-spring.git -b development`

Build them with:

`mvn package -DskipTests`

Copy all the packages to your Arrowhead server (you may have to start SSH server on it first with `sudo systemctl start ssh`:

`scp target/arrowhead-*.deb X.X.X.X:~/`

### 5. Install Arrowhead Core Debian Packages

Go to the folder where you copied the packages and then:

`sudo apt install ./arrowhead-*.deb`

The installation process will show prompts asking for input parameters. Certificate passwords need to be at least 6 
character long!

After the process is done, please see the log files in the /var/log/arrowhead folder. If any of the installed systems failed to start
(because the Service Registry was unreachable at the time when they try to connect to it), please reboot the failed core system manually by using
`sudo systemctl start arrowhead-<core system name>.service` You can find the service files in the /etc/systemd/system folder.

The created services will restart on reboot.

## Hints

### Arrowhead management script

Arrowhead comes with a management script `arrowhead` used to simplify things.
See `/etc/arrowhead/arrowhead.cfg` for configuration options for the script.

#### Standalone usage

To use this script without installing arrowhead on your system follow these steps.

##### 1. Download neccessary files.

Create a new, empty directory `mkdir $HOME/arrowhead` and download these files to it.

1. [ahconf.sh](core-commons/src/deb/ahconf.sh)
2. [arrowhead](core-commons/src/deb/arrowhead)
3. [arrowhead.cfg](core-commons/src/deb/arrowhead.cfg)

##### 2. Permissions

Make sure that `ahconf.sh` and `arrowhead` are executable

```bash
chmod +x ahconf.sh arrowhead
```

##### 3. Set neccessary flags

When running arrowhead standalone you need to set two flags for everything to work.

```bash
arrowhead ... -r=/path/to/created/directory -a=/path/to/ahconf.sh
```

Where directory is the directory you created in step one.

#### Example uses

##### Generate new master certificate (and, in turn, system certificates)

```bash
sudo arrowhead certs -m
```

##### Generate new certificates for all systems

```bash
sudo arrowhead certs -u
```

##### Generate new certificates for specific systems

```bash
sudo arrowhead certs -s=SYSTEM...
```

##### Generate certificates for new systems

Add the new system to the systems list in `arrowhead.cfg` then run:

```bash
sudo arrowhead certs -n -u
```

### Add a new application system

#### Certificate generation

##### Method a)

See [this](#generate-certificates-for-new-systems)

##### Method b)

You can use the script `ah_gen_system_cert` to generate certificate (and trust store) to a system.

```bash
sudo ah_gen_system_cert SYSTEM_NAME PASSWORD SYSTEM_HOST SYSTEM_IP
```

SYSTEM_NAME and PASSWORD are mandatory parameters. The other two are the DNS name and IP address of the computer that running 
the system. If SYSTEM_HOST and SYSTEM_IP are not specified, the command assumes the system is running on the local machine.
Examples:

```bash
sudo ah_gen_system_cert client1 123456 abc.com 10.0.0.22
```

```bash
sudo ah_gen_system_cert localsystem1 123456
```

A new directory is generated in your current directory which is named SYSTEM_NAME. Generated certificates will appear in this directory.

### Add a new relay

You can use the script `ah_gen_relay_cert` to generate certificate (and trust store) to a ActiveMQ relay.

```bash
sudo ah_gen_relay_cert RELAY_NAME PASSWORD RELAY_MASTER_CERT RELAY_MASTER_PASSWORD RELAY_HOST RELAY_IP
```

RELAY_NAME, PASSWORD, RELAY_MASTER_CERT (path to the relay master certificate .p12 file) and RELAY_MASTER_PASSWORD are mandatory parameters. 
The other two are the DNS name and IP address of the computer that running the relay. If RELAY_HOST and RELAY_IP are not specified, the 
command assumes the relay is running on the local machine.
Examples:

```bash
sudo ah_gen_cert relay1 123456 ./relay-master.p12 654321 abc.com 10.0.0.22
```

```bash
sudo ah_gen_cert relay1 123456 ./relay-master.p12 654321
```

A new directory is generated in your current directory which is named RELAY_NAME. Generated certificates will appear in this directory.

### Other hints

Log files (log4j2) are available in: `/var/log/arrowhead/*`

Output from systems are available with: `journalctl -u arrowhead-*.service`

Restart services: `sudo systemctl restart arrowhead-\*.service`

Configuration and certificates are found under: `/etc/arrowhead`

Generated passwords can be found in application.properties files located in /etc/arrowhead/systems/<system name>
directory.

Mysql database: `sudo mysql -u root`, to see the Arrowhead tables:

```SQL
use arrowhead;
show tables;
```

`apt purge` can be used to remove configuration files, log files, etc. Use `sudo apt purge arrowhead-\*` to
remove everything arrowhead related. Please note the database tables and users will not deleted by the purge.
