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

<a name="qos_monitor_endpoints_get_echo" />

### Echo 
```
GET /qos_monitor/echo
```

Returns a "Got it" message with the purpose of testing the core service availability.

