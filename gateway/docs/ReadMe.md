# Gateway

<a name="gateway_sdd" />

## System Design Description Overview

This supporting core system has the purpose of establishing a secured datapath - if required - between a consumer and a provider located in different clouds by its following services:

- Connect to Consumer
- Connect to Provider

These Services are part of the Inter-Cloud Negotiation (ICN) process initiated by the requester cloud's Gatekeeper. During the ICN process, when a Gateway is required by one of the cloud, then the Gatekeepers in both cloud establish a new datapath to their application systems and ensure the data exchange via a Relay system.

![Alt text](/documentation/images/gateway_overview.png)

> Please follow this guide to setup the Arrowhead Gatekeeper and Gateway core systems: [Gatekeeper & Gateway Setup Guide with ActiveMQ Relay](documentation/gatekeeper/GatekeeperSetup.md)

<a name="gateway_usecases" />

## Services and Use Cases

Use case 1: *Connect to Consumer*

| Name | Description |
| ---- | --------- |
| ID | Connect-To-Consumer |
| Brief Description | The Gateway is tasked to connect to the Consumer and mediate between the Relay and the Consumer. |
| Primary Actors | Gatekeeper |
| Secondary Actors | - Arrowhead compliant ActiveMQ Relay |
| Preconditions | Inter-Cloud orchestration process was started by a consuming Application System. |
| Main Flow | - The Gatekeeper sends a ConnectToConsumerRequest to the Gateway. <br/>- The Gateway internally creates a new ActiveSession object. <br/>- The Gateway starts a new thread. <br/>- The Gateway creates a sslServerSocket. <br/>- The Consumer connects to the port of the serverSocket. <br/>- The Gateway gets the request from the Consumer through the SSLSocket forwards it to the Relay. <br/>- The Gateway gets the response from the Provider via the Relay, decrypts and forwards it to the Consumer through the socket. <br/>- The Gateway checks the control messages from the Relay and if a "close" message is received, than close the session. |

Use case 2: *Connect to Provider*

| Name | Description |
| ---- | --------- |
| ID | Connect-To-Provider |
| Brief Description | The Gateway is tasked to connect to the Provider and mediate between the Relay and the Provider. |
| Primary Actors | Gatekeeper |
| Secondary Actors | - Arrowhead compliant ActiveMQ Relay |
| Preconditions | Inter-Cloud orchestration process was started by a consuming Application System. |
| Main Flow | - The Gatekeeper sends a ConnectToProviderRequest to the Gateway. <br/>- The Gateway internally creates a new ActiveSession object with new queues for a choosen Relay. <br/>- The Gateway starts a new thread. <br/>- The Gateway creates a sslServerSocket. <br/>- The Gateway gets the request from the Consumer through the Relay. <br/>-  The Gateway gets the response from the Provider via the SSLSocket, then encrypts and forwards it to the Relay. <br/>- The Gateway checks the control messages from the Relay and if a "close" message is received, than close the session. |

<a name="gateway_endpoints" />

## Endpoints

<a name="gateway_endpoints_client" />

### Client endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#gateway_endpoints_get_echo) | /echo | GET    | -    | OK     |

<a name="gateway_endpoints_private" />

### Private endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Connect To Consumer](#gateway_endpoints_connect_to_consumer) | /connect_consumer | POST    | [GatewayConsumerConnectionRequest](#datastructures_gatewayconsumerconnectionrequest) | Server Port number |
| [Connect To Provider](#gateway_endpoints_connect_to_provider) | /connect_provider | POST    | [GatewayProviderConnectionRequest](#datastructures_gatewayproviderconnectionrequest) | [GatewayProviderConnectionResponse](#datastructures_gatewayproviderconnectionresponse) |
| [Get Public Key](#gateway_endpoints_get_public_key) | /publickey | GET    | - | Public Key string |

<a name="gateway_endpoints_mgmt" />

### Management endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get Active Sessions](#gateway_endpoints_get_active_sessions) | /mgmgt/sessions | GET | - | [ActiveSessionList](#datastructures_activesessionlist) |
| [Close Session](#gateway_endpoints_close_session) | /mgmgt/sessions/close | POST | [ActiveSession](#datastructures_activesession) | OK |

<a name="gateway_endpoints_get_echo" />

### Echo 
```
GET /gateway/echo
```

Returns a "Got it!" message with the purpose of testing the core service availability.

<a name="gateway_endpoints_connect_to_consumer" />

### Connect To Consumer 
```
POST /gateway/connect_consumer
```

Creates a ServerSocket between the given Relay and Consumer and return the ServerSocket port.

<a name="datastructures_gatewayconsumerconnectionrequest" />

__GatewayConsumerConnectionRequest__ is the input.

```json
{
  "consumer": {
    "systemName": "string",
    "address": "string",
    "port": 0,
    "authenticationInfo": "string"    
  },
  "consumerCloud": {    
    "name": "string",
    "operator": "string",
    "neighbor": true,
    "secure": true,
    "authenticationInfo": "string",
    "gatekeeperRelayIds": [
      0
    ],
    "gatewayRelayIds": [
      0
    ]
  },
  "provider": {
    "systemName": "string",
    "address": "string",
    "port": 0,
    "authenticationInfo": "string"
  },
  "providerCloud": {
    "name": "string",
    "operator": "string",
    "neighbor": true,
    "secure": true,
    "authenticationInfo": "string",
    "gatekeeperRelayIds": [
      0
    ],
    "gatewayRelayIds": [
      0
    ]
  },
  "providerGWPublicKey": "string",
  "peerName": "string",
  "queueId": "string",
  "relay": {
    "address": "string",
    "port": 0,
    "exclusive": true,
    "secure": true,
    "type": "string"
  },
  "serviceDefinition": "string"
}
```

| Field | Description |
| ----- | ----------- |
| `consumer` | Consumer Application System |
| `consumerCloud` | Cloud of Consumer Application System |
| `provider` | Provider Application System |
| `providerCloud` | Cloud of Provider Application System |
| `providerGWPublicKey` | Base64 encoded public key of provider cloud's Gateway |
| `peerName` | Server Common Name of provider cloud's Gateway |
| `queueId` | ID of the message queue in the Relay created by the provider |
| `relay` | Messaging Relay system |
| `serviceDefinition` | Definition of the service. |

<a name="gateway_endpoints_connect_to_provider" />

### Connect To Provider 
```
POST /gateway/connect_provider
```

Creates a Socket and Message queue between the given Relay and Provider and returns the necessary connection information.

<a name="datastructures_gatewayproviderconnectionrequest" />

__GatewayProviderConnectionRequest__ is the input.

```json
{
  "consumer": {
    "systemName": "string",
    "address": "string",
    "port": 0,
    "authenticationInfo": "string"    
  },
  "consumerCloud": {    
    "name": "string",
    "operator": "string",
    "neighbor": true,
    "secure": true,
    "authenticationInfo": "string",
    "gatekeeperRelayIds": [
      0
    ],
    "gatewayRelayIds": [
      0
    ]
  },
  "provider": {
    "systemName": "string",
    "address": "string",
    "port": 0,
    "authenticationInfo": "string"
  },
  "providerCloud": {
    "name": "string",
    "operator": "string",
    "neighbor": true,
    "secure": true,
    "authenticationInfo": "string",
    "gatekeeperRelayIds": [
      0
    ],
    "gatewayRelayIds": [
      0
    ]
  },
  "consumerGWPublicKey": "string",
  "relay": {
    "address": "string",
    "port": 0,
    "exclusive": true,
    "secure": true,
    "type": "string"
  },
  "serviceDefinition": "string"
}
```

| Field | Description |
| ----- | ----------- |
| `consumer` | Consumer Application System |
| `consumerCloud` | Cloud of Consumer Application System |
| `provider` | Provider Application System |
| `providerCloud` | Cloud of Provider Application System |
| `consumerGWPublicKey` | Base64 encoded public key of consumer cloud's Gateway |
| `relay` | Messaging Relay system |
| `serviceDefinition` | Definition of the service. |

<a name="datastructures_gatewayproviderconnectionresponse" />

__GatewayProviderConnectionResponse__ is the output.

```json
{
  "peerName": "string",
  "queueId": "string",
  "providerGWPublicKey": "string"  
}
```

| Field | Description |
| ----- | ----------- |
| `peerName` | Server Common Name of provider cloud's Gateway |
| `queueId` | ID of the message queue in the Relay created by the provider |
| `providerGWPublicKey` | Base64 encoded public key of provider cloud's Gateway |

<a name="gateway_endpoints_get_public_key" />

### Get Public Key
```
GET /gateway/publickey
```

Returns the public key of the Gateway core service as a Base64 encoded text.

<a name="gateway_endpoints_get_active_sessions" />

### Get Active Sessions
```
GET /gateway/mgmgt/sessions
```
Returns active Gateway sessions by the given paging parameters. If `page` and `item_per_page` are
not defined, no paging is involved.             

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `page` | zero based page index | no |
| `item_per_page` | maximum number of items returned | no |

<a name="datastructures_activesessionlist" />

__ActiveSessionList__ is the output.

```json
{
  "count": 0,
  "data": [
    {
      "queueId": "string",
	  "peerName": "string",
	  "consumer": {
        "systemName": "string",
		"address": "string",
        "port": 0,
		"authenticationInfo": "string"        
      },
      "consumerCloud": {
        "name": "string",
		"operator": "string",
		"authenticationInfo": "string",        
        "neighbor": true,        
        "secure": true,
		"gatekeeperRelayIds": [
          0
        ],
        "gatewayRelayIds": [
          0
        ]
      },      
      "provider": {
        "systemName": "string",
		"address": "string",
        "port": 0,
		"authenticationInfo": "string"
      },
      "providerCloud": {
        "name": "string",
		"operator": "string",
		"authenticationInfo": "string",        
        "neighbor": true,        
        "secure": true,
		"gatekeeperRelayIds": [
          0
        ],
        "gatewayRelayIds": [
          0
        ]
      },
	  "serviceDefinition": "string",
      "relay": {
        "address": "string",
		"port": 0,
        "exclusive": true,        
        "secure": true,
        "type": "GATEWAY_RELAY"
      },
      "requestQueue": "string",
	  "requestControlQueue": "string",
      "responseQueue": "string",
      "responseControlQueue": "string",      
      "sessionStartedAt": "string",
	  "consumerServerSocketPort": 0
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `count` | Number of record found |
| `data` | Array of data |
| `queueId` | ID of the message queue in the Relay created by the provider |
| `peerName` | Server Common Name of provider cloud's Gateway |
| `consumer` | Consumer Application System |
| `consumerCloud` | Cloud of Consumer Application System |
| `provider` | Provider Application System |
| `providerCloud` | Cloud of Provider Application System |
| `serviceDefinition` | Definition of the service. |
| `relay` | Messaging Relay system |
| `requestQueue` | request messaging queue through the the Relay |
| `requestControlQueue` | control queue of request messaging through the the Relay |
| `responseQueue` | response messaging queue through the the Relay |
| `responseControlQueue` | control queue of response messaging through the the Relay |
| `sessionStartedAt` | Time stamp of session start |
| `consumerServerSocketPort` | Port number delegated to consumer connection |

<a name="gateway_endpoints_close_session" />

### Close Session
```
POST /gateway/mgmgt/sessions/close
```

Closing the requested active gateway session.

<a name="datastructures_activesession" />

__ActiveSession__ is the output.

```json
{
  "queueId": "string",
  "peerName": "string",
	"consumer": {
    "systemName": "string",
	"address": "string",
    "port": 0,
	"authenticationInfo": "string"        
  },
  "consumerCloud": {
    "name": "string",
	"operator": "string",
	"authenticationInfo": "string",        
    "neighbor": true,        
    "secure": true,
	"gatekeeperRelayIds": [
      0
    ],
    "gatewayRelayIds": [
      0
    ]
  },      
  "provider": {
    "systemName": "string",
	"address": "string",
    "port": 0,
	"authenticationInfo": "string"
  },
  "providerCloud": {
    "name": "string",
	"operator": "string",
	"authenticationInfo": "string",        
    "neighbor": true,        
    "secure": true,
	"gatekeeperRelayIds": [
      0
    ],
    "gatewayRelayIds": [
      0
    ]
  },
  "serviceDefinition": "string",
  "relay": {
    "address": "string",
	"port": 0,
    "exclusive": true,        
    "secure": true,
    "type": "GATEWAY_RELAY"
  },
  "requestQueue": "string",
  "requestControlQueue": "string",
  "responseQueue": "string",
  "responseControlQueue": "string",      
  "sessionStartedAt": "string",
  "consumerServerSocketPort": 0
}
```

| Field | Description |
| ----- | ----------- |
| `queueId` | ID of the message queue in the Relay created by the provider |
| `peerName` | Server Common Name of provider cloud's Gateway |
| `consumer` | Consumer Application System |
| `consumerCloud` | Cloud of Consumer Application System |
| `provider` | Provider Application System |
| `providerCloud` | Cloud of Provider Application System |
| `serviceDefinition` | Definition of the service. |
| `relay` | Messaging Relay system |
| `requestQueue` | request messaging queue through the the Relay |
| `requestControlQueue` | control queue of request messaging through the the Relay |
| `responseQueue` | response messaging queue through the the Relay |
| `responseControlQueue` | control queue of response messaging through the the Relay |
| `sessionStartedAt` | Time stamp of session start |
