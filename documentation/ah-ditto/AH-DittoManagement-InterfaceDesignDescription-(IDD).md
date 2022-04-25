# AH-Ditto Management HTTP/TLS/JSON - Interface Design Description


## Abstract
This document describes the HTTP/{TLS}/JSON variant of a service that can be used to manage digital twins in the Eclipse Ditto framework by the AH-Ditto adapter system. It provides a way to do CRUD operations on Policies, Things, and Connections on Eclipse Ditto.

## Overview
This document describes the HTTP/{TLS}/JSON variant of the Ah-Ditto Management services, which allows administrator to create and manage the digital twins in Eclipse Ditto framework along with Policies and Connections. The admin can perform CRUD operations related to Things, Policies, and Connections on Eclipse Ditto by consuming these management services. The rest of this document describes how to realize these management services using HTTP, optionally with TLS, and JSON, both in terms of its interfaces and its information model.

## Service Interfaces
This section describes the interfaces that must be exposed by AH-Ditto Management services. In particular, the below subsection first names the HTTP method and path used to call the interface, after which it names an abstract interface from the AH-Ditto Management service description document, output type, as well as errors that can be thrown. The interface is expected to respond with HTTP status code 200 OK for all successful calls.

<!-- ### Things
AH-Ditto provides the following services to manage things or digital twins in the eclipse ditto framework. -->

### PUT {baseURI}/things/{id}
 - __Interface:	Create/UpdateAThing
 - __Input: [Thing](#thing)__
 - __Output: [Thing](#thing)__

Called to create or update the __[Thing](#Thing)__ specified by the `id` path parameter.

Example of valid invocation:
```json
PUT /ditto/mgmt/things/my.test:pi HTTP/1.1
Accept: application/json

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

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Type: application/json
{
    "thingId": "my.test:pi",
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

### GET {baseURI}/things/{id}
 - __Interface:	GetAThing__
 - __Output: [Thing](#thing)__

Called to acquire the __[Thing](#Thing)__ specified by the `id` path parameter.

Example of valid invocation:
```json
GET /ditto/mgmt/things/my.test:pi HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
    "thingId": "my.test:pi",
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

### DELETE {baseURI}/things/{id}
 - __Interface:	DeleteAThing__

Called to delete a __[Thing](#Thing)__ specified by the `id` path parameter.

Example of valid invocation:
```json
DELETE /ditto/mgmt/things/my.test:pi HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
```



### POST {baseURI}/connectivity/create
 - __Interface:	CreateAThing__
 - __Input: [Connection](#Connection)__
 - __Output: [Connection](#Connection)__

Called to create a [Connection](#Connection).

Example of valid invocation:
```json
POST /ditto/mgmt/connectivity/create HTTP/1.1
Accept: application/json

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

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Type: application/json
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

### POST {baseURI}/connectivity/modify/{connectionId}
 - __Interface:	ModifyAConnection__
 - __Input: [Connection](#Connection)__
 - __Output: [Connection](#Connection)__

Called to modify a [Connection](#Connection) specified by the `connectionId` path parameter.

Example of valid invocation:
```json
POST /ditto/mgmt/connectivity/modify/demo-test4 HTTP/1.1
Accept: application/json

{
    "connectionType": "mqtt",
    "connectionStatus": "open",
    "failoverEnabled": true,
    "uri": "tcp://192.168.1.210:1883",
    "sources": [{
        "addresses": ["ModifyTopic/#"],
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

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Type: application/json
{
    "id": "demo-test4",
    "connectionType": "mqtt",
    "connectionStatus": "close",
    "failoverEnabled": true,
    "uri": "tcp://192.168.1.210:1883",
    "sources": [{
        "addresses": ["ModifyTopic/#"],
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


### GET {baseURI}/connectivity/retrieve/{connectionId}
 - __Interface:	RetrieveAConnection__
 - __Output: [Connection](#Connection)__

Called to retrieve a [Connection](#Connection) specified by the `connectionId` path parameter.

Example of valid invocation:
```json
GET /ditto/mgmt/connectivity/modify/demo-test4 HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Type: application/json
{
    "id": "demo-test4",
    "connectionType": "mqtt",
    "connectionStatus": "close",
    "failoverEnabled": true,
    "uri": "tcp://192.168.1.210:1883",
    "sources": [{
        "addresses": ["ModifyTopic/#"],
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

### POST {baseURI}/connectivity/open/{connectionId}
 - __Interface:	OpenAConnection__
 - __Output: [Connection](#Connection)__

Called to open a [Connection](#Connection) specified by the `connectionId` path parameter.

Example of valid invocation:
```json
POST /ditto/mgmt/connectivity/open/demo-test4 HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Type: application/json
{
    "id": "demo-test4",
    "connectionType": "mqtt",
    "connectionStatus": "open",
    "failoverEnabled": true,
    "uri": "tcp://192.168.1.210:1883",
    "sources": [{
        "addresses": ["ModifyTopic/#"],
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

### POST {baseURI}/connectivity/close/{connectionId}
 - __Interface:	CloseAConnection__
 - __Output: [Connection](#Connection)__

Called to close a [Connection](#Connection) specified by the `connectionId` path parameter.

Example of valid invocation:
```json
POST /ditto/mgmt/connectivity/close/demo-test4 HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Type: application/json
{
    "id": "demo-test4",
    "connectionType": "mqtt",
    "connectionStatus": "close",
    "failoverEnabled": true,
    "uri": "tcp://192.168.1.210:1883",
    "sources": [{
        "addresses": ["ModifyTopic/#"],
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

### DELETE {baseURI}/connectivity/delete/{connectionId}
 - __Interface:	DeleteAConnection__
 - __Output: [Connection](#Connection)__

Called to delete a [Connection](#Connection) specified by the `connectionId` path parameter.

Example of valid invocation:
```json
POST /ditto/mgmt/connectivity/delete/demo-test4 HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
```
