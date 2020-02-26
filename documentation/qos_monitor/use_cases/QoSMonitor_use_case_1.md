<a name="qos_monitor_usecases" />

## Use case: Ping Measurement
| Name | Description |
| ---- | --------- |
| ID | Ping Measurement |
| Brief Description | QosMonitors Ping Task process is tasked to measure the ping related values of all the availble systems |
| Primary Actors | QosMonitor - Ping Task process, QosMonitor - DB service, Service Registry |
| Preconditions | -  Service Registry has to be availble. 
| Main Flow | - The Ping Task Configuration send a start signal to PingTask. <br/>- If the PingTask is still working on the last task, it skips the start signal.<br/>- If the PingTask is not working it starts working.<br/>- PingTask call ServiceRegistry's for the list of registered systems.<br/>- When list returned PingTask check if there are system on the list.<br/>- If not, PingTask finish working.<br/>- If there are systems, PingTask start:<br/>-- Ping the system.<br/>-- Calculate measurement results.<br/>-- Logs results to DB.<br/>-- Check for next system to measure.
