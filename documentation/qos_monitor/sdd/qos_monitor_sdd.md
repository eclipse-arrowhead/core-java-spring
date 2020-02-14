<a name="qos_monitor" />

# QOS MONITOR

<a name="qos_monitor_sdd" />

## System Design Description Overview

The purpose of QoS Monitor supporting core system is providing qos measurements to the QoS Manager.

![#1589F0](https://placehold.it/15/1589F0/000000?text=+) `AH Service Registry`
![#f03c15](https://placehold.it/15/f03c15/000000?text=+) `AH Authorization` 
![#c5f015](https://placehold.it/15/c5f015/000000?text=+) `AH Orchestrator / QoS Manager`
![#ffcc44](https://placehold.it/15/a33c00/000000?text=+) `AH QoS Monitor`
![Alt text](/documentation/qos_monitor/sdd/overview.png)

<a name="qos_monitor_sysd" />

## System Design Overview
![Alt text](/documentation/qos_monitor/sysd/qos_monitor_sys_d.jpg)

<a name="qos_monitor_provided_services" />

## Provided services

The QoS Monitor provides the following services:
* [Echo](#qos_monitor_endpoints_get_echo)
* [Ping Measurement](#qos_monitor_endpoints_ping_measurement_by_system_id)

<a name="qos_monitor_consumed_services" />

## Consumed services

The QoS Monitor consumes the following service:
* QueryAll private service from the ServiceRegistry core system

<a name="qos_monitor_usecases" />

## Use cases

The QoS Monitor has the following use cases:
* [Ping Measurement](documentation/qos_monitor/use_cases/QoSMonitor_use_case_1.md)
![Alt text](/documentation/qos_monitor/use_cases/PingMeasurement.png)
* [Reset Counter](documentation/qos_monitor/use_cases/QoSMonitor_use_case_2.md)
![Alt text](/documentation/qos_monitor/use_cases/Reset_Counter.png)
* [Get Measurements](documentation/qos_monitor/use_cases/QoSMonitor_use_case_3.md)
![Alt text](/documentation/qos_monitor/use_cases/GetMeaurements.png)

<a name="qos_monitor_endpoints" />

## Endpoints

<a name="qos_monitor_endpoints_client" />

### Client endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#qos_monitor_endpoints_get_echo) | /echo | GET    | -    | OK     |

<a name="qos_monitor_endpoints_management" />

### Management endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get ping measurements mgmt](#qos_monitor_endpoints_get_ping_measurements_list) | /mgmt/ping/measurements | GET | direction && item_per_page && page && sort_field | [PingMeasurement list response](#qos_monitor_ping_measurement_list_response) |
| [Get ping measurements by system id mgmt](#qos_monitor_mgmt_endpoints_get_ping_measurement_by_system_id) | /mgmt/ping/measurements/{id} | GET | id | [Ping Measurment response](#qos_monitor_ping_measurement_response) |

<a name="qos_monitor_endpoints_private" />

### Private endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get ping measurements by system id](#qos_monitor_endpoints_get_ping_measurement_by_system_id) | /ping/measurements/{id} | GET | id | [Ping Measurment response](#qos_monitor_ping_measurement_response) |

<a name="qos_monitor_endpoints_get_echo" />

### Echo 
```
GET /qos_monitor/echo
```

Returns a "Got it" message with the purpose of testing the core service availability.

### Get ping measurements mgmt

```
GET /mgmt/ping/measurements
```

__Get subscriptions query parameters__ the input :

`https://qos_monitor_ip:qos_monitor_port/qos_monitor/mgmt/ping/measurements?dirction=`ASC`&item_per_page=`100`&page=`0`&sort_field=`id

| __Get ping measurements mgmt__  query parameters |
| ------------------------------------------------------- |

| Parameter | Description | Necessity | Format/Limitations |
| --------- | ----------- | --------- | ----------- |
| `direction` |  Direction of sorting. | optional | valid values: "ASC", "DESC" - default: "ASC"|
| `item_per_page` | Maximum number of items returned. | optional (mandatory, if page is defined)| integer |
| `page` | Zero based page index. | optional (mandatory, if item_per_page is defined)| integer |
| `sort_field` | The field to sort the results by. | optional | valid values: "id", "updatedAt", "createdAt" - default: "id" |

<a name="qos_monitor_ping_measurement_list_response" />

__PingMeasurement list response__ the output :

```json
{
	"data": [
		{
			"id": 1,
			"measurement": {
				"id": 1,
				"system": {
				"id": 5,
				"systemName": "testsystem",
				"address": "testsystem.ai",
				"port": 12345,
				"createdAt": "2020-02-04 09:38:56",
				"updatedAt": "2020-02-04 09:38:56"
				},
				"measurementType": "PING",
				"lastMeasurementAt": "2020-02-14T10:48:10+01:00",
				"createdAt": "2020-02-04T10:42:04+01:00",
				"updatedAt": "2020-02-14T10:48:47+01:00"
			},
			"available": true,
			"lastAccessAt": "2020-02-14T10:48:10+01:00",
			"minResponseTime": 15,
			"maxResponseTime": 26,
			"meanResponseTimeWithTimeout": 18,
			"meanResponseTimeWithoutTimeout": 18,
			"jitterWithTimeout": 3,
			"jitterWithoutTimeout": 3,
			"lostPerMeasurementPercent": 0,
			"sent": 12075,
			"received": 12070,
			"countStartedAt": "2020-02-07T00:00:00+01:00",
			"sentAll": 63175,
			"receivedAll": 56721,
			"createdAt": "2020-02-14T10:48:47+01:00",
			"updatedAt": "2020-02-14T10:48:47+01:00"
		}
	],
	"count": 1
}
```

<a name="qos_monitor_mgmt_endpoints_get_ping_measurement_by_system_id" />

### Get ping measurements by system id mgmt

```
GET /mgmt/ping/measurements/{id}
```

__Get ping measurements by system id mgmt path parameter__ the input :

`https://qos_monitor_ip:qos_monitor_port/qos_monitor/mgmt/ping/measurements/`1

| __Get ping measurement by system id__ path parameter |
| ------------------------------------------------------- |

| Parameter | Description | Necessity | Format/Limitations |
| --------- | ----------- | --------- | ----------- |
| `id` |  Id of measured system | mandatory | integer |

<a name="qos_monitor_ping_measurement_response" />

__Ping Measurment response by system id__ the output :

```json
	{
		"id": 1,
		"measurement": {
			"id": 1,
			"system": {
			"id": 5,
			"systemName": "testsystem",
			"address": "testsystem.ai",
			"port": 12345,
			"createdAt": "2020-02-04 09:38:56",
			"updatedAt": "2020-02-04 09:38:56"
			},
			"measurementType": "PING",
			"lastMeasurementAt": "2020-02-14T10:48:10+01:00",
			"createdAt": "2020-02-04T10:42:04+01:00",
			"updatedAt": "2020-02-14T10:48:47+01:00"
		},
		"available": true,
		"lastAccessAt": "2020-02-14T10:48:10+01:00",
		"minResponseTime": 15,
		"maxResponseTime": 26,
		"meanResponseTimeWithTimeout": 18,
		"meanResponseTimeWithoutTimeout": 18,
		"jitterWithTimeout": 3,
		"jitterWithoutTimeout": 3,
		"lostPerMeasurementPercent": 0,
		"sent": 12075,
		"received": 12070,
		"countStartedAt": "2020-02-07T00:00:00+01:00",
		"sentAll": 63175,
		"receivedAll": 56721,
		"createdAt": "2020-02-14T10:48:47+01:00",
		"updatedAt": "2020-02-14T10:48:47+01:00"
	}
```

<a name="qos_monitor_endpoints_get_ping_measurement_by_system_id" />

### Get ping measurements by system id 

For private endpoints no detailed description available.