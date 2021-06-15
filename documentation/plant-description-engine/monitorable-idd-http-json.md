# Monitorable HTTP(S)/JSON - Interface Design Description

## Abstract
This document describes the HTTP/{TLS}/JSON variant of a service that can be used by core systems to monitor an application system.

## Overview
This document describes the HTTP/{TLS}/JSON variant of the [Monitorable]
service, which allows for arbitrary Arrowhead Framework systems to monitor an application system.
Readers of this document are assumed to be familiar with the [Monitorable] service.
For more information about the service, please refer to the service description document [Monitorable].
The rest of this document describes how to realize the [Monitorable] service using [HTTP], optionally with [TLS], and [JSON], both in terms of its [interfaces](#service-interfaces) and its [information model](#information-model).

## Service Interfaces
This section describes the interfaces that must be exposed by [Monitorable] services. In particular, the below
subsection first names the HTTP method and path used to call the interface, after which it names an abstract
interface from the [Monitorable] service description document, output type, as well as errors that can be thrown. The
interface is expected to respond with HTTP status code 200 OK for all successful calls.

### GET {baseURL}/inventoryid
 - __Interface:	GetInventoryId__
 - __Output: [InventoryId](#inventoryid)__

Called to acquire the application systems Inventory id.

Example of valid invocation:
```json
GET /inventoryid HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Length: 36
Content-Type: application/json

{
	"id": "application/system/1203"
}
```

### GET {baseURL}/systemdata
 - __Interface: GetSystemData__
 - __Output: [SystemData](#systemdata)__

Called to acquire the latest copy of the application systems monitorable system data.

Example of valid invocation:
```json
GET /systemdata HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Length: 87
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

### GET {baseURL}/ping
 - __Interface: Ping__
 - __Output: Any valid JSON object__

Called to check that the application system is alive and responding to service requests.

Example of valid invocation:
```json
GET /ping HTTP/1.1
Accept: application/json
```


Example of valid response:
```html
HTTP/1.1 200 OK
Content-Length: 18
Content-Type: application/json

{
	"pong": true
}
```

## Information model
Here, all data objects that can be part of Monitorable service calls are listed in alphabetic order.
As a complement to the explicitly defined types
in this section, there is also a list of implicit [primitive](#primitives) types.

### InventoryId
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `id` | String | The system's Id in an Inventory system | `false` | |

### SystemData
JSON object with the following fields:

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `data` | Object | System specific data - key-value pairs | `false` | {} |


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
[Monitorable]:monitorable-sd.md
[TLS]:https://doi.org/10.17487/RFC8446