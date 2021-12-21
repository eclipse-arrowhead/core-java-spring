# Inventory HTTP(S)/JSON - Interface Design Description

## Abstract
This document describes the HTTP/{TLS}/JSON variant of a service that be used query to an Inventory system for Inventory data related to application systems.

## Overview
This document describes the HTTP/{TLS}/JSON variant of the [Inventory]
service, which allows for arbitrary Arrowhead Framework systems to enable querying for Inventory data related to application systems.

Readers of this document are assumed to be familiar with the [Inventory] service.
For more information about the service, please refer to the service description document [Inventory].
The rest of this document describes how to realize the [Inventory] service using [HTTP], optionally with [TLS], and [JSON], both in terms of its [interfaces](#service-interfaces) and its [information model](#information-model).

## Service Interfaces
This section describes the interfaces that must be exposed by [Inventory] services. In particular, the below
subsection first names the HTTP method and path used to call the interface, after which it names an abstract
interface from the [Inventory] service description document, output type, as well as errors that can be thrown. The
interface is expected to respond with HTTP status code 200 OK for all successful calls.

### GET {baseURL}/data/{id}
 - __Interface:	GetInventoryData__
 - __Output: [InventoryData](#inventorydata)__

Called to acquire the application system's inventory data.

Example of valid invocation:
```json
GET /inventory/data/1 HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Length: 88
Content-Type: application/json

{
	"data": {
		"key1": "value1",
		"key2": 2,
		"key3": {
			"key3_key1": 1
		}
	}
}
```

### GET {baseURL}/system
 - __Interface: GetInventorySystems__
 - __Output: [Systemlist](#systemlist)__

Called to acquire for systems present in the Inventory system.

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `page` | zero based page index | no |
| `item_per_page` | maximum number of items returned | no |
| `filter_field` | filter by a given column | no |
| `filter_value` | value to filter by | no |


> **Note:**  Possible values for `filter_field` are:
> * `systemName`
> * `metaData`

Example of valid invocation:
```json
GET /inventory/system?filter_field=systemName&filter_value=my%20system HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Length: 141
Content-Type: application/json

{
	"count": 1,
	"data": [{
		"inventoryId": "1",
		"systemName": "mysystem",
		"metaData": {
			"key1": 1,
			"key2": "value 2"
		}
	}]
}
```

## Information model
Here, all data objects that can be part of Inventory service calls are listed in alphabetic order.
As a complement to the explicitly defined types
in this section, there is also a list of implicit [primitive](#primitives) types.

### inventoryData
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `data` | Object | System specific data - key-value pairs | `false` | {} |

### System

| Field | Type | Description | Mandatory |
| ----- | ---- | ----------- | --------- |
| `inventoryId` | String | The system's Id in this Inventory system | `true` |
| `systemName` | String | Identity of the system | `false` |
| `metadata` | Object\<String> | Metadata - key-value pairs | `false` |

### SystemList

| Field | Type | Description | Mandatory |
| ----- | ---- | ----------- | --------- |
| `count` | Integer | Number of records found | `true` |
| `data` | Array\<[System](#system)> | Array of [Systems](#system) that the Inventory contains | `true` |


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

[HTTP]:https://doi.org/10.17487/RFC7230
[JSON]:https://doi.org/10.17487/RFC7159
[Inventory]:inventory-sd.md
[TLS]:https://doi.org/10.17487/RFC8446