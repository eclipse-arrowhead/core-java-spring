| Name | Description |
| ---- | --------- |
| ID | Proxy-Fetch |
| Brief Description | A Consumer fetches data from the Proxy service |
| Primary Actors | Consumer, DataManager system |
| Preconditions | -  A consumer must have stored data at the target endpoint |
| Main Flow | - The Consumer performs a request to get data from the DataMana ger's Proxy service. <br/>- The Proxy service validates: <br/>- - security <br/>- If a consumer has stored data there previously, the data is returned. If no data exists, an error is returned.
