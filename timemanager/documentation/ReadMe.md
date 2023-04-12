# TimeManager

<a name="timemanager_sdd" />

## System Design Description Overview

The purpose of TimeManager supporting core system is to provide time and location based services.

The TimeManager provides features for a local cloud systems to :
* Fetch accurate and trusted time and location information,


<a name="timemanager_sysd" />

## System Design Overview

<a name="timemanager_provided_services" />

## Provided services

The TimeManager provides the following services:
* [Echo](#timemanager_endpoints_get_echo)
* [Time](#timemanager_endpoints_time)

<a name="timemanager_consumed_services" />

## Consumed services

The TimeManager consumes the following services:

None currently, but will consume Orchestration later on.

<a name="timemanager_usecases" />

## Use cases

The TimeManager has the following use cases:
* [Fetch trusted time](documentation/timemanager/use_cases/TM_use_case_1.md)

<a name="timemanager_endpoints" />

## Endpoints

Swagger API documentation is available on: `https://<host>:<port>` <br />
The base URL for the requests: `http://<host>:<port>/timemanager`

<a name="timemanager_endpoints_client" />

### Client endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#timemanager_endpoints_get_echo) | /echo | GET    | -    | OK     |
| [Time](#timemanager_endpoints_get_time) | /time | GET    | -    | TimeResponse  |

<a name="timemanager_endpoints_get_echo" />

### Echo
```
GET /timemanager/echo
```

Returns a "Got it!" message with the purpose of testing the system availability.

<a name="timemanager_endpoints_get_time" />

### Get trusted time and location
```
GET /timemanager/time
```

Returns time stamps (UNIX in seconds and millseconds), time zone ("Europe/Budapest"), Daylist savings active (true/false) and if the time is trusted (true/false).

<a name="timemanager_gettime_response" />

__TimeResponse__ output:

```json

{
  "epoch": 1627844812,
  "epochMs": 1627844812102,
  "tz": "string",
  "dst": true,
  "trusted": true
}

```