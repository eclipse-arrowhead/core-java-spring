## Setup Guide for using Arrowhead Gatekeeper and Gateway core systems

Arrowhead Gatekeeper and Arrowhead Gateway are two optional core systems that allow consumers in the local cloud to use providers from other clouds. Because local clouds often can't be accessed from outside, the gatekeeper and gateway systems use a third party software (ActiveMQ) as relay to make connection between clouds.
**Please note that Arrowhead Gatekeeper and Arrowhead Gateway core systems work only in secure mode.**
**Please also note that the ActiveMQ server has to be accessible for every participant clouds.**
The following is a quick guide on the essentials.

### 1. Install ActiveMQ

The Arrowhead Gatekeeper and Arrowhead Gateway can use any messaging service that implements JMS (Java Message Service) specification. Our default implementation is using ActiveMQ Classic.

You can download it from the following link for both Windows and Unix/Linux operation systems:

[ActiveMQ Classic](https://activemq.apache.org/components/classic/download/)

The release version of ActiveMQ Classic is just an archive file (ZIP or tar.gz). After extraction you can use it.

**Please note that ActiveMQ needs Java 7+ in able to operate but since Arrowhead needs Java 11 this should not be a problem.**

Some help for the installation:

[ActiveMQ Classic Documentation](https://activemq.apache.org/using-activemq)

[A Guide to installing ActiveMQ on Ubuntu 18.04](https://websiteforstudents.com/how-to-install-apache-activemq-on-ubuntu-18-04-16-04/)

Before starting the ActiveMQ server you have to modify the configuration. The `activemq.xml` file contains all settings of the server which is located in the `conf` subfolder of the ActiveMQ installation folder.

In the first step you have to make sure that ActiveMQ cleans up all the possibly stucked queues. Even though Arrowhead Gatekeeper and Gateway systems have full control on creating and removing queues, some of them could stuck when for example unexpected hard shutdown of Gatekeeper/Gateway have happened.
- Find the tag `<broker>` in the configuration file and add an attribute to it:
```schedulePeriodForDestinationPurge="60000"```
- Then find the tag `<policyEntries>` under the `<broker>` tag and append a new entry. Set `inactiveTimeoutBeforeGC` at least five time bigger then the `inactive_gateway_bridge_timeout` in the Gateway Core system :
```<policyEntry queue=">" gcInactiveDestinations="true" inactiveTimoutBeforeGC="300000"/>```
- After these modifications your configuration files should look similar than this:
```
 <broker xmlns="http://activemq.apache.org/schema/core" brokerName="localhost" dataDirectory="${activemq.data}" schedulePeriodForDestinationPurge="60000">

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
                <policyEntry queue=">" gcInactiveDestinations="true" inactiveTimeoutBeforeGC="300000"/>
              </policyEntries>
            </policyMap>
        </destinationPolicy>
	
	    ...
```

The next steps depend on your choice of security. ActiveMQ can work using simple TCP protocol (insecure mode) or SSL protocol (secure mode). 
**Please note that even if you choose insecure mode Arrowhead Gatekeeper and Gateway core systems will encrypt and sign every message before sending through the relay.**

#### 1.1 Configuring insecure (TCP) mode

Find the tag `<transportConnectors>`. There is a lot of connector entries can be found here. You can delete them except the one that named *openwire*:
``` <transportConnector name="openwire" uri="tcp://0.0.0.0:61616?maximumConnections=1000&amp;wireFormat.maxFrameSize=104857600"/>```
Please take notice that the relay will listen for messages on port `61616`.

#### 1.2 Configuring secure (SSL)  mode

In SSL mode, ActiveMQ needs a certificate and a trust store. For testing purposes, you can use the ones you can find in the *core-java-spring* project's `certificates/relay1` subfolder which can work with `testcloud1` and `testcloud2` local clouds. In production environment, you have to create your own certificates and trust store. 

##### 1.2.1 Creating certificates

For relays we introduced a very similar certificate chain than we use in case of Arrowhead systems ([see README.md for details](https://github.com/arrowhead-f/core-java-spring#certificates)). It consists three levels:
- Master certificate: `arrowhead.eu` (it's the Arrowhead master certificate)
- Relay master certificate: `relay.arrowhead.eu` (it's basically the same than a cloud certificate in Arrowhead, but the CN has only 3 parts)
- Relay certificate: `my-relay.relay.arrowhead.eu` (it's very similar to the Arrowhead system's certificates, but the CN has only 4 parts)

For creating these certificates, please consulting the previously linked README document. 
You need a trust store as well. It is also similar to the one we use in case of Arrowhead systems, but you have to import the master certificate as trusted certificate instead of the cloud certificate.

#### 1.2.2 Setting certificates

After you have the necessary certificate and trust store you have to install them.
- Go to the `<ActiveMQ installation folder>/conf` folder.
- Remove the following files from the folder: `broker.ks`, `broker.ts`, `client.ks`, `client.ts` These are the default certificates and trust stores for demo purposes.
- Copy the relay certificate and the relay trust store to this folder.
- Go back to the `activemq.xml` file and add the following configuration entry after the `<destinationPolicy>` section:
```
 <sslContext>
    <sslContext keyStore="file:${activemq.base}/conf/relay1.p12"
                keyStorePassword="123456"
                keyStoreKeyPassword="123456"
                trustStore="file:${activemq.base}/conf/relay1-truststore.p12"
                trustStorePassword="123456" />
 </sslContext>
```
- In the lines above we assume that the certificate file named `relay1.p12`, the trust store file named `relay1-truststore.p12` and all the passwords are `123456`. If this is not true, please adjust the settings accordingly.

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

- Open the `application.properties` of the Orchestrator core system (which is located besides the `arrowhead-orchestrator-4.1.3.jar` or in the `/etc/arrowhead/systems/orchestrator` folder if you have used the Debian installer).
- Find the property named `gatekeeper_is_present` and set to `true`. 
- Save the file.
- Restart the Orchestrator core system.
```
sudo systemctl stop arrowhead-orchestrator.service
sudo systemctl start arrowhead-orchestrator.service
```
if you have used the Debian installer to install Arrowhead core systems.

- Open the `application.properties` of the Gatekeeper core system (which is located besides the `arrowhead-gatekeeper-4.1.3.jar` or in the `/etc/arrowhead/systems/gatekeeper` folder if you have used the Debian installer).
- Find the properties named `gateway_is_present` and `gateway_is_mandatory` and set both of them to `true`.
- Save the file.

- If you configured a secure ActiveMQ server before there is one step you must take before starting the gatekeeper and gateway systems. You have to append the relay master certificate to the trust stores of these core systems. *Please note the in case of test trust stores (`testcloud1` and `testcloud2`) we have already done that.*

After this last step you can start the Arrowhead Gatekeeper and Arrowhead Gateway systems. If you have used the Debian installers to install the Arrowhead core systems, these two systems are already running so you have to restart it:

```
sudo systemctl stop arrowhead-gatekeeper.service
sudo systemctl stop arrowhead-gateway.service
sudo systemctl start arrowhead-gateway.service
sudo systemctl start arrowhead-gatekeeper.service
```

The goal is to create connection between local clouds so you have to do these steps (editing configuration files, extending trust stores and restarting systems) in all participating clouds. For testing you need at least two (let's call them *testcloud1* and *tetstcloud2*).

### 3. Adding Relay

A local cloud must know about the relay before using it, so you have to register the relay into the cloud's database. You can use the gatekeeper's Swagger interface to register the relay (or you can use the management tool if available):
- In a browser open the following address:
```https://<ip address of the testcloud1's gatekeeper>:8449```
- Select the `/gatekeeper/mgmt/relays` POST request 
- The input JSON object should be something like this:
```
[
  {
    "address": "<ip address of the ActiveMQ server>",
    "port": 61616, 
    "type": "GENERAL_RELAY",
    "exclusive": false,
    "secure": false
  }
]
```
**Please note that the JSON above represents an insecure relay. If you want to use a secure relay, you have to modify the port (`61617`) and the secure field (`true`).**
Repeat this step using the Swagger interface of the *testcloud2*'s gatekeeper.

### 4. Adding Neighbour Clouds

Arrowhead clouds must know each other if they want to communicate. So the next task is to registering *testcloud2* to *testcloud1*'s database as a neighboring cloud (and vice versa). To do this, we will need the text representation of the public key of *testcloud2*'s gatekeeper (also known as authorization info).

#### 4.1 Acquiring Authorization Info

We need the public key of *testcloud2*'s gatekeeper in PEM format. In real life, you have to contact the operators of the *testcloud2* and ask them to send the key. It's OK to send it via e-mail because it is a PUBLIC key. In test environment, you have to obtain the public key from the `gatekeeper.p12` file by using the following command:

```
keytool -list -rfc -keystore gatekeeper.p12 -alias gatekeeper -storepass 123456 | openssl x509 -inform pem -pubkey -noout
```

The `keytool` program comes with the Java 11. If you don't have `openssl`, please install it.
**Please note that the command above contains the password of the certificate file in the `-storepass` argument so you have to change that to the actual password before executing the command.**
The output of the command will be something similar:
```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyxw7OMHft33H69BgLiXm
iNI1nTo2mdRIoDru0q5BLg0RQOKZD4woSeDd7LYNV1p66YAZWEan6+TUi5EGC8kX
naLNL52nwal3p1/2TAY+p+95OtI9iUVfI5pzfyxEVxc1nqV40F70XNKoFduPWLFw
YaSEg3cXaBiUSiCgTsMQzoEZQ9o7ueTxnUrBgx0UlsuQQOdKagTJMfuTF1/2IKMt
lVgnHZ5/yVTRtsaUlage+TG/9tto2pxd3TWj5rTGGLDbkKSg4BP9YOHcTTnJZNbG
1bnRwgDrpEPI4TaK6GNOQicRTiKLjv79/EyGFJzgTkcPJ0lW4pc9Al/2Dx15z9L6
owIDAQAB
-----END PUBLIC KEY-----
```
We don't need the `BEGIN PUBLIC KEY` prefix and the `END PUBLIC KEY` suffix and you have to eliminate all line breaks as well. So the desired output of the public key above is
```
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyxw7OMHft33H69BgLiXmiNI1nTo2mdRIoDru0q5BLg0RQOKZD4woSeDd7LYNV1p66YAZWEan6+TUi5EGC8kXnaLNL52nwal3p1/2TAY+p+95OtI9iUVfI5pzfyxEVxc1nqV40F70XNKoFduPWLFwYaSEg3cXaBiUSiCgTsMQzoEZQ9o7ueTxnUrBgx0UlsuQQOdKagTJMfuTF1/2IKMtlVgnHZ5/yVTRtsaUlage+TG/9tto2pxd3TWj5rTGGLDbkKSg4BP9YOHcTTnJZNbG1bnRwgDrpEPI4TaK6GNOQicRTiKLjv79/EyGFJzgTkcPJ0lW4pc9Al/2Dx15z9L6owIDAQAB
```
in one line. This is the authorization info we will use in the next step.

#### 4.2 Registering Neighbour Cloud `testcloud2`

You can use the gatekeeper's Swagger interface of `testcloud1` to register `testcloud2` (or you can use the management tool if available):
- In a browser open the following address:
```https://<ip address of the testcloud1's gatekeeper>:8449```
- Select the `/gatekeeper/mgmt/relays` GET request and call it without any paramaters. This will return the available relay list (something similar like this):
```
{
  "data": [
    {
      "id": 1,
      "address": "10.0.0.85",
      "port": 61617,
      "secure": true,
      "exclusive": false,
      "type": "GENERAL_RELAY",
      "createdAt": "2019-10-17 10:16:33",
      "updatedAt": "2019-10-17 10:16:33"
    }
  ],
  "count": 1
}
```
- The important thing here is the id of the relay we registered before (`1` in our example).
- Next, select the `/gatekeeper/mgmt/clouds` POST request.
- The input JSON object should be something like this:

```
[
  {
    "name": "testcloud2",
    "operator": "aitia",
    "authenticationInfo": "<here comes the long string we acquired in the previous step>",
    "neighbor": true,
    "secure": true,
    "gatekeeperRelayIds": [
      1
    ]
  }
]
```

#### 4.3 Vice versa

The whole process must be repeat: 
- you have to acquire the authorization info of the gatekeeper of `testcloud1` and
- using the Swagger interface of `testcloud2`'s gatekeeper you have to register `testcloud1` to the database of `testcloud2` as a neighbour cloud.

After these steps everything is set for consuming services from an other cloud in a secure way. **Please note that you have to explicitly enable the intercloud orchestration in the orchestration form (by setting the `enableInterCloud` flag to `true`). Also, the consumer cloud must have access to a specific service of a specific provider before any consumer system of that cloud can consume the service (see [web service documentation](https://github.com/arrowhead-f/core-java-spring/blob/master/README.md#authorization_endpoints_post_intercloud) if you need help adding the necessary authorization rules).**
