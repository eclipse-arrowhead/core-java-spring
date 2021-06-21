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

In case the QoS Monitor started with the DefaultExternal or the OrchestratedExternal strategy, 
it consumes the "qos-icmp-ping" service.

The "qos-icmp-ping" service is a composit service consisting of a request-response interaction followed by a sequence of events.
The "qos-icmp-ping" service provider should provide an endpoint of the "qos-icmp-ping" service.
The input of the "qos-icmp-ping" service endpoint is an IcmpRequest object.

Input IcmpRequest:
```json
{
    "type": "object",
    "properties": {
        "timeToRepeat": {
            "type": "number"
        },
        "timeout": {
            "type": "number"
        },
        "packetSize": {
            "type": "number"
        },
        "ttl": {
            "type": "number"
        },
        "host": {
            "type": "string"
        }
    },
    "required": [
                "host",
		"ttl",
		"packetSize",
		"timeout",
		"timeToRepeat"
            ]
}
```

The response of the "qos-icmp-ping" service endpoint is an IcmpResponseACK object.

Output IcmpResponseACK:
```json
{
    "type": "object",
    "properties": {
        "externalMeasurementUuid": {
            "type": "string",
            "format": "uuid"
        },
        "ackOk": {
            "type": "string"
        }
    },
    "required": [
                "externalMeasurementUuid",
                "ackOk"
            ]
}
```

The response must be followed by publishing the following events in the order they listed below:

1. RECEIVED_MONITORING_REQUEST
1. STARTED_MONITORING_MEASUREMENT
1. FINISHED_MONITORING_MEASUREMENT

* INTERRUPTED_MONITORING_MEASUREMENT

The "INTERRUPTED_MONITORING_MEASUREMENT" event could precede any other event and it should not be followed by any other event.

In order to receive the events invoked by the "qos-icmp-ping" service, the QoS Monitor provide a [notification](#endpoint_post_ping_event_notification) service endpoint.

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
| [Notification](#endpoint_post_ping_event_notification) | /externalpingmonitorevent | POST    | [EventDTO](#input_post_ping_event_notification)    | OK     |

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

### Notification <a name="endpoint_post_ping_event_notification"/>
```
POST /qos_monitor/externalpingmonitorevent
```
Returns HTTP-OK in order to confirm received event notification.

**Input:** <a name="input_post_ping_event_notification"/>
```json
{
  "eventType": "string",
  "metaData": {
    "processID": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "payload": "string",
  "timeStamp": "string"
}
```

| __Input__  fields |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `eventType` | Type of event. | mandatory | must be parsable to a valid [QosMonitorEventType](#input_qos_monitor_event_type) |
| `metaData` |  The "key - value" pairs for event filtering. | mandatory | max.length = 65535, must contain a "processID" key associated with a string value, which is parsable to UUID object |
| `payload` | String representation of the event. | mandatory | must be an empty list as "{[]}", unless the event type is FINISHED_MONITORING_MEASUREMENT, otherwise it must be a list of [IcmpPingResponse](#input_icmp_ping_response) |
| `timestamp` | The time of publishing  | mandatory | UTC time in `yyyy-MM-dd` `T` `HH`:`mm`:`ss.sss` `Z` format |

**QosMonitorEventType:** <a name="input_qos_monitor_event_type"/>


| __RECEIVED_MONITORING_REQUEST__  type |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `eventType` | Type of event. | mandatory | must be "RECEIVED_MONITORING_REQUEST" |
| `metaData` |  The "key - value" pairs for event filtering. | mandatory | max.length = 65535, must contain a "processID" key associated with a string value, which is parsable to UUID object |
| `payload` | String representation of the event. | mandatory | must be an empty list as "{[]}" |
| `timestamp` | The time of publishing  | mandatory | UTC time in `yyyy-MM-dd` `T` `HH`:`mm`:`ss.sss` `Z` format |


| __STARTED_MONITORING_MEASUREMENT__  type |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `eventType` | Type of event. | mandatory | must be "STARTED_MONITORING_MEASUREMENT" |
| `metaData` |  The "key - value" pairs for event filtering. | mandatory | max.length = 65535, must contain a "processID" key associated with a string value, which is parsable to UUID object |
| `payload` | String representation of the event. | mandatory | must be an empty list as "{[]}" |
| `timestamp` | The time of publishing  | mandatory | UTC time in `yyyy-MM-dd` `T` `HH`:`mm`:`ss.sss` `Z` format |

| __INTERRUPTED_MONITORING_MEASUREMENT__  type |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `eventType` | Type of event. | mandatory | must be "INTERRUPTED_MONITORING_MEASUREMENT" |
| `metaData` |  The "key - value" pairs for event filtering. | mandatory | max.length = 65535, must contain a "processID" key associated with a string value, which is parsable to UUID object, must contain an "EXCEPTION" key associated with a string value,  may contain an "ROOT_CAUSE" key associated with a string value|
| `payload` | String representation of the event. | mandatory | must be an empty list as "{[]}" |
| `timestamp` | The time of publishing  | mandatory | UTC time in `yyyy-MM-dd` `T` `HH`:`mm`:`ss.sss` `Z` format |

| __FINISHED_MONITORING_MEASUREMENT__  type |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `eventType` | Type of event. | mandatory | must be "FINISHED_MONITORING_MEASUREMENT" |
| `metaData` |  The "key - value" pairs for event filtering. | mandatory | max.length = 65535, must contain a "processID" key associated with a string value, which is parsable to UUID object |
| `payload` | String representation of the event. | mandatory | must be an list of [IcmpPingResponse](#input_icmp_ping_response) |
| `timestamp` | The time of publishing  | mandatory | UTC time in `yyyy-MM-dd` `T` `HH`:`mm`:`ss.sss` `Z` format |

**IcmpPingResponse:** <a name="input_icmp_ping_response"/>

| __IcmpPingResponse__  fields |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `successFlag` | Measurement success indicator | mandatory | boolean |
| `timeoutFlag` | Measurement timeout indicator | mandatory | boolean |
| `errorMessage` | String representation of the measurement error. | optional | string |
| `throwable` | String representation of the stacktrace of the measurement error.  | optional | string |
| `host` | Domain name or address of the measured system | mandatory | string |
| `size` | Size of the payload of the measured icmp packet | mandatory | integer |
| `rtt` | Round trip time of the measured icmp packet | mandatory | integer - zero if error|
| `ttl` | Time to live of the measured icmp packet | mandatory | integer |
| `duration` | Measurement time of the measured icmp packet | mandatory | integer - zero if error|

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
