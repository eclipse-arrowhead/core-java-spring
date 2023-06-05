# DataManager

<a name="datamanager_sdd" />

## System Design Description Overview

The purpose of DataManager supporting core system is to provide storage of sensor data.


The DataManager provides features for producers and consumers to:
* Store SenML sensor and actuator data,
* Fetch cached data,
* and perform database queries.

Type support data model is SenML https://tools.ietf.org/html/rfc8428

<a name="datamanager_sysd" />

## System Design Overview

<a name="datamanager_provided_services" />

## Provided services

The DataManager provides the following services:
* [Echo](#datamanager_endpoints_get_echo)
* [Historian](#datamanager_endpoints_historian)
* [Proxy](#datamanager_endpoints_proxy)

<a name="datamanager_consumed_services" />

## Consumed services

The DataManager consumes the following services:

None currently, but will consume Orchestration later on.

<a name="datamanager_usecases" />

## Use cases

The DataManager has the following use cases:
* [Update cache message](documentation/datamanager/use_cases/DM_use_case_1.md)
* [Fetch cache message](documentation/datamanager/use_cases/DM_use_case_2.md)
* [Update stored message](documentation/datamanager/use_cases/DM_use_case_3.md)
* [Fetch stored message](documentation/datamanager/use_cases/DM_use_case_4.md)

<a name="datamanager_endpoints_historian" />

## Endpoints

Swagger API documentation is available on: `https://<host>:<port>` <br />
The base URL for the requests: `http://<host>:<port>/datamanager`

<a name="datamanager_endpoints_client" />

### Client endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#datamanager_endpoints_get_echo) | /echo | GET    | -    | OK     |
| [Get system list](#datamanager_histendpoints_getsys) | /historian | GET    | -    | SystemList     |
| [Get service list](#datamanager_histendpoints_getsrv_from_sys) | /historian/{systemName} | GET    | -    | ServiceList |
| [Fetch data from db](#datamanager_histendpoints_getdb) | /historian/{systemName}/{serviceName} | GET    | -   | SenML |
| [Store data in db](#datamanager_histendpoints_storedb) | /historian/{systemName}/{serviceName} | PUT    | SenML   | - |
| [Get system list](#datamanager_proxyendpoints_getsys) | /proxy | GET    | -    | SystemList     |
| [Get service list](#datamanager_proxyendpoints_getsrv_from_sys) | /proxy/{systemName} | GET    | -    | ServiceList |
| [Fetch data from cache](#datamanager_proxyendpoints_fetchdata) | /proxy/{systemName}/{serviceName} | GET    | -   | SenML |
| [Store data in cache](#datamanager_proxyendpoints_storedata) | /proxy/{systemName}/{serviceName} | PUT    | SenML   | - |

<a name="datamanager_endpoints_get_echo" />

### Echo
```
GET /datamanager/echo
```

Returns a "Got it!" message with the purpose of testing the system availability.

<a name="datamanager_proxyendpoints_getsys" />

### Get system list
```
GET /datamanager/proxy/
```

Returns a list of all systems that have at least one active service endpoint.

<a name="datamanager_getsyslist_response" />

__GetSystemListResponse__ output:

```json

{
  "systems": ["systemName1", "systemNameX"]
}

```

<a name="datamanager_proxyendpoints_getsrv_from_sys" />

### Get service list
```
GET /datamanager/proxy/{systemName}
```

Returns a list of all service endpoints that are active.

<a name="datamanager_proxygetsrvlist_response" />

__GetServicesResponse__ output:

```json

{
  "services": ["serviceDefinition1", "serviceDefinitionX"]
}

```

<a name="datamanager_proxyendpoints_fetchdata" />

### Fetch data from cache
```
GET /datamanager/proxy/{systemName}/{serviceName}
```

Returns sensor data from a service endpoint from the cache.

<a name="datamanager_proxygetsrvdata_response" />

__GetServiceDataResponse__ output:

```json

[
  {
    "bn": "string",
    "bt": 0.0,
    "bu": "string",
    "bver": 0
  }, {
    "n": "string",
    "t": 0.0,
    "u": "string",
    "v": 0.0,
    "vs": "string",
    "vb": false,
    "vd": "string"
  }
]
```

<a name="datamanager_proxyendpoints_storedata" />

### Store data in cache
```
PUT /datamanager/proxy/{systemName}/{serviceName}
```

Stores sensor data in a service endpoint in the proxy cache.

<a name="datamanager_putsrvdata_request" />

__PutServiceDataRequest__ input:

```json

[
  {
    "bn": "string",
    "bt": 0.0,
    "bu": "string",
    "bver": 0
  }, {
    "n": "string",
    "t": 0.0,
    "u": "string",
    "v": 0.0,
    "vs": "string",
    "vb": false,
    "vd": "string"
  }
]
```


<a name="datamanager_histendpoints_getsys" />

### Get system list
```
GET /datamanager/historian
```

Returns a list of all systems that have at least one service endpoint in the database.

<a name="datamanager_getsyslist_response" />

__GetSystemListResponse__ output:

```json

{
  "systems": ["systemName1", "systemNameX"]
}

```

<a name="datamanager_histendpoints_getsrv_from_sys" />

### Get service list
```
GET /datamanager/historian/{systemName}
```

Returns a list of all service endpoints that have data stored in the database.

<a name="datamanager_histgetsrvlist_response" />

__GetServicesResponse__ output:

```json

{
  "services": ["serviceDefinition1", "serviceDefinitionX"]
}

```

<a name="datamanager_histendpoints_getdb" />

### Fetch data from db
```
GET /datamanager/historian/{systemName}/{serviceName}
```

Returns sensor data from a service endpoint from the database.

<a name="datamanager_histgetsrvdata_response" />

__GetServiceDataResponse__ output:

```json

[
  {
    "bn": "string",
    "bt": 0.0,
    "bu": "string",
    "bver": 0
  }, {
    "n": "string",
    "t": 0.0,
    "u": "string",
    "v": 0.0,
    "vs": "string",
    "vb": false,
    "vd": "string"
  }
]
```

<a name="datamanager_histendpoints_storedb" />

### Store data in db
```
PUT /datamanager/historian/{systemName}/{serviceName}
```

Stores sensor data in a service endpoint in the database.

<a name="datamanager_putsrvdata_request" />

__PutServiceDataRequest__ input:

```json

[
  {
    "bn": "string",
    "bt": 0.0,
    "bu": "string",
    "bver": 0
  }, {
    "n": "string",
    "t": 0.0,
    "u": "string",
    "v": 0.0,
    "vs": "string",
    "vb": false,
    "vd": "string"
  }
]
```
