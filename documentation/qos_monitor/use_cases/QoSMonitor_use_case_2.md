<a name="qos_monitor_usecases" />

## Use case: Reset Counter

| Name | Description |
| ---- | --------- |
| ID | Reset Counter |
| Brief Description | QosMonitors Reset Counter Task process is tasked to reset the ping mesasurements sent, received and count-started-at fields |
| Primary Actors | QosMonitor - Reset Counter Task process, QosMonitor - DB service |
| Main Flow | - The Reset Counter Task Configuration send a start signal to PingTask.<br/>- If the Reset Counter Task is still working on the last task, it skips the start signal.<br/>- If the Reset Counter Task is not working it starts working.<br/>- Reset Counter Task call QoS DB Service's to update ping measurments.<br/>- QoS DB Service retrieve all ping measurement.<br/>- QoS DB Service reset all ping mesasurements sent, received fields to 0 and reset count-started-at field to current date.<br/>- Qos DB Service return ok to Reset Counter Task.<br/>- Reset Counter Task stops working.
