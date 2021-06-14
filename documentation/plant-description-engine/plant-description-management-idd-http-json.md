# Plant Description Management HTTP(S)/JSON - Interface Design Description

## Abstract
This document describes the HTTP/{TLS}/JSON variant of a service that can be used to manage Plant Descriptions in the [Plant Description Engine] core system.

## Overview
This document describes the HTTP/{TLS}/JSON variant of the
[Plant Description Management] ([PDM]) service, which allows for arbitrary
Arrowhead Framework systems to manage Plant Descriptions in the [Plant Description Engine] ([PDE]) core system.
Readers of this document are assumed to be familiar with the [PDM] service.
For more information about the service, please refer to the service description document [PDM].
The rest of this document describes how to realize the [PDM] service using [HTTP], optionally with [TLS], and [JSON], both in terms of its [interfaces](#service-interfaces) and its [information model](#information-model).

## Service Interfaces
This section describes the interfaces that must be exposed by [PDM] services. In particular, the below
subsection first names the HTTP method and path used to call the interface, after which it names an abstract
interface from the [PDM] service description document, output type, as well as errors that can be thrown. The
interface is expected to respond with HTTP status code 200 OK for all successful calls.

### POST /{baseURI}/mgmt/pd
 - __Interface: AddPlantDescription__
 - __Input: [PlantDescription](#plantdescription)__
 - __Output: [PlantDescriptionEntry](#plantdescriptionentry)__

Called to add a Plant Description to the PDE.

Example of valid invocation:

```json
POST /pde/mgmt/pd HTTP/1.1
Accept: application/json
Content-Length: 2342
Content-Type: application/json

{
	"plantDescription": "ArrowHead core",
	"systems": [
		{
			"systemName": "serviceregistry",
			"systemId": "service_registry",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery"},
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "authorization",
			"systemId": "authorization",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation"},
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra"},
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "orchestration",
			"systemId": "orchestration",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation", "consumer": true },
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "orchestration-service"},
				{ "portName": "orchestrationStoreManagement", "serviceDefinition": "orchestration-store-management"},
				{ "portName": "orchestrationPush", "serviceDefinition": "orchestration-push", "consumer": true },
				{ "portName": "OrchestrationCapabilities", "serviceDefinition": "orchestration-capabilities", "consumer": true },
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		}
	],
	"connections": [
		{ "consumer": { "systemId": "authorization", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "tokenGeneration" },
		  "producer": { "systemId": "authorization", "portName": "tokenGeneration" }},
		{ "consumer": { "systemId": "orchestration", "portName": "authorizationControl" },
		  "producer": { "systemId": "authorization", "portName": "authorizationControl" }}
	]
}
```

Example of valid response:

```json
HTTP/1.1 200 OK
Content-Length: 2394
Content-Type: application/json

{
	"id": 1,
	"plantDescription": "ArrowHead core",
	"active": false,
	"systems": [
		{
			"systemName": "serviceregistry",
			"systemId": "service_registry",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery"}
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "authorization",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation"},
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra"}
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "orchestration",
			"systemId": "orchestration",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation", "consumer": true },
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "orchestration-service"},
				{ "portName": "orchestrationStoreManagement", "serviceDefinition": "orchestration-store-management"},
				{ "portName": "orchestrationPush", "serviceDefinition": "orchestration-push", "consumer": true },
				{ "portName": "OrchestrationCapabilities", "serviceDefinition": "orchestration-capabilities", "consumer": true }
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		}
	],
	"connections": [
		{ "consumer": { "systemId": "authorization", "portName": "service_registry" },
			"producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "service_registry" },
			"producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "tokenGeneration" },
			"producer": { "systemId": "authorization", "portName": "tokenGeneration" }},
		{ "consumer": { "systemId": "orchestration", "portName": "authorizationControl" },
			"producer": { "systemId": "authorization", "portName": "authorizationControl" }}
	],
	"createdAt": "2020-03-13T16:54:00.511Z",
	"updatedAt": "2020-03-13T16:54:00.511Z"
}
```

### DELETE {baseURI}/mgmt/pd/{id}
 - __Interface: DeletePlantDescription__

Called to delete the __[PlantDescriptionEntry](#plantdescriptionentry)__ specified by the `id` path parameter.

Example of valid invocation:

```json
DELETE /pde/mgmt/pd/1 HTTP/1.1
````


Example of valid response:

```json
HTTP/1.1 200 OK
Content-Length: 0
```

### GET {baseURI}/mgmt/pd
 - __Interface:	GetAllPlantDescriptions__
 - __Output: [PlantDescriptionEntryList](#plantdescriptionentrylist)__

Called to acquire a list of Plant Description Entries present in the PDE.

Returns a list of Plant Description Entries. If `page` and `item_per_page` are not defined, returns
all records.

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `page` | zero based page index | no |
| `item_per_page` | maximum number of items returned | no |
| `sort_field` | sorts by the given column | no |
| `direction` | direction of sorting | no |
| `active` | filter out active/inactive entries | no |

> **Note:** Default value for `sort_field` is `id`. All possible values are:
> * `id`
> * `createdAt`
> * `updatedAt`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC`

> **Note:**  Possible values for `active` are `true` and `false`.

Example of valid invocation:

```json
GET /pde/mgmt/pd HTTP/1.1
Accept: application/json
```

Example of valid response:

```json
HTTP/1.1 200 OK
Content-Length: 2554
Content-Type: application/json

{
  "count": 1,
  "data": [
	{
		"id": 1,
		"plantDescription": "ArrowHead core",
		"active": false,
		"systems": [
			{
				"systemName": "serviceregistry",
				"systemId": "service_registry",
				"ports": [
					{ "portName": "service_registry", "serviceDefinition": "service-discovery"}
					{ "portName": "monitorable", "serviceDefinition": "monitorable"}
				]
			},
			{
				"systemName": "authorization",
				"systemId": "authorization",
				"ports": [
					{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
					{ "portName": "tokenGeneration", "serviceDefinition": "token-generation"},
					{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra"}
					{ "portName": "monitorable", "serviceDefinition": "monitorable"}
				]
			},
			{
				"systemName": "orchestration",
				"systemId": "orchestration",
				"ports": [
					{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
					{ "portName": "tokenGeneration", "serviceDefinition": "token-generation", "consumer": true },
					{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra", "consumer": true },
					{ "portName": "orchestrationService", "serviceDefinition": "orchestration-service"},
					{ "portName": "orchestrationStoreManagement", "serviceDefinition": "orchestration-store-management"},
					{ "portName": "orchestrationPush", "serviceDefinition": "orchestration-push", "consumer": true },
					{ "portName": "OrchestrationCapabilities", "serviceDefinition": "orchestration-capabilities", "consumer": true }
					{ "portName": "monitorable", "serviceDefinition": "monitorable"}
				]
			},
		],
		"connections": [
			{ "consumer": { "systemId": "authorization", "portName": "service_registry" },
			  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
			{ "consumer": { "systemId": "orchestration", "portName": "service_registry" },
			  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
			{ "consumer": { "systemId": "orchestration", "portName": "tokenGeneration" },
			  "producer": { "systemId": "authorization", "portName": "tokenGeneration" }},
			{ "consumer": { "systemId": "orchestration", "portName": "authorizationControl" },
			  "producer": { "systemId": "authorization", "portName": "authorizationControl" }}
		],
      "createdAt": "2020-03-13T16:54:00.511Z",
      "updatedAt": "2020-03-13T16:54:00.511Z"
	}
  ]
}
```

### Get {baseURI}/mgmt/pd/{id}
 - __Interface: GetPlantDescription__
 - __Output: [PlantDescriptionEntry](#plantdescriptionentry)__

Called to acquire the __[PlantDescriptionEntry](#plantdescriptionentry)__ specified by the `id` path parameter.

Example of valid invocation:

```json
GET /pde/mgmt/pd/1 HTTP/1.1
Accept: application/json
````

Example of valid response:

```json
HTTP/1.1 200 OK
Content-Length: 2460
Content-Type: application/json

{
	"id": 1,
	"plantDescription": "ArrowHead core",
	"active": false,
	"systems": [
		{
			"systemName": "serviceregistry",
			"systemId": "service_registry",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery"}
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "authorization",
			"systemNId": "authorization",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation"},
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra"}
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "orchestration",
			"systemId": "orchestration",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation", "consumer": true },
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "orchestration-service"},
				{ "portName": "orchestrationStoreManagement", "serviceDefinition": "orchestration-store-management"},
				{ "portName": "orchestrationPush", "serviceDefinition": "orchestration-push", "consumer": true },
				{ "portName": "OrchestrationCapabilities", "serviceDefinition": "orchestration-capabilities", "consumer": true }
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		}
	],
	"connections": [
		{ "consumer": { "systemId": "authorization", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "tokenGeneration" },
		  "producer": { "systemId": "authorization", "portName": "tokenGeneration" }},
		{ "consumer": { "systemId": "orchestration", "portName": "authorizationControl" },
		  "producer": { "systemId": "authorization", "portName": "authorizationControl" }}
	],
  "createdAt": "2020-03-13T16:54:00.511Z",
  "updatedAt": "2020-03-13T16:54:00.511Z"
}
```


### PUT /{baseURI}/mgmt/pd{id}
 - __Interface: ReplacePlantDescription__
 - __Input: [PlantDescription](#plantdescription)__
 - __Output: [PlantDescriptionEntry](#plantdescriptionentry)__

Called to replace the Plant Description Entry specified by the `id` path parameter with the Plant Description in the `PlantDescription` parameter.

Example of valid invocation:

```json
PUT /pde/mgmt/pd/1 HTTP/1.1
Accept: application/json
Content-Length: 2342
Content-Type: application/json

{
	"plantDescription": "ArrowHead core",
	"systems": [
		{
			"systemName": "serviceregistry",
			"systemId": "service_registry",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery"},
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "authorization",
			"systemId": "authorization",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation"},
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra"},
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "orchestration",
			"systemId": "orchestration",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation", "consumer": true },
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "orchestration-service"},
				{ "portName": "orchestrationStoreManagement", "serviceDefinition": "orchestration-store-management"},
				{ "portName": "orchestrationPush", "serviceDefinition": "orchestration-push", "consumer": true },
				{ "portName": "OrchestrationCapabilities", "serviceDefinition": "orchestration-capabilities", "consumer": true }
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		}
	],
	"connections": [
		{ "consumer": { "systemId": "authorization", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "tokenGeneration" },
		  "producer": { "systemId": "authorization", "portName": "tokenGeneration" }},
		{ "consumer": { "systemId": "orchestration", "portName": "authorizationControl" },
		  "producer": { "systemId": "authorization", "portName": "authorizationControl" }}
	]
}
```

Example of valid response:

```json
HTTP/1.1 200 OK
Content-Length: 2460
Content-Type: application/json

{
	"id": 1,
	"plantDescription": "ArrowHead core",
	"active": false,
	"systems": [
		{
			"systemName": "serviceregistry",
			"systemId": "service_registry",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery"}
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "authorization",
			"systemId": "authorization",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation"},
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra"}
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "orchestration",
			"systemId": "orchestration",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation", "consumer": true },
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "orchestration-service"},
				{ "portName": "orchestrationStoreManagement", "serviceDefinition": "orchestration-store-management"},
				{ "portName": "orchestrationPush", "serviceDefinition": "orchestration-push", "consumer": true },
				{ "portName": "OrchestrationCapabilities", "serviceDefinition": "orchestration-capabilities", "consumer": true },
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		}
	],
	"connections": [
		{ "consumer": { "systemId": "authorization", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "tokenGeneration" },
		  "producer": { "systemId": "authorization", "portName": "tokenGeneration" }},
		{ "consumer": { "systemId": "orchestration", "portName": "authorizationControl" },
		  "producer": { "systemId": "authorization", "portName": "authorizationControl" }}
	],
  "createdAt": "2020-03-13T16:54:00.511Z",
  "updatedAt": "2020-03-13T17:44:00.511Z"
}
```

### PATCH /{baseURI}/mgmt/pd/{id}
 - __Interface: UpdatePlantDescription__
 - __Input: [PlantDescriptionUpdate](#plantdescriptionupdate)__
 - __Output: [PlantDescriptionEntry](#plantdescriptionentry)__

Called to update the Plant Description Entry specified by the `id` path parameter with the information in the `PlantDescriptionUpdate` parameter.

Example of valid invocation:

```json
PATCH /pde/mgmt/pd/1 HTTP/1.1
Accept: application/json
Content-Length: 23
Content-Type: application/json

{
	"active": true
}
```

Example of valid response:

```json
HTTP/1.1 200 OK
Content-Length: 2459
Content-Type: application/json

{
	"id": 1,
	"plantDescription": "ArrowHead core",
	"active": true,
	"systems": [
		{
			"systemName": "serviceregistry",
			"systemId": "service_registry",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery"},
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "authorization",
			"systemId": "authorization",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation"},
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra"}
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		},
		{
			"systemName": "orchestration",
			"systemId": "orchestration",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "service-discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "token-generation", "consumer": true },
				{ "portName": "authorizationControl", "serviceDefinition": "authorization-control-intra", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "orchestration-service"},
				{ "portName": "orchestrationStoreManagement", "serviceDefinition": "orchestration-store-management"},
				{ "portName": "orchestrationPush", "serviceDefinition": "orchestration-push", "consumer": true },
				{ "portName": "OrchestrationCapabilities", "serviceDefinition": "orchestration-capabilities", "consumer": true }
				{ "portName": "monitorable", "serviceDefinition": "monitorable"}
			]
		}
	],
	"connections": [
		{ "consumer": { "systemId": "authorization", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "service_registry" },
		  "producer": { "systemId": "service_registry", "portName": "service_registry" }},
		{ "consumer": { "systemId": "orchestration", "portName": "tokenGeneration" },
		  "producer": { "systemId": "authorization", "portName": "tokenGeneration" }},
		{ "consumer": { "systemId": "orchestration", "portName": "authorizationControl" },
		  "producer": { "systemId": "authorization", "portName": "authorizationControl" }}
	],
  "createdAt": "2020-03-13T16:54:00.511Z",
  "updatedAt": "2020-03-13T18:23:00.511Z"
}
```

## Information model
Here, all data objects that can be part of PDM service calls are listed in alphabetic order.
As a complement to the explicitly defined types in this section, there is also a list of implicit [primitive](#primitives) types.

### Connection
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `consumer` | [SystemPort](#systemport) | The consumer end SystemPort of the connection | `true` | |
| `producer` | [SystemPort](#systemport) | The producer end SystemPort of the connection | `true` | |
| `priority` | Integer | Priority of the connection | `false` | `null` |

### PlantDescription
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `plantDescription` | String | Plant description name | `true` | |
| `active` | Boolean | Is this the active plant description | `false` | `false` |
| `include` | Array\<Integer>| Array with Ids of other PDs that are included in this PD | `false` | [] |
| `systems` | Array\<[System](#system)> | Array with systems expected to be present in the plant | `true` ||
| `connections` | Array\<[Connection](#connection)> | Array with connection that should be populated into the Orchestrator | `true` ||

### PlantDescriptionEntry
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `id` | Integer | Id of the entry | `true` ||
| `plantDescription` | String | Plant description name| `true` ||
| `active` | Boolean | Is this the active plant description | `true` ||
| `include` | Array\<Integer>| Array with Ids of other PDs that are included in this PD | `true` | |
| `systems` | Array\<[System](#system)> | Array with systems expected to be present in the plant | `true` ||
| `connections` | Array\<[Connection](#connection)> | Array with connection that should be populated into the Orchestrator | `true` ||
| `createdAt` | [DateTime](#alias-datetime--string) | Creation date of the entry | `true` ||
| `updatedAt` | [DateTime](#alias-datetime--string) | When the entry was last updated | `true` ||

### PlantDescriptionEntryList
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `count` | Integer | Number of records found | `true` ||
| `data` | Array\<[PlantDescriptionEntry](#plantdescriptionentry)> | Array with Plant Description Entries | `true` ||

### PlantDescriptionUpdate
JSON object with the following fields:

Currently only the following values can be updated. If a field is not present the current value will remain unchanged.

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `plantDescription` | String | Plant description name | `false` ||
| `active` | Boolean | Is this the active plant description | `false` ||
| `include` | Array\<Integer>| Array with Ids of other PDs that are included in this PD | `false` | |
| `systems` | Array\<[System](#system)> | Array with systems expected to be present in the plant | `false` ||
| `connections` | Array\<[Connection](#connection)> | Array with connection that should be populated into the Orchestrator | `false` ||

### Port
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `portName` | String | Identity of the port | `true` | |
| `serviceDefinition` | String | Service definition identity | `true` | |
| `serviceInterface` | String | Service interface | `false` | `null` |
| `consumer` | Boolean | Is the port a consumer port | `false` | `false` |
| `metadata` | Object\<String> | Metadata - key-value pairs | `false` | null |

### System
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `systemId` | String | Identity of the system within the PDE | `true` | |
| `systemName` | String | Name of the system | `false` | null |
| `metadata` | Object\<String> | Metadata - key-value pairs | `false` | null |
| `ports` | Array\<[Port](#port)> | Array with service ports exposed by the system | `true` ||

### SystemPort
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `systemId` | String | Identity of the system within the PDE | `true` | |
| `portName` | String | Identity of the port | `true` | |


### Primitives
As all messages are encoded using the [JSON] format,
the following primitive constructs, part of that standard, become available.
Note that the official standard is defined in terms of parsing rules, while this list only concerns
syntactic information. Furthermore, the Object and Array types are given optional generic type parameters,
which are used in this document to signify when pair values or elements are expected to conform to certain
types.

| JSON Type | Description |
| --------- | ----------- |
| Value | Any out of Object, Array, String, Integer, Boolean or Null. |
| Object \<A> | An unordered collection of [String: Value] pairs, where each Value conforms to type A. Empty objects are omitted. |
| Array \<A> | An ordered collection of Value elements, where each element conforms to type A. Empty arrays are omitted. |
| String | An arbitrary UTF-8 string. |
| Integer | 32-bit signed two's complement integer, which has a minimum value of -2<sup>31</sup> and a maximum value of 2<sup>31</sup>-1 |
| Boolean | One out of `true` or `false`. |
| Null | Must be null. |

#### alias DateTime = String
Pinpoints a moment in time by providing a formatted string that conforms to the
[RFC 3339] specification, which could be regarded as a simplification of the ISO 8601
standard. Naively, the format could expressed as `YYYY-MM-DDTHH:MM:SS.sssZ`,
where `YYYY` denotes year (4 digits),
`MM` denotes month starting from 01,
`DD` denotes day starting from 01,
`HH` denotes hour in the 24-hour format (00-23),
`MM` denotes minute (00-59),
`SS` denotes second (00-59) and
`sss` denotes second fractions (000-999).
`T` is used as separator between the date and the time,
while `Z` denotes the UTC time zone.
At least three fraction digits should be used, which gives millisecond precision. An example of a valid date/time string is `2019-09-19T15:20:50.521Z`.
Other forms or variants, including the use of other time zones, is adviced against.



[HTTP]:https://doi.org/10.17487/RFC7230
[JSON]:https://doi.org/10.17487/RFC7159
[PDM]:plant-description-management-sd.md
[PDE]:plant-description-engine-sysd.md
[Plant Description Management]:plant-description-management-sd.md
[Plant Description Monitor]:plant-description-monitor-sd.md
[Plant Description Engine]:plant-description-engine-sysd.md
[RFC 3339]:https://doi.org/10.17487/RFC3339
[TLS]:https://doi.org/10.17487/RFC8446
