| Name | Description |
| ---- | --------- |
| ID | Historian-Fetch |
| Brief Description | A Consumer fetches data from the Historian service |
| Primary Actors | Consumer, DataManager system |
| Preconditions | -  A consumer must have stored data at the target endpoint |
| Main Flow | - The Consumer performs a request to get data from the DataManager's Historian service. <br/>- The Historian service validates: <br/>- - security <br/>- If a consumer has stored data in the database previously, the data is returned. If no data exists, an error is returned.
