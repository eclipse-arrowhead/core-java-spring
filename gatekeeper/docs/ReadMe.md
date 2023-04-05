# Gatekeeper 

<a name="gatekeeper_sdd" />

## System Design Description Overview

This supporting core system has the purpose of providing inter-Cloud servicing capabilities in the Arrowhead Framework by its following services:

- Global Service Discovery (GSD)
- Inter-Cloud Negotiation (ICN)

These Services are part of the inter-Cloud orchestration process, but the Gatekeeper is only available for the other core systems. Gatekeeper is the only one core system which has the functionality of discovering other Clouds via Relay systems. Neighbor Clouds and Relay systems are stored in the MySQL database of this module.  
During the inter-Cloud orchestration, the Global Service Discovery is the first process which aims to collect the known clouds with providers serving the specified service. After GSD, the Inter Cloud Negotiation process steps in place with the purpose of establishing the way of collaboration. Working together with the Orchestrators of both Clouds, at the end a servicing instace can be created.

![Alt text](/documentation/images/gatekeeper_overview.png)

> Please follow this guide to setup the Arrowhead Gatekeeper and Gateway core systems: [Gatekeeper & Gateway Setup Guide with ActiveMQ Relay](documentation/gatekeeper/GatekeeperSetup.md)

<a name="gatekeeper_usecases" />

## Services and Use Cases

Use case 1: *Global Service Discovery request*

| Name | Description |
| ---- | --------- |
| ID | GSD-1 |
| Brief Description | The Gatekeeper is tasked to find a Service in other Local Clouds |
| Primary Actors | Gatekeeper |
| Secondary Actors | - Relays used by the Gatekeeper <br/>- The Gatekeeper instances of another Clouds |
| Preconditions | Orchestration process was started by an Application System. |
| Main Flow | - The Orchestrator consumes the GSD Initialization Service of its local Gatekeeper. <br/>- Gatekeeper collects the preferred or neighbor Clouds and one of its Relays. <br/>- The Gatekeeper queries the other Gatekeepers via the Relays. <br/>- These Gatekeepers verify whether they could facilitate this request or not. <br/>- The requester Gatekeeper collects these answers and respond via the GSD Initialization Service to its Orchestrator |
| Postconditions | The Orchestrator has a list of other Local Clouds that can provide the Service we are looking for.  |

Use case 2: *Inter-Cloud Negotiation request*

| Name | Description |
| ---- | --------- |
| ID | ICN-1 |
| Brief Description | The Gatekeeper is tasked to start negotiating with another Cloud. |
| Primary Actors | Gatekeeper |
| Secondary Actors | - Relays used by the Gatekeeper <br/>- The Gatekeeper instances of another Clouds <br/>- The other Orchestrator from the second Cloud|
| Preconditions | Orchestration process was started by an Application System. The GSD process has ended, the requester Orchestrator has chosen a partnering Cloud, where it wants to connect to. |
| Main Flow | - The Orchestrator consumes the ICN Initialization Service of its local Gatekeeper. <br/>- The Gatekeeper consumes the other Gatekeeper's ICN Proposal service via an Relay.<br/>- The secondary Gatekeeper validates the AuthorizationControl and requests Orchestration from its own Orchestrator <br/>- The secondary Orhestrator responds to the secondary Gatekeeper with an Orchestration result. <br/>- The secondary Gatekeeper responds to the primary, requester Gatekeeper. <br/>- Additional administrative tasks are executed (e.g. configuration of the Gateway modules) <br/>- The primary, requester Orchestrator is receiving the response via the ICN initialization service. |

<a name="gatekeeper_endpoints" />

## Endpoints

<a name="gatekeeper_endpoints_client" />

### Client endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#gatekeeper_endpoints_get_echo)     | /echo       | GET    | -     | OK     |

<a name="gatekeeper_endpoints_private" />

### Private endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
|[Init GSD](#gatekeeper_endpoints_post_init_gsd)|/gatekeeper/init_gsd|POST|[GSDQueryForm](#datastructures_gsdqueryform)|[GSDQueryResult](#datastructures_gsdqueryresult)|
|[Init ICN](#gatekeeper_endpoints_post_init_icn)|/gatekeeper/init_icn|POST|[ICNRequestForm](#datastructures_icnrequestform)|[ICNResult](#datastructures_icnresult)|

<a name="gatekeeper_endpoints_mgmt" />

### Management endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get all Cloud entries](#gatekeeper_endpoints_get_all_cloud) | /mgmgt/clouds | GET | - | [CloudWithRelaysListResponse](#datastructures_cloudwithrelayslistresponse) |
| [Get Cloud by ID](#gatekeeper_endpoints_get_cloud_by_id) | /mgmgt/clouds/{id} | GET | cloudId | [CloudWithRelaysResponse](#datastructures_cloudwithrelaysresponse) |
| [Register Clouds](#gatekeeper_endpoints_register_clouds) | /mgmgt/clouds | POST | [CloudRequest list](#datastructures_cloudrequestlist) | [CloudWithRelaysListResponse](#datastructures_cloudwithrelayslistresponse2) |
| [Update Cloud](#gatekeeper_endpoints_update_cloud) | /mgmgt/clouds/{id} | PUT | [CloudRequest](#datastructures_cloudrequest) | [CloudWithRelaysResponse](#datastructures_cloudwithrelaysresponse2) |
| [Assign Relays to Cloud](#gatekeeper_endpoints_assign_relays_to_cloud) | /mgmgt/clouds/assign | POST | [CloudRelaysAssignmentRequest](#datastructures_cloudrelaysassignmentrequest) | [CloudWithRelaysResponse](#datastructures_cloudwithrelaysresponse3) |
| [Delete Cloud](#gatekeeper_endpoints_delete_cloud) | /mgmgt/clouds/{id} | DELETE | cloudId | - |
| [Get all Relay entries](#gatekeeper_endpoints_get_all_relay) | /mgmgt/relays | GET | - | [RelayListResponse](#datastructures_relaylistresponse) |
| [Get Relay by ID](#gatekeeper_endpoints_get_relay_by_id) | /mgmgt/relays/{id} | GET | relayId | [RelayResponse](#datastructures_relayresponse) |
| [Get Relay by Address and Port](#gatekeeper_endpoints_get_relay_by_address_and_port) | /mgmgt/relays/{address}/{port} | GET | address, port | [RelayResponse](#datastructures_relayresponse2) |
| [Register Relays](#gatekeeper_endpoints_register_relays) | /mgmgt/relays | POST | [RelayRequest list](#datastructures_relayrequestlist) | [RelayListResponse](#datastructures_relaylistresponse2) |
| [Update Relay](#gatekeeper_endpoints_update_relay) | /mgmgt/relays/{id} | PUT | [RelayRequest](#datastructures_relayrequest) | [RelayResponse](#datastructures_relayresponse3) |
| [Delete Relay](#gatekeeper_endpoints_delete_relay) | /mgmgt/relays/{id} | DELETE | relayId | - |

<a name="gatekeeper_removed" />

### Removed Endpoints <br />

The following endpoints no longer exist:
* `GET /gatekeeper/mgmt/neighborhood/operator/{operator}/cloudname/{cloudName}`
* `DELETE /gatekeeper/mgmt/neighborhood/operator/{operator}/cloudname/{cloudName}`
* `GET /gatekeeper/mgmt/brokers/brokername/{brokerName}`
* `GET /gatekeeper/mgmt/brokers/address/{address}`

<a name="gatekeeper_endpoints_get_echo" />

### Echo 
```
GET /gatekeeper/echo
```

Returns a "Got it!" message with the purpose of testing the core service availability.

<a name="gatekeeper_endpoints_post_init_gsd" />

### Init GSD 
```
POST /gatekeeper/init_gsd
```

Returns the result of Global Service Discovery.

<a name="datastructures_gsdqueryform" />

__GSDQueryForm__ is the input

```json
{
  "requestedService": {
	"serviceDefinitionRequirement": "string",
    "interfaceRequirements": [
      "string"
    ],
	"securityRequirements": [
      "NOT_SECURE"
    ],
	"versionRequirement": 0,
    "maxVersionRequirement": 0,
    "minVersionRequirement": 0,
    "pingProviders": true,
	"metadataRequirements": {
      "additionalProp1": "string",
      "additionalProp2": "string",
      "additionalProp3": "string"
    }
  },
  "preferredClouds": [
    {
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
    }
  ]
}
```

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `requestedService` | Object describes the requested service | yes |
| `serviceDefinitionRequirement` | Service Definition | yes |
| `interfaceRequirements` | List of interfaces | no |
| `securityRequirements` | List of required security levels | no |
| `versionRequirement` | Version of the Service | no |
| `maxVersionRequirement` | Maximum version of the Service | no |
| `minVersionRequirement` | Minimum version of the Service | no |
| `pingProviders` | Whether or not the providers should be pinged | no |
| `metadataRequirements` | Metadata | no |
| `preferredClouds` | List of preferred clouds | no |

<a name="datastructures_gsdqueryresult" />

__GSDQueryResult__ is the output

```json
{
  "results": [
    {
      "providerCloud": {
	    "id": 0,
		"name": "string",
		"operator": "string",
        "authenticationInfo": "string",        
        "neighbor": true,        
        "ownCloud": true,
        "secure": true,
		"createdAt": "string",
        "updatedAt": "string"
      },
	  "requiredServiceDefinition": "string",
	  "availableInterfaces": [
        "string"
      ],
      "serviceMetadata": {
        "additionalProp1": "string",
        "additionalProp2": "string",
        "additionalProp3": "string"
      },
	   "numOfProviders": 0
    }
  ],
  "unsuccessfulRequests": 0
}
```

| Field | Description |
| ----- | ----------- |
| `results` | List of result objects |
| `providerCloud` | Cloud where the result coming from |
| `requiredServiceDefinition` | Service Definition |
| `availableInterfaces` | List of available interfaces |
| `serviceMetadata` | Metadata |
| `numOfProviders` | Number of providers serving the service within the cloud |
| `unsuccessfulRequests` | Number of clouds not responded |

<a name="gatekeeper_endpoints_post_init_icn" />

### Init ICN 
```
POST /gatekeeper/init_icn
```

Returns the result of Inter-Cloud Negotiation.

<a name="datastructures_icnrequestform" />

__ICNRequestForm__ is the input

```json
{
  "targetCloudId": 0,
  "requestedService": {
    "serviceDefinitionRequirement": "string",
    "interfaceRequirements": [
      "string"
    ],
    "securityRequirements": [
      "NOT_SECURE"
    ],
	"versionRequirement": 0,
    "maxVersionRequirement": 0,
    "minVersionRequirement": 0,
    "pingProviders": true,	
	"metadataRequirements": {
      "additionalProp1": "string",
      "additionalProp2": "string",
      "additionalProp3": "string"
    }
  },
  "preferredSystems": [
    {
      "systemName": "string",
	  "address": "string",
	  "port": 0,
      "authenticationInfo": "string"
    }
  ],
  "requesterSystem": {
	"systemName": "string",
    "address": "string",
	"port": 0,
    "authenticationInfo": "string"
  },
  "negotiationFlags": {
    "additionalProp1": true,
    "additionalProp2": true,
    "additionalProp3": true
  }
}
```

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `targetCloudId` | Local ID of the target cloud | yes |
| `requestedService` | Object describes the requested service | yes |
| `serviceDefinitionRequirement` | Service Definition | yes |
| `interfaceRequirements` | List of interfaces | no |
| `securityRequirements` | List of required security levels | no |
| `versionRequirement` | Version of the Service | no |
| `maxVersionRequirement` | Maximum version of the Service | no |
| `minVersionRequirement` | Minimum version of the Service | no |
| `pingProviders` | Whether or not the providers should be pinged | no |
| `metadataRequirements` | Metadata | no |
| `preferredSystems` | List of perferred systems | no |
| `requesterSystem` | Requester Cloud details (Own cloud) | yes |
| `negotiationFlags` | Orchestration flags | no |

<a name="datastructures_icnresult" />

__ICNResult__ is the output

```json
{
  "response": [
    {
      "service": {
	    "id": 0,       
        "serviceDefinition": "string",
		"createdAt": "string", 
        "updatedAt": "string"
      },
	  "serviceUri": "string",
	  "provider": {
	    "id": 0,
		"systemName": "string",
        "address": "string",
		"port": 0,
        "authenticationInfo": "string",
        "createdAt": "string",        
        "updatedAt": "string"
      },
	  "interfaces": [
        {
          "id": 0,
          "interfaceName": "string",
		  "createdAt": "string",
          "updatedAt": "string"
        }
      ],      
      "secure": "NOT_SECURE",     
      "version": 0,
	  "metadata": {
        "additionalProp1": "string",
        "additionalProp2": "string",
        "additionalProp3": "string"
      },
	  "authorizationTokens": {
        "additionalProp1": "string",
        "additionalProp2": "string",
        "additionalProp3": "string"
      },
      "warnings": [
        "FROM_OTHER_CLOUD"
      ]
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `results` | List of result objects |
| `service` | Required service |
| `serviceUri` | URI of the service |
| `provider` | Provider details |
| `interfaces` | List of available interfaces |
| `secure` | Level of security |
| `version` | Version number |
| `metadata` | Service metadata |
| `authorizationTokens` | Authorization Tokens per interfaces |
| `warnings` | Warnings |

<a name="gatekeeper_endpoints_get_all_cloud" />

### Get all Cloud entries 
```
GET /gatekeeper/mgmgt/clouds
```

Returns Cloud entries by the given paging parameters. If `page` and `item_per_page` are
not defined, no paging is involved.             

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `page` | zero based page index | no |
| `item_per_page` | maximum number of items returned | no |
| `sort_field` | sorts by the given column | no |
| `direction` | direction of sorting | no |

> **Note:** Default value for `sort_field` is `id`. All possible values are: 
> * `id`
> * `createdAt`
> * `updatedAt`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC` 


<a name="datastructures_cloudwithrelayslistresponse" />

__CloudWithRelaysListRespone__ is the output.

```json
{
  "count": 0,
  "data": [
    {
	  "id": 0,
      "name": "string",
	  "operator": "string",
      "neighbor": true,      
      "ownCloud": true,
      "secure": true,
      "authenticationInfo": "string",
      "createdAt": "string",
	  "updatedAt": "string",
      "gatekeeperRelays": [
        {
          "id": 0,
		  "address": "string",
          "port": 0,		            
          "exclusive": true,
          "secure": true,
          "type": "GATEKEEPER_RELAY",
		  "createdAt": "string",
          "updatedAt": "string"
        }
      ],
      "gatewayRelays": [
        {
          "id": 0,
		  "address": "string",
          "port": 0,		            
          "exclusive": true,
          "secure": true,
          "type": "GATEWAY_RELAY",
		  "createdAt": "string",
          "updatedAt": "string"
        }
      ]
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `count` | Number of record found |
| `data` | Array of data |
| `name` | Name of the cloud |
| `operator` | Operator of the cloud |
| `neighbor` | Whether or not it is a neighbor Cloud |
| `ownCloud` | Whether or not it is the own Cloud |
| `secure` | Whether or not it is a secured Cloud/Relay |
| `authenticationInfo` | Base64 encoded public key of the Cloud |
| `gatekeeperRelays` | List of Relays uesd by Gatekeeper |
| `gatewayRelays` | List of Relays uesd by Gateway |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_get_cloud_by_id" />

### Get Cloud by ID 
```
GET /gatekeeper/mgmgt/clouds/{id}
```

Returns the Cloud Entry specified by the ID path parameter.

<a name="datastructures_cloudwithrelaysresponse" />

__CloudWithRelaysResponse__ is the output.

```json
{
  "id": 0,
  "name": "string",
  "operator": "string",
  "neighbor": true,      
  "ownCloud": true,
  "secure": true,
  "authenticationInfo": "string",
  "createdAt": "string",
  "updatedAt": "string",
  "gatekeeperRelays": [
      {
       "id": 0,
       "address": "string",
       "port": 0,		            
       "exclusive": true,
       "secure": true,
       "type": "GATEKEEPER_RELAY",
       "createdAt": "string",
       "updatedAt": "string"
      }
    ],
    "gatewayRelays": [
      {
        "id": 0,
	"address": "string",
        "port": 0,		            
        "exclusive": true,
        "secure": true,
        "type": "GATEWAY_RELAY",
        "createdAt": "string",
        "updatedAt": "string"
      }
    ]
}
```

| Field | Description |
| ----- | ----------- |
| `name` | Name of the cloud |
| `operator` | Operator of the cloud |
| `neighbor` | Whether or not it is a neighbor Cloud |
| `ownCloud` | Whether or not it is the own Cloud |
| `secure` | Whether or not it is a secured Cloud/Relay |
| `authenticationInfo` | Base64 encoded public key of the Cloud |
| `gatekeeperRelays` | List of Relays used by Gatekeeper |
| `gatewayRelays` | List of Relays used by Gateway |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_register_clouds" />

### Register Clouds 
```
POST /gatekeeper/mgmgt/clouds
```

Returns created Cloud entries.

<a name="datastructures_cloudrequestlist" />

__CloudRequest__ list is the input.

```json
[
  {   
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
  }
]
```

| Field | Description |
| ----- | ----------- |
| `name` | Name of the cloud |
| `operator` | Operator of the cloud |
| `neighbor` | Whether or not it is a neighbor Cloud |
| `secure` | Whether or not it is a secured Cloud |
| `authenticationInfo` | Base64 encoded public key of the Cloud |
| `gatekeeperRelayIds` | List of Relay IDs used by Gatekeeper |
| `gatewayRelayIds` | List of Relay IDs used by Gateway |

<a name="datastructures_cloudwithrelayslistresponse2" />

__CloudWithRelaysListResponse__ is the output.

```json
{
  "count": 0,
  "data": [
    {
      "id": 0,
      "name": "string",
      "operator": "string",
      "neighbor": true,      
      "ownCloud": true,
      "secure": true,
      "authenticationInfo": "string",
      "createdAt": "string",
      "updatedAt": "string",
      "gatekeeperRelays": [
        {
          "id": 0,
	  "address": "string",
          "port": 0,		            
          "exclusive": true,
          "secure": true,
          "type": "GATEKEEPER_RELAY",
	  "createdAt": "string",
          "updatedAt": "string"
        }
      ],
      "gatewayRelays": [
        {
          "id": 0,
	  "address": "string",
          "port": 0,		            
          "exclusive": true,
          "secure": true,
          "type": "GATEWAY_RELAY",
          "createdAt": "string",
          "updatedAt": "string"
        }
      ]
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `count` | Number of record found |
| `data` | Array of data |
| `name` | Name of the cloud |
| `operator` | Operator of the cloud |
| `neighbor` | Whether or not it is a neighbor Cloud |
| `ownCloud` | Whether or not it is the own Cloud |
| `secure` | Whether or not it is a secured Cloud/Relay |
| `authenticationInfo` | Base64 encoded public key of the Cloud |
| `gatekeeperRelays` | List of Relays uesd by Gatekeeper |
| `gatewayRelays` | List of Relays uesd by Gateway |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_update_cloud" />

### Update Cloud
```
PUT /gatekeeper/mgmgt/clouds/{id}
```

Returns updated Cloud entry specified by the ID path parameter.

<a name="datastructures_cloudrequest" />

__CloudRequest__ is the input.

```json
{
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
}
```

| Field | Description |
| ----- | ----------- |
| `name` | Name of the cloud |
| `operator` | Operator of the cloud |
| `neighbor` | Whether or not it is a neighbor Cloud |
| `secure` | Whether or not it is a secured Cloud |
| `authenticationInfo` | Base64 encoded public key of the Cloud |
| `gatekeeperRelayIds` | List of Relay IDs used by Gatekeeper |
| `gatewayRelayIds` | List of Relay IDs used by Gateway |

<a name="datastructures_cloudwithrelaysresponse2" />

__CloudWithRelaysResponse__ is the output.

```json
{
  "id": 0,
  "name": "string",
  "operator": "string",
  "neighbor": true,      
  "ownCloud": true,
  "secure": true,
  "authenticationInfo": "string",
  "createdAt": "string",
  "updatedAt": "string",
  "gatekeeperRelays": [
      {
       "id": 0,
       "address": "string",
       "port": 0,		            
       "exclusive": true,
       "secure": true,
       "type": "GATEKEEPER_RELAY",
       "createdAt": "string",
       "updatedAt": "string"
      }
    ],
    "gatewayRelays": [
      {
        "id": 0,
	"address": "string",
        "port": 0,		            
        "exclusive": true,
        "secure": true,
        "type": "GATEWAY_RELAY",
        "createdAt": "string",
        "updatedAt": "string"
      }
    ]
}
```

| Field | Description |
| ----- | ----------- |
| `name` | Name of the cloud |
| `operator` | Operator of the cloud |
| `neighbor` | Whether or not it is a neighbor Cloud |
| `ownCloud` | Whether or not it is the own Cloud |
| `secure` | Whether or not it is a secured Cloud/Relay |
| `authenticationInfo` | Base64 encoded public key of the Cloud |
| `gatekeeperRelays` | List of Relays uesd by Gatekeeper |
| `gatewayRelays` | List of Relays uesd by Gateway |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_assign_relays_to_cloud" />

### Assign Relays to Cloud
```
POST /gatekeeper/mgmgt/clouds/assign
```

Returns updated Cloud entry.

<a name="datastructures_cloudrelaysassignmentrequest" />

__CloudRelaysAssignmentRequest__ is the input.

```json
{
  "cloudId": 0,
  "gatekeeperRelayIds": [
    0
  ],
  "gatewayRelayIds": [
    0
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `cloudId` |ID of the cloud |
| `gatekeeperRelayIds` | List of Relay IDs used by Gatekeeper |
| `gatewayRelayIds` | List of Relay IDs used by Gateway |

<a name="datastructures_cloudwithrelaysresponse3" />

__CloudWithRelaysResponse__ is the output.

```json
{
  "id": 0,
  "name": "string",
  "operator": "string",
  "neighbor": true,      
  "ownCloud": true,
  "secure": true,
  "authenticationInfo": "string",
  "createdAt": "string",
  "updatedAt": "string",
  "gatekeeperRelays": [
      {
       "id": 0,
       "address": "string",
       "port": 0,		            
       "exclusive": true,
       "secure": true,
       "type": "GATEKEEPER_RELAY",
       "createdAt": "string",
       "updatedAt": "string"
      }
    ],
    "gatewayRelays": [
      {
        "id": 0,
	"address": "string",
        "port": 0,		            
        "exclusive": true,
        "secure": true,
        "type": "GATEWAY_RELAY",
        "createdAt": "string",
        "updatedAt": "string"
      }
    ]
}
```

| Field | Description |
| ----- | ----------- |
| `name` | Name of the cloud |
| `operator` | Operator of the cloud |
| `neighbor` | Whether or not it is a neighbor Cloud |
| `ownCloud` | Whether or not it is the own Cloud |
| `secure` | Whether or not it is a secured Cloud/Relay |
| `authenticationInfo` | Base64 encoded public key of the Cloud |
| `gatekeeperRelays` | List of Relays used by Gatekeeper |
| `gatewayRelays` | List of Relays used by Gateway |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_delete_cloud" />

### Delete Cloud
```
DELETE /gatekeeper/mgmgt/clouds/{id}
```

Remove requested Cloud entry

<a name="gatekeeper_endpoints_get_all_relay" />

### Get all Relay entries 
```
GET /gatekeeper/mgmgt/relays
```

Returns Relay entries by the given paging parameters. If `page` and `item_per_page` are
not defined, no paging is involved.             

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `page` | zero based page index | no |
| `item_per_page` | maximum number of items returned | no |
| `sort_field` | sorts by the given column | no |
| `direction` | direction of sorting | no |

> **Note:** Default value for `sort_field` is `id`. All possible values are: 
> * `id`
> * `createdAt`
> * `updatedAt`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC` 


<a name="datastructures_relaylistresponse" />

__RelayListResponse__ is the output.

```json
{
  "count": 0,
  "data": [
    {      
      "id": 0,
      "address": "string",
      "port": 0,
      "exclusive": true,
      "secure": true,
      "type": "GATEKEEPER_RELAY",
      "createdAt": "string",
      "updatedAt": "string"
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `count` | Number of record found |
| `data` | Array of data |
| `id` | ID of the Relay |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `secure` | Whether or not it is a secured Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_get_relay_by_id" />

### Get Relay by ID 
```
GET /gatekeeper/mgmgt/relays/{id}
```

Returns the Relay Entry specified by the ID path parameter.

<a name="datastructures_relayresponse" />

__RelayResponse__ is the output.

```json
{      
  "id": 0,
  "address": "string",
  "port": 0,
  "exclusive": true,
  "secure": true,
  "type": "GATEKEEPER_RELAY",
  "createdAt": "string",
  "updatedAt": "string"
}
```

| Field | Description |
| ----- | ----------- |
| `id` | ID of the Relay |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `secure` | Whether or not it is a secured Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_get_relay_by_address_and_port" />

### Get Relay by Address and Port 
```
GET /gatekeeper/mgmgt/relays/{address}/{port}
```

Returns the Relay Entry specified by the address and port path parameter.

<a name="datastructures_relayresponse2" />

__RelayResponse__ is the output.

```json
{      
  "id": 0,
  "address": "string",
  "port": 0,
  "exclusive": true,
  "secure": true,
  "type": "GATEKEEPER_RELAY",
  "createdAt": "string",
  "updatedAt": "string"
}
```

| Field | Description |
| ----- | ----------- |
| `id` | ID of the Relay |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `secure` | Whether or not it is a secured Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_register_relays" />

### Register Relays
```
POST /gatekeeper/mgmgt/relays
```
<a name="datastructures_relayrequestlist" />

__RelayRequest__ list is the input

```json
[
 {      
  "address": "string",
  "port": 0,
  "exclusive": true,
  "secure": true,
  "type": "GATEKEEPER_RELAY",
  "createdAt": "string",
  "updatedAt": "string"
 }
]
```

| Field | Description |
| ----- | ----------- |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `secure` | Whether or not it is a secured Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="datastructures_relaylistresponse2" />

__RelayListResponse__ is the output.

```json
{
  "count": 0,
  "data": [
    {      
      "id": 0,
      "address": "string",
      "port": 0,
      "exclusive": true,
      "secure": true,
      "type": "GATEKEEPER_RELAY",
      "createdAt": "string",
      "updatedAt": "string"
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `count` | Number of record found |
| `data` | Array of data |
| `id` | ID of the Relay |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `secure` | Whether or not it is a secured Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_update_relay" />

### Update Relay
```
PUT /gatekeeper/mgmgt/relays/{id}
```

Returns updated Relay entry specified by the ID path parameter.

<a name="datastructures_relayrequest" />

__RelayRequest__ is the input.

```json
{      
  "address": "string",
  "port": 0,
  "exclusive": true,
  "secure": true,
  "type": "GATEKEEPER_RELAY",
  "createdAt": "string",
  "updatedAt": "string"
}
```

| Field | Description |
| ----- | ----------- |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `secure` | Whether or not it is a secured Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="datastructures_relayresponse3" />

__RelayResponse__ is the output.

```json
{      
  "id": 0,
  "address": "string",
  "port": 0,
  "exclusive": true,
  "secure": true,
  "type": "GATEKEEPER_RELAY",
  "createdAt": "string",
  "updatedAt": "string"
}
```

| Field | Description |
| ----- | ----------- |
| `id` | ID of the Relay |
| `address` | Host of the Relay |
| `port` | Port of the Relay |
| `exclusive` | Whether or not is is a not public Relay |
| `secure` | Whether or not it is a secured Relay |
| `type` | Type of the Relay (Possible values: 'GENERAL_RELAY, 'GATEKEEPER_RELAY', 'GATEWAY_RELAY') |

<a name="gatekeeper_endpoints_delete_relay" />

### Delete Relay
```
DELETE /gatekeeper/mgmgt/relays/{id}
```

Remove requested Relay entry.
