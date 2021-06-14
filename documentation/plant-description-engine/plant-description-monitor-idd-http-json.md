# Plant Description Monitor HTTP(S)/JSON - Interface Design Description

## Abstract
This document describes the HTTP/{TLS}/JSON variant of a service that can be used to monitor alarms raised by the [Plant Description Engine] core system.

## Overview
This document describes the HTTP/{TLS}/JSON variant of the
[Plant Description Monitor] (PDMon) service, which allows for arbitrary
Arrowhead Framework systems to monitor alarms raised by the [Plant Description Engine] ([PDE]) core system.
Readers of this document are assumed to be familiar with the [PDMon] service.
For more information about the service, please refer to the service description document [PDMon].
The rest of this document describes how to realize the [PDMon] service using [HTTP], optionally with [TLS], and [JSON], both in terms of its [interfaces](#service-interfaces) and its [information model](#information-model).

## Service Interfaces
This section describes the interfaces that must be exposed by [Plant Description Monitor] services. In particular, the below
subsection first names the HTTP method and path used to call the interface, after which it names an abstract
interface from the [Plant Description Monitor] service description document, output type, as well as errors that can be thrown. The
interface is expected to respond with HTTP status code 200 OK for all successful calls.

### GET {baseURI}/monitor/alarm
 - __Interface:	GetAllPdeAlarms__
 - __Output: [PdeAlarmList](#pdealarmlist)__

Called to acquire a list of PDE alarms raised by the PDE.

Returns a list of PDE alarms. If `page` and `item_per_page` are not defined, returns
all records.

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `page` | zero based page index | no |
| `item_per_page` | maximum number of items returned | no |
| `sort_field` | sorts by the given column | no |
| `direction` | direction of sorting | no |
| `systemName` | filter by system name | no |
| `systemId` | filter by system ID | no |
| `acknowledged` | filter by acknowledged column | no |
| `severity` | filter by severity | no |


> **Note:** Default value for `sort_field` is `id`. All possible values are:
> * `id`
> * `raisedAt`
> * `updatedAt`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC`

> **Note:**  Possible values for `acknowledged` are `true` and `false`.

> **Note:**  Possible values for `severity` are:
> * `indeterminate`
> * `critical`
> * `major`
> * `minor`
> * `warning`
> * `cleared`
> * `not_cleared`

Example of valid invocation:
```json
GET /pde/monitor/alarm?severity=major HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Length: 324
Content-Type: application/json

{
	"count": 1,
	"data": [
		{
            "id": 1,
            "systemName": "exampleproducerwithmetadata",
            "systemId": "example-producer",
            "acknowledged": false,
            "severity": "major",
            "description": "System not registered in Service Registry",
            "raisedAt": "2020-03-13T16:54:00.511Z",
            "updatedAt": "2020-03-13T16:54:00.511Z"
        }
	]
}
```

### GET {baseURI}/monitor/alarm/{id}
 - __Interface:	GetPdeAlarm__
 - __Output: [PdeAlarm](#pdealarm)__

Called to acquire the __[PdeAlarm](#pdealarm)__ specified by the `id` path parameter.

Example of valid invocation:
```json
GET /pde/monitor/alarm/1 HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Length: 262
Content-Type: application/json

{
	"id": 1,
	"systemName": "exampleproducerwithmetadata",
	"systemId": "example-producer",
	"acknowledged": false,
	"severity": "major",
	"description": "System not registered in Service Registry",
	"raisedAt": "2020-03-13T16:54:00.511Z",
	"updatedAt": "2020-03-13T16:54:00.511Z"
}
```

### PATCH {baseURI}/monitor/alarm/{id}
 - __Interface:	UpdatePdeAlarm__
 - __Input: [PdeAlarmUpdate](#pdealarmupdate)__
 - __Output: [PdeAlarm](#pdealarm)__

Called to update the PDE Alarm specified by the `id` parameter with the information in the the request body.
The newly updated PDE Alarm is returned.

Example of valid invocation:
```json
GET /pde/monitor/alarm/1 HTTP/1.1
Accept: application/json
Content-Length: 29
Content-Type: application/json

{
	"acknowledged": true
}
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Length: 309
Content-Type: application/json

{
	"id": 1,
	"systemName": "exampleproducerwithmetadata",
	"systemId": "example-producer",
	"acknowledged": true,
	"severity": "major",
	"description": "System not registered in Service Registry",
	"raisedAt": "2020-03-13T16:54:00.511Z",
	"acknowledgedAt": "2020-03-13T17:32:00.531Z",
	"updatedAt": "2020-03-13T17:32:00.531Z"
}
```

### GET {baseURI}/monitor/pd
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
| `active` | filter by active column | no |

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
GET /pde/monitor/pd HTTP/1.1
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
					{ "portName": "service_registry", "serviceDefinition": "Service Discovery"}
					{ "portName": "monitorable", "serviceDefinition": "eu.arrowehead.services.monitorable"}
				]
			},
			{
				"systemName": "authorization",
				"systemId": "authorization",
				"ports": [
					{ "portName": "service_registry", "serviceDefinition": "Service Discovery", "consumer": true },
					{ "portName": "tokenGeneration", "serviceDefinition": "Token Generation"},
					{ "portName": "authorizationControl", "serviceDefinition": "Authorization Control"}
					{ "portName": "monitorable", "serviceDefinition": "eu.arrowehead.services.monitorable"}
				]
			},
			{
				"systemName": "orchestration",
				"systemId": "orchestration",
				"ports": [
					{ "portName": "service_registry", "serviceDefinition": "Service Discovery", "consumer": true },
					{ "portName": "tokenGeneration", "serviceDefinition": "Token Generation", "consumer": true },
					{ "portName": "authorizationControl", "serviceDefinition": "Authorization Control", "consumer": true },
					{ "portName": "orchestrationService", "serviceDefinition": "OrchestrationService"},
					{ "portName": "orchestrationStoreManagement", "serviceDefinition": "OrchestrationStoreManagement"},
					{ "portName": "orchestrationPush", "serviceDefinition": "OrchestrationPush", "consumer": true },
					{ "portName": "orchestrationCapabiliteis", "serviceDefinition": "OrchestrationCapabiliteis", "consumer": true }
					{ "portName": "monitorable", "serviceDefinition": "eu.arrowehead.services.monitorable"}
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

### Get {baseURI}/monitor/pd/{id}
 - __Interface: GetPlantDescription__
 - __Output: [PlantDescriptionEntry](#plantdescriptionentry)__

Called to acquire the __[PlantDescriptionEntry](#plantdescriptionentry)__ specified by the `id` path parameter.

Example of valid invocation:

```json
GET /pde/monitor/pd/1 HTTP/1.1
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
				{ "portName": "service_registry", "serviceDefinition": "Service Discovery"}
				{ "portName": "monitorable", "serviceDefinition": "eu.arrowehead.services.monitorable"}
			]
		},
		{
			"systemName": "authorization",
			"systemId": "authorization",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "Token Generation"},
				{ "portName": "authorizationControl", "serviceDefinition": "Authorization Control"}
				{ "portName": "monitorable", "serviceDefinition": "eu.arrowehead.services.monitorable"}
			]
		},
		{
			"systemName": "orchestration",
			"systemId": "orchestration",
			"ports": [
				{ "portName": "service_registry", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "Token Generation", "consumer": true },
				{ "portName": "authorizationControl", "serviceDefinition": "Authorization Control", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "OrchestrationService"},
				{ "portName": "orchestrationStoreManagement", "serviceDefinition": "OrchestrationStoreManagement"},
				{ "portName": "orchestrationPush", "serviceDefinition": "OrchestrationPush", "consumer": true },
				{ "portName": "orchestrationCapabiliteis", "serviceDefinition": "OrchestrationCapabiliteis", "consumer": true }
				{ "portName": "monitorable", "serviceDefinition": "eu.arrowehead.services.monitorable"}
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


## Information model
Here, all data objects that can be part of PDMon service calls are listed in alphabetic order.
Some of the types are identical to types defined in the [Plant Description Management HTTP(S)/JSON - Interface Design Description] and are not redefined here.
As a complement to the explicitly defined types
in this section, there is also a list of implicit [primitive](#primitives) types.

### PdeAlarm
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `id` | Integer | Id of the alarm | `true` | |
| `systemId` | String | Identity of a system within the PDE, or the string `unknown` | `true` | |
| `systemName` | String | Name of the system | `false` | |
| `acknowledged` | Boolean | Has the alarm been acknowledged by an operator | `true` | |
| `severity` | String | One out of `indeterminate/critical/major/minor/warning/cleared` | `true` | |
| `description` | String | Description of the problem | `true` | |
| `raisedAt` | [DateTime](#alias-datetime--string) | When the alarm was first raised | `true` | |
| `updatedAt` | [DateTime](#alias-datetime--string) | When the alarm was last updated | `true` | |
| `clearedAt` | [DateTime](#alias-datetime--string) | When the alarm was cleared | `false` | |
| `acknowledgedAt` | [DateTime](#alias-datetime--string) | When the alarm was acknowledged | `false` | |


### PdeAlarmList
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `count` | Integer | Number of records found | `true` | |
| `data` | Array\<[PdeAlarm](#pdealarm)> | Array of [PDE Alarms](#pdealarm) | `true` | |

### PdeAlarmUpdate
JSON object with the following fields. Currently only the following values can be updated. If a field is not present the current value will be used.

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `acknowledged` | Boolean | Has the alarm been acknowledged by an operator |`false`||

### PlantDescriptionEntry
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `id` | Integer | Id of the entry | `true` ||
| `plantDescription` | String | Plant description name| `true` ||
| `active` | Boolean | Is this the active plant description | `true` ||
| `systems` | Array\<[SystemEntry](#systementry)> | Array with systems expected to be present in the plant | `true` ||
| `connections` | Array\<[Connection]> | Array with connection that should be populated into the Orchestrator | `true` ||
| `createdAt` | [DateTime](#alias-datetime--string) | Creation date of the entry | `true` ||
| `updatedAt` | [DateTime](#alias-datetime--string) | When the entry was last updated | `true` ||

### PlantDescriptionEntryList
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `count` | Integer | Number of records found | `true` ||
| `data` | Array\<[PlantDescriptionEntry](#plantdescriptionentry)> | Array with Plant Description Entries | `true` ||

### PortEntry
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `portName` | String | Identity of the port | `true` | |
| `serviceDefinition` | String | Service definition identity | `true` | |
| `consumer` | Boolean | Is the port a consumer port | `false` | `false` |
| `metadata` | Object\<String> | Metadata - key-value pairs. Only allowed if `consumer` is true. | `false` | null |
| `systemData` | Object | System specific data - key-value pairs | `false` | Only present if provided by [Monitorable] service with metadata matching that of this port |
| `inventoryId` | String | The system's Id in an Inventory system | `false` | Only present if provided by [Monitorable] service with metadata matching that of this port |
| `inventoryData` | Custom | Inventory specific data - key-value pairs | `false` | Only present if provided by [Inventory] system |

### SystemEntry
JSON object with the following fields:

| Field | Type | Description | Mandatory | Note |
| ----- | ---- | ----------- | --------- | ------------- |
| `systemId` | String | Identity of the system within the PDE | `true` | |
| `systemName` | String | Name of the system | `false` | null |
| `metadata` | Object\<String> | Metadata - key-value pairs | `false` | Only present if specified |
| `ports` | Array\<[PortEntry](#PortEntry)> | Array with service ports exposed by the system | `true` ||
| `systemData` | Object | System specific data - key-value pairs | `false` | Only present if provided by Monitorable |
| `inventoryId` | String | The system's Id in an Inventory system | `false` | Only present if provided by Monitorable |
| `inventoryData` | Object | Inventory specific data - key-value pairs | `false` | Only present if provided by Inventory  |


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

[Connection]:plant-description-management-idd-http-json.md#connection
[HTTP]:https://doi.org/10.17487/RFC7230
[JSON]:https://doi.org/10.17487/RFC7159
[PDE]:plant-description-engine-sysd.md
[Plant Description Management]:plant-description-management-sd.md
[Plant Description Monitor]:plant-description-monitor-sd.md
[Plant Description Management HTTP(S)/JSON - Interface Design Description]:plant-description-management-idd-http-json.md
[PDMon]:plant-description-monitor-sd.md
[Plant Description Engine]:plant-description-engine-sysd.md
[RFC 3339]:https://doi.org/10.17487/RFC3339
[TLS]:https://doi.org/10.17487/RFC8446
