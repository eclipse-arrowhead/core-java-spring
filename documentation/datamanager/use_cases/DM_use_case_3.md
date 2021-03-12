| Name | Description |
| ---- | --------- |
| ID | Historian-Store |
| Brief Description | A Consumer stores data in the Historian service |
| Primary Actors | Consumer, DataManager system |
| Preconditions | -  None|
| Main Flow | - The Consumer performs a request to the DataManager's Historian service. <br/>- The Historian service validates: <br/>- - the request format <br/>- - security <br/>- Sensor data is then stored in the database, ready to be used by any other consumers.
