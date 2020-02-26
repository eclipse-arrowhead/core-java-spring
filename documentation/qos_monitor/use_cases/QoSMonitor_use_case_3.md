<a name="qos_monitor_usecases" />

## Use case: Get Measurements

| Name | Description |
| ---- | --------- |
| ID | Get Measurements|
| Brief Description | QosMonitors is tasked to retrieve ping measurements |
| Primary Actors | Orchestrator - QosManager, QosMonitor - Controller, QosMonitor - DB service |
| Main Flow | - QosManager sends a getPingMeasurements request to QoS Monitor.<br/>- QoS Monitor checks if the request came from the Orchestrator.<br/>- If not, QoS Monitor returns Authorization Exception.<br/>- If the request came from the Orchestrator, QoSMonitor Controller validate the request parameter.<br/>- If the parameter is not valid, the Controller returns BadRequest Exception.<br/>- If the parameter is valid, the Controller call QoS DB Service for the ping measurement specified by the request parameter.<br/>- Qos DB Service returns the ping measurement to the Controller.<br/>- The Controller returns the ping measurement to the QoS Manager.
