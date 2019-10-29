## Setup Guide for using Arrowhead Gatekeeper and Gateway core systems

Arrowhead Gatekeeper and Arrowhead Gateway are two optional core systems that allow consumers in the local cloud to use providers from other clouds. Because local clouds often can't be accessed from outside, the gatekeeper and gateway systems use a third party software (ActiveMQ) as relay to make connection between clouds.
**Please note that Arrowhead Gatekeeper and Arrowhead Gateway core systems work only in secure mode.**

The following is a quick guide on the essentials.

### 1. Install ActiveMQ

The Arrowhead Gatekeeper and Arrowhead Gateway can use any messaging service that implements JMS (Java Message Service) specification. Our default implementation is using ActiveMQ Classic.

You can download it from the following link for both Windows and Unix/Linux operation systems:
[ActiveMQ Classic](https://activemq.apache.org/components/classic/download/)
The release version of ActiveMQ Classic is just an archive file (ZIP or tar.gz). After extraction you can use it.

**Please note that ActiveMQ needs Java 7+ in able to operate but since Arrowhead needs Java 11 this should not be a problem.**

[ActiveMQ Classic Documentation](https://activemq.apache.org/using-activemq)
[A Guide to installing ActiveMQ on Ubuntu 18.04](https://www.osradar.com/install-apachemq-ubuntu-18-04/)

Before starting the ActiveMQ server you have to modify the configuration. The `activemq.xml` file contains all settings of the server which is located in the `conf` subfolder of the ActiveMQ installation folder.

In the first step you have to make sure that ActiveMQ cleans up all the unused queues because the Arrowhead Gatekeeper and Gateway systems create a lot of queues but don't/can't delete them.

- Find the tag `<broker>` in the configuration file and add an attribute to it:

```schedulePeriodForDestinationPurge="10000"```

- Then find the tag `<policyEntries>` under the `<broker>` tag and append a new entry:

```<policyEntry queue=">" gcInactiveDestinations="true" inactiveTimoutBeforeGC="60000"/>```

- After these modifications your configuration files should looks similar than this:

```
 <broker xmlns="http://activemq.apache.org/schema/core" brokerName="localhost" dataDirectory="${activemq.data}" schedulePeriodForDestinationPurge="10000">

        <destinationPolicy>
            <policyMap>
              <policyEntries>
                <policyEntry topic=">" >
                    <!-- The constantPendingMessageLimitStrategy is used to prevent
                         slow topic consumers to block producers and affect other consumers
                         by limiting the number of messages that are retained
                         For more information, see:

                         http://activemq.apache.org/slow-consumer-handling.html

                    -->
                  <pendingMessageLimitStrategy>
                    <constantPendingMessageLimitStrategy limit="1000"/>
                  </pendingMessageLimitStrategy>
                </policyEntry>
                <policyEntry queue=">" gcInactiveDestinations="true" inactiveTimoutBeforeGC="60000"/>
              </policyEntries>
            </policyMap>
        </destinationPolicy>
	
	    ...
```

The next steps are depends on your choice of security. ActiveMQ can work using simple TCP protocol (insecure mode) or SSL protocol (secure mode). 
**Please note that even if you choose insecure mode Arrowhead Gatekeeper and Arrowhead Gateway core systems will encrypt and sign every message before sending through the relay.**

#### 1.1 Configuring insecure (TCP) mode

Find the tag `<transportConnectors>`. There is a lot of connector entries can be found here. You can delete them except the one that named *openwire*:
``` <transportConnector name="openwire" uri="tcp://0.0.0.0:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>```

Please take notice that the relay will listen for messages on port `61616`.

#### 1.2 Configuring secure (SSL)  mode

In SSL mode, ActiveMQ needs certificate and a trust store. For testing purposes, you can use the ones you can find in the *core-java-spring* project's `certificates/relay1` subfolder which can work with `testcloud1` and `testcloud2` local clouds. In production environment, you have to create your own certificates and trust store. 

##### 1.2.1 Creating certificates

For relays we introduced a very similar certificate chain than we use in case of Arrowhead systems ([see README.md for details](https://github.com/arrowhead-f/core-java-spring#certificates)). It consists three levels:
- Master certificate: `arrowhead.eu` (It's the Arrowhead master certificate)
- Relay master certificate: `relay.arrowhead.eu` (It's basically the same than a cloud certificate in Arrowhead, but the CN has only 3 parts)
- Relay certificate: `my_relay.relay.arrowhead.eu` (It's very similar to the Arrowhead system's certificates, but the C_N has only 4 parts)

For creating these certificates, please consulting the previously linked README document. 
You need a trust store as well. It is also similar to the one we use in case of Arrowhead systems, but you have to import the master certificate ss trusted certificate instead of the cloud certificate.

#### 1.2.2 Setting certificates

After you have the necessary certificate and trust store you have to install them.

- Go to the `<activemq installation folder>/conf` folder.
- Remove the following files from the folder: `broker.ks, broker.ts, client.ks, client.ts` These are the default certificates and trust stores for demo purposes.
- Copy the relay certificate and the relay trust store to this folder.
- Go back to the `activemq.xml` file and add the following configuration entry after the `<destinationPolicy>` section:
```
 <sslContext>
    <sslContext keyStore="file:${activemq.base}/conf/relay1.p12"
                keyStorePassword="123456"
                keyStoreKeyPassword="123456"
                trustStore="file:${activemq.base}/conf/relay-truststore.p12"
                trustStorePassword="123456" />
 </sslContext>
```
- In the lines above we assume that the certificate file named `relay1.p12`, the trust store file named `relay-truststore.p12` and all the passwords are `123456`. If this is not true, please adjust the settings accordingly.

#### 1.2.3 Configuring transport connector

Still in the configuration file, you have to modify the transport connector to enable using SSL protocol.

- Find the tag `<transportConnectors>`.
- There is a lot of connector entries can be found here. You should delete them.
- Add the following transport connector entry:
```<transportConnector name="ssl" uri="ssl://0.0.0.0:61617?trace=true&amp;needClientAuth=true" />```

Please take notice that the relay will listen for messages on port `61617`.

After saving the configuration file, you should run the ActiveMQ server from the `bin` directory executing the `activemq` file.

Check ActiveMQ is running using a browser:

```http://<ip address of ActiveMQ server>:8161/admin```

The username and password is `admin/admin` by default.

### 2. Starting Arrowhead Gatekeepers and Arrowhead Gateways

If you use secure ActiveMQ server there is one step you must take before starting the gatekeeper and gateway systems. You have to append the relay master certificate to the trust stores of these core systems. *Please note the in case of test trust stores (`testcloud1` and `testcloud2`) we already done that.*

After this last step you can start the Arrowhead Gatekeeper and Arrowhead Gateway systems. If you use the Debian installers to install the Arrowhead core systems, these two systems are already running so you have to restart it:

```
sudo systemctl stop arrowhead-gatekeeper.service
sudo systemctl stop arrowhead-gateway.service
sudo systemctl start arrowhead-gateway.service
sudo systemctl start arrowhead-gatekeeper.service
```

The goal is to create connection between local clouds so you have to do these steps (extending trust stores and restarting systems) in all participating clouds. For testing you need at least two (let's call them testcloud1 and tetstcloud2).

### 3. Adding Relay

A local cloud must know about the relay before using it, so you have to register the relay into the cloud's database. You can use Gatekeeper's Swagger interface to register the relay (or you can use the management tool if available):

- In a browser open the following address:

```https://<ip address of the testcloud1's gatekeeper>:8449```
- Select the `/gatekeeper/mgmt/relays` POST request 
- The input JSON object should be something like this:
```
[
  {
    "address": "ip address of the ActiveMQ server",
    "exclusive": false,
    "port": 61616, 
    "secure": false,
    "type": "GENERAL_RELAY"
  }
]
```
**Please note that the JSON above represents an insecure relay. If you want to use a secure relay, you have to modify the port (61617) and the secure field (true).**

Repeat this step using the Swagger interface of the testcloud2's gatekeeper.

### 4. Adding Neighbor Clouds

