| Name | Description |
| ---- | --------- |
| ID | Proxy-Store |
| Brief Description | A Consumer stores data in the Proxy service |
| Primary Actors | Consumer, DataManager system |
| Preconditions | -  None|
| Main Flow | - The Consumer performs a request to the DataManager's Proxy service. <br/>- The Proxy service validates: <br/>- - the request format <br/>- - security <br/>- Sensor data is then cached, ready to be used by any other consumers
