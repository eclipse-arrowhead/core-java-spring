# AH-Ditto Demo

## Summary

This is an example use-case of AH-Ditto (Eclipse Arrowhead Adapter system to integrate [Eclipse Ditto](https://www.eclipse.org/ditto/) into [Eclipse Arrowhead](https://www.arrowhead.eu/eclipse-arrowhead)) to demonstrate digital twin as a proxy (DTaaP) in Eclipse Arrowhead framework. 
In this example we will create a digital twin of a device. The device have two sensing features of temperature and humidity. The digital twin of this device is created in Eclipse Ditto via AH-Ditto. Device sends data to its digital twin in intervals, whereas the consumers of Eclipse Arrowhead framework will always be able to get the data from its digital tiwn via AH-Ditto.

## Prerequisites
### 1. Eclipse Ditto  
You can either use the [ditto sandbox](https://www.eclipse.org/ditto/sandbox.html), clone the latest version from [github](https://github.com/eclipse/ditto) or pull the latest Docker images from the
[docker hub](https://hub.docker.com/search/?isAutomated=0&isOfficial=0&page=1&pullCount=0&q=eclipse+ditto&starCount=0).

* In order to start the latest built Docker images from Docker Hub, simply execute:
    ```bash
    cd deployment/docker/
    docker-compose up -d
    ```
* Open following URL to get started: http://localhost:8080
### 2. Eclipse Arrowhead
We need to run the Arrowhead core systems. You can get the working copy of these systems from [Github](https://github.com/Xarepo/core-java-spring/) specifically from branch `ditto-integration`. After cloning and pulling from this branch we can do as follows to run the core systems:
* from the root folder of ```core-java-spring```, we need to run this command to make the jar files without tests.
    ``` 
    mvn install -DskipTests 
    ```
* To start service_registry:
    * got to serviceregistry/target by ```cd serviceregistry/target```
    * Execute ``` java -jar arrowhead-serviceregistry-4.4.1.jar ```
    * Service Registry will be available on ```https://localhost:8443```
* To start authorization:
    * got to serviceregistry/target by ```cd authorization/target```
    * Execute ``` java -jar arrowhead-authorization-4.4.1.jar ```
    * Authorization will be available on ```https://localhost:8445```
* To start orchestrator:
    * got to serviceregistry/target by ```cd orchestrator/target```
    * Execute ``` java -jar arrowhead-orchestrator-4.4.1.jar ```
    * Orchestrator will be available on ```https://localhost:8441```

**Note**
> For setting up Eclipse Arrowhead we should have set up `MySql` databases according to the guidelines provided on [Github](https://github.com/Xarepo/core-java-spring/).

### 3. AH-Ditto
We need to run the AH-Ditto arrowhead system from the branch `ditto-integration`. We can run directly by maven or if using ``vs code`` we can run by creating the `launch.json` file and run by launching `DittoMain`.
* Ah-Ditto will be available on ```https://localhost:8464```

### 2. Eclipse Mosquitto
We can either use the [mosquitto test server](https://test.mosquitto.org/), clone the latest version
from [github](https://github.com/eclipse/mosquitto) or run it in a Docker container locally on your machine via:
`docker run -d -p 1883:1883 -p 9001:9001 eclipse-mosquitto`. 
## Create Policy

Currently, AH-Ditto creates a dummy policy on Thing creation. But the policy for AH-Ditto is supposed to be as shown below and in `policy.json`. Created policy have a policyID of `eu.arrowhead:ah-ditto`.

```json
{
  "entries": {
    "ah-ditto-admin": {
      "subjects": {
        "nginx:ah-ditto-admin": {
          "type": "nginx basic auth user"
        }
      },
      "resources": {
        "thing:/": {
          "grant": [
            "READ",
            "WRITE"
          ],
          "revoke": []
        },
        "policy:/": {
          "grant": [
            "READ",
            "WRITE"
          ],
          "revoke": []
        },
        "message:/": {
          "grant": [
            "READ",
            "WRITE"
          ],
          "revoke": []
        }
      }
    },
    "ah-ditto-consumer": {
      "subjects": {
        "nginx:ah-ditto-consumer": {
          "type": "technical client"
        }
      },
      "resources": {
        "thing:/features/": {
          "grant": [
            "READ"
          ],
          "revoke": []
        }
      }
    }
  }
}
```

## Create Thing
The device we chose for this demo have two features of `thermometer` and `humidity`. The model for this device used to creat the thing is saved in `thing.json` is as follows:
```json
{
    "policyId": "eu.arrowhead:ah-ditto",
    "attributes": {
        "name": "pi",
        "type": "pi board",
        "serviceDefinitions": {
            "thermometer": "thermometer",
            "humidity":"humidity"
        }
    },
    "features": {
        "thermometer": {
            "properties": {
                "temperature": 0
            }
        },
        "humidity": {
            "properties": {
                "hum": 0
            }
        }
    }
}
```
We can create the thing by consuming the management service of AH-Ditto. If we set the `thingId` as `my.test:pi` the request will be as follows with the `thing.json` as body.
``` 
PUT /ditto/mgmt/things/{thingId} 
```
### Service registration to Eclipse Arrowhead Framework
Once AH-Ditto recieved the request it create thing based on the model in the Eclipse Ditto. As soon as the thing is created in Eclipse Ditto, AH-Ditto register the services in service_registry based on the features. As the thing model have two features as `thermometer` and `humidity`, the services registered in service_registry have the following endpoints.
* thermometer => `/ditto/access/things/my.test:pi/features/thermometer`
* humidity => `/ditto/access/things/my.test:pi/features/humidity`

Now the consumers from the Eclipse Arrowhead Framework can discover these services via `orchestrator` and can consume by AH-Ditto system via above service endpoints.

**Note**
> Once the services are registered in the service registry, we need to update the orchestration store rules. We can do this via running the `orchesteratorStoreRules.sql` script from `mysql`.

## Create Connection
In order to establish communication between the device and Eclipse Ditto we need to set up and launch an MQTT broker. Now we need to create and open a connection in Eclipse Ditto. To do that we need to consume a management service of AH-Ditto with the `connection.json` as body payload.
```
POST /ditto/mgmt/connectivity/create
```
The connection model to send is as follows:
```json
{
    "id": "demo-test4",
    "connectionType": "mqtt",
    "connectionStatus": "open",
    "failoverEnabled": true,
    "uri": "tcp://192.168.1.210:1883",
    "sources": [{
        "addresses": ["ditto-tutorial4/#"],
        "authorizationContext": ["nginx:ditto"],
        "qos": 0,
        "filters": []
    }],
    "targets": [{
        "address": "ditto-tutorial4/{{ thing:id }}",
        "topics": ["_/_/things/twin/events", "_/_/things/live/messages"],
        "authorizationContext": ["nginx:ditto"],
        "qos": 0
    }],
    "mappingContext": {
        "mappingEngine": "JavaScript",
        "options": {
            "incomingScript": "function mapToDittoProtocolMsg(headers, textPayload, bytePayload, contentType) { const jsonString = String.fromCharCode.apply(null, new Uint8Array(bytePayload)); const jsonData = JSON.parse(jsonString); const thingId = jsonData.thingId.split(':'); const value = { thermometer: { properties: { temperature: jsonData.temp } }, humidity: { properties: { hum: jsonData.hum } } }; return Ditto.buildDittoProtocolMsg(thingId[0], thingId[1], 'things', 'twin','commands', 'modify', '/features', headers, value); }",
            "loadBytebufferJS": "false",
            "loadLongJS": "false"
        }
    }
}
```
*Note**
> The `uri` is an ip of server where `MQTT` is running. 

### Payload Mapping
Eclipse Ditto need to map the incoming message comming from the device to Ditto-Protocol. Therefore, we must need to specify `mappingContext` of the connection and provide the `incomingScript` need to use for the mapping.

Our device sends a meesage as follows:
```json
{
    "temp": 30.67,
    "hum": 36,
    "thingId": "my.test:pi"
}
```

Ditto has to map the message above to a Ditto-protocol message:
```json
{
    "thingId": "my.test:pi",
    "policyId": "eu.arrowhead:ah-ditto",
    "features": {
        "thermometer": {
            "properties": {
                "temperature": 0
            }
        },
        "humidity": {
            "properties": {
                "hum": 0
            }
        }
    }
}
```

Therefore, we define the following `incomingScript` mapping function:

```javascript
function mapToDittoProtocolMsg(headers, textPayload, bytePayload, contentType) {
    const jsonString = String.fromCharCode.apply(null, new Uint8Array(bytePayload));
    const jsonData = JSON.parse(jsonString); const thingId = jsonData.thingId.split(':'); 
    const value = { 
        thermometer: 
        { 
            properties: 
            { 
                temperature: jsonData.temp 
            } 
        },
        humidity: 
        { 
            properties: 
            { 
                hum: jsonData.hum 
            } 
        } 
    }; 
    return Ditto.buildDittoProtocolMsg(
        thingId[0], 
        thingId[1], 
        'things', 
        'twin',
        'commands', 
        'modify', 
        '/features', 
        headers, 
        value
        );
}
```

## Consumer Application

For our consumer example application we have used an actuator fan. Based on the temperature value the fan maintain its speed. There fan always want to consume the service from our device. The device can be in sleep state and therefore cannot provide service all the time. But with the presence of digital twin thr device only need to populate its digital twin with the update temperature value and the consumer can then get the value anytime from the digital twin. The consumer application can get from this [GitHUb](https://github.com/Xarepo/pde-tests)

To run this from `vscode`, we can create the `launch.json` file and update the contents of the file as follows:

```json
{
    "version": "0.2.0",
    "configurations": [
            {
                "type": "java",
                "name": "Fan",
                "request": "launch",
                "mainClass": "eu.arrowhead.core.fan.Fan",
                "projectName": "fan",
                "args": [
                    "config/properties/fan.properties",
                    ]
            }
        ]
    }
}
```