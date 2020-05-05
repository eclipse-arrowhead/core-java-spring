# QUALITY OF SERVICE MONITOR
### Interface Design Description

QoS-Monitor offers three types of endpoints. Client, Management and Private.

Swagger API documentation is available on: `https://<host>:<port>`.<br/>
The base URL for the requests: `http://<host>:<port>/qos_monitor`.

## Client Endpoint Description

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#endpoint_get_echo) | /echo | GET    | -    | OK     |

## Private Endpoint Description

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get Public Key](#endpoint_get_publickey) | /publickey | GET | - | [String](#output_get_publickey) |
| [Retrieve Intra-Cloud Ping Measurement](#endpoint_get_intra_ping) | /measurements/intracloud/ping/{id} | GET | [id](#input_get_intra_ping) | [Response DTO](#output_get_intra_ping) |
| [Calculate Intra-Cloud Ping Median Measurement](#endpoint_get_intra_median_ping) | /measurements/intracloud/ping_median/{attribute} | GET | [attribute](#input_get_intra_median_ping) | [Response DTO](#output_get_intra_median_ping) |
| [Retrieve Inter-Cloud Direct Ping Measurement](#endpoint_post_inter_direct_ping) | /measurements/intercloud/ping | POST | [Request DTO](#input_post_inter_direct_ping) | [Response DTO](#output_post_inter_direct_ping) |
| [Retrieve Inter-Cloud Relay Echo Measurement](#endpoint_post_inter_relay_echo) | /measurements/intercloud/relay_echo | POST | [Request DTO](#input_post_inter_relay_echo) | [Response DTO](#output_post_inter_relay_echo) |

## Management Endpoint Description

todo

---

### Echo <a name="endpoint_get_echo"/>
```
GET /qos_monitor/echo
```

Returns a "Got it" message with the purpose of testing the core service availability.

### Get Public Key <a name="endpoint_get_publickey"/>
```
GET /qos_monitor/publickey
```

Returns the public key of the QoS Monitor Core System.

**Output:** <a name="output_get_publickey"/>

Base64 encoded text.

### Retrieve Intra-Cloud Ping Measurement <a name="endpoint_get_intra_ping"/>
```
GET /qos_monitor/measurements/intracloud/ping/{id}
```

Returns the requested Intra-Cloud Ping Measurement entry by system id.

**Input:** <a name="input_get_intra_ping"/>

ID of the intra-cloud ping measurement as a path variable.

**Output:** <a name="output_get_intra_ping"/>

```json
{
  "id": 0,
  "measurement": {
    "id": 0,
    "measurementType": "PING",
    "system": {
      "id": 0,
      "systemName": "string",
      "address": "string",      
      "port": 0,
      "authenticationInfo": "string",
      "createdAt": "string",
      "updatedAt": "string"
    },
    "lastMeasurementAt": "2020-05-04T13:50:19.127Z",
    "createdAt": "2020-05-04T13:50:19.127Z",
    "updatedAt": "2020-05-04T13:50:19.127Z"
  },
  "available": true,
  "minResponseTime": 0,
  "maxResponseTime": 0,
  "meanResponseTimeWithTimeout": 0,
  "meanResponseTimeWithoutTimeout": 0,
  "jitterWithTimeout": 0,
  "jitterWithoutTimeout": 0,  
  "lostPerMeasurementPercent": 0,
  "sent": 0,
  "sentAll": 0,
  "received": 0,
  "receivedAll": 0,
  "lastAccessAt": "2020-05-04T13:50:19.127Z",
  "countStartedAt": "2020-05-04T13:50:19.127Z",
  "createdAt": "2020-05-04T13:50:19.127Z",
  "updatedAt": "2020-05-04T13:50:19.127Z"
}
```

| Field | Description |
| ----- | ----------- |
| id | ID of the intra-cloud ping measurement |
| measurement.id | ID of the intra-cloud measurement |
| measurement.measurementType | Type of the measurement |
| measurement.system.id | ID of the measured system |
| measurement.system.systemName | Name of the measured system |
| measurement.system.address | Address of the measured system |
| measurement.system.port | Port of the measured system |
| measurement.system.authenticationInfo | Base64 encoded public key of the measured system |
| measurement.system.createdAt | Date of creation of the measured system |
| measurement.system.updatedAt | Date of update of the measured system |
| measurement.lastMeasurementAt | Time of the last measurement |
| measurement.createdAt | Date of creation of the measurement |
| measurement.updatedAt | Date of update of the measurement |
| available | Boolean value of the systems calculated availability|
| minResponseTime | Integer value of milliseconds of the fastest returned ping|
| maxResponseTime | Integer value of milliseconds of the slowest returned ping|
| meanResponseTimeWithTimeout | Integer value of milliseconds of the calculated average of pings including timeouts|
| meanResponseTimeWithoutTimeout | Integer value of milliseconds of the calculated average of pings not including timeouts|
| jitterWithTimeout | Integer value of milliseconds of the calculated standard deviation of pings including timeouts|
| jitterWithoutTimeout | Integer value of milliseconds of the calculated standard deviation of pings not including timeouts|
| lostPerMeasurementPercent | Integer value of calculated lost ping percentage|
| sent | Integer value of sent pings in measurement|
| sentAll | Integer value of sent pings since ping measurement created|
| received | Integer value of received pings in measurement|
| receivedAll | Integer value of received pings since ping measurement created|
| lastAccessAt | TimeStamp value of the systems last known availability|
| countStartedAt | TimeSatmp value of the last reset of sent and received fields|
| createdAt | Date of creation of the ping measurement |
| updatedAt | Date of update of the ping measurement |

### Calculate Intra-Cloud Ping Median Measurement <a name="endpoint_get_intra_median_ping"/>
```
GET /qos_monitor/measurements/intracloud/ping_median/{attribute}
```

Returns the calculated median Intra-Cloud Ping Measurement entry by defined attribute.

**Input:** <a name="input_get_intra_median_ping"/>

Attribute of the ping measurement as a path variable. Possible values are:

`MIN_RESPONSE_TIME, MAX_RESPONSE_TIME, MEAN_RESPONSE_TIME_WITH_TIMEOUT, MEAN_RESPONSE_TIME_WITHOUT_TIMEOUT, JITTER_WITH_TIMEOUT, JITTER_WITHOUT_TIMEOUT, LOST_PER_MEASUREMENT_PERCENT`

**Output:** <a name="output_get_intra_median_ping"/>

```json
{
  "id": 0,
  "measurement": {
    "id": 0,
    "measurementType": "PING",
    "system": {
      "id": 0,
      "systemName": "string",
      "address": "string",      
      "port": 0,
      "authenticationInfo": "string",
      "createdAt": "string",
      "updatedAt": "string"
    },
    "lastMeasurementAt": "2020-05-04T13:50:19.127Z",
    "createdAt": "2020-05-04T13:50:19.127Z",
    "updatedAt": "2020-05-04T13:50:19.127Z"
  },
  "available": true,
  "minResponseTime": 0,
  "maxResponseTime": 0,
  "meanResponseTimeWithTimeout": 0,
  "meanResponseTimeWithoutTimeout": 0,
  "jitterWithTimeout": 0,
  "jitterWithoutTimeout": 0,  
  "lostPerMeasurementPercent": 0,
  "sent": 0,
  "sentAll": 0,
  "received": 0,
  "receivedAll": 0,
  "lastAccessAt": "2020-05-04T13:50:19.127Z",
  "countStartedAt": "2020-05-04T13:50:19.127Z",
  "createdAt": "2020-05-04T13:50:19.127Z",
  "updatedAt": "2020-05-04T13:50:19.127Z"
}
```

| Field | Description |
| ----- | ----------- |
| id | ID of the intra-cloud ping measurement |
| measurement.id | ID of the intra-cloud measurement |
| measurement.measurementType | Type of the measurement |
| measurement.system.id | ID of the measured system |
| measurement.system.systemName | Name of the measured system |
| measurement.system.address | Address of the measured system |
| measurement.system.port | Port of the measured system |
| measurement.system.authenticationInfo | Base64 encoded public key of the measured system |
| measurement.system.createdAt | Date of creation of the measured system |
| measurement.system.updatedAt | Date of update of the measured system |
| measurement.lastMeasurementAt | Time of the last measurement |
| measurement.createdAt | Date of creation of the measurement |
| measurement.updatedAt | Date of update of the measurement |
| available | Boolean value of the systems calculated availability|
| minResponseTime | Integer value of milliseconds of the fastest returned ping|
| maxResponseTime | Integer value of milliseconds of the slowest returned ping|
| meanResponseTimeWithTimeout | Integer value of milliseconds of the calculated average of pings including timeouts|
| meanResponseTimeWithoutTimeout | Integer value of milliseconds of the calculated average of pings not including timeouts|
| jitterWithTimeout | Integer value of milliseconds of the calculated standard deviation of pings including timeouts|
| jitterWithoutTimeout | Integer value of milliseconds of the calculated standard deviation of pings not including timeouts|
| lostPerMeasurementPercent | Integer value of calculated lost ping percentage|
| sent | Integer value of sent pings in measurement|
| sentAll | Integer value of sent pings since ping measurement created|
| received | Integer value of received pings in measurement|
| receivedAll | Integer value of received pings since ping measurement created|
| lastAccessAt | TimeStamp value of the systems last known availability|
| countStartedAt | TimeSatmp value of the last reset of sent and received fields|
| createdAt | Date of creation of the ping measurement |
| updatedAt | Date of update of the ping measurement |

### Retrieve Inter-Cloud Direct Ping Measurement <a name="endpoint_post_inter_direct_ping"/>
```
GET /qos_monitor/measurements/intercloud/ping
```

Returns the requested Inter-Cloud Direct Ping Measurement entry by cloud and system.

**Input:**  <a name="input_post_inter_direct_ping"/>

```json
{
  "cloud": {    
    "id": 0,
    "operator": "string",
    "name": "string",        
    "ownCloud": false,
    "neighbor": true,
    "secure": true,
    "authenticationInfo": "string",
    "createdAt": "string",
    "updatedAt": "string"
  },
  "system": {
    "id": 0,
    "systemName": "string",
    "address": "string",
    "port": 0,
    "authenticationInfo": "string",
    "createdAt": "string",    
    "updatedAt": "string"
  }
}
```

| Field | Description | Necessity |
| ----- | ----------- | --------- |
| cloud.id | ID of the cloud | mandatory |
| cloud.operator | Operator of the cloud | mandatory |
| cloud.name | Name of the cloud | mandatory |
| cloud.owncloud | Flag to indicate own cloud (meant to be false) | optional |
| cloud.neighbor | Flag to indicate neighbor cloud | optional |
| cloud.secure | Flag to indicate security | optional |
| cloud.authenticationInfo | Base64 encoded public key of the cloud | optional |
| cloud.createdAt | Date of creation of the cloud | optional |
| cloud.updatedAt | Date of update of the cloud | optional |
| system.id | ID of the system |  mandatory |
| system.systemName | Name of the system |  mandatory |
| system.address | Address of the system |  mandatory |
| system.port | Port of the system | optional |
| system.authenticationInfo | Base64 encoded public key of the system | optional |
| system.createdAt | Date of creation of the system | optional |
| system.updatedAt | Date of update of the system | optional |

**Output:** <a name="output_post_inter_direct_ping"/>

```json
{
  "id": 0,
  "measurement": {
    "id": 0,
    "measurementType": "PING",
    "address": "string",
    "cloud": {    
      "id": 0,
      "operator": "string",
      "name": "string",        
      "ownCloud": false,
      "neighbor": true,
      "secure": true,
      "authenticationInfo": "string",
      "createdAt": "string",
      "updatedAt": "string"
    },
    "lastMeasurementAt": "2020-05-04T13:50:19.127Z",
    "createdAt": "2020-05-04T13:50:19.127Z",
    "updatedAt": "2020-05-04T13:50:19.127Z"
  },
  "available": true,
  "minResponseTime": 0,
  "maxResponseTime": 0,
  "meanResponseTimeWithTimeout": 0,
  "meanResponseTimeWithoutTimeout": 0,
  "jitterWithTimeout": 0,
  "jitterWithoutTimeout": 0,  
  "lostPerMeasurementPercent": 0,
  "sent": 0,
  "sentAll": 0,
  "received": 0,
  "receivedAll": 0,
  "lastAccessAt": "2020-05-04T13:50:19.127Z",
  "countStartedAt": "2020-05-04T13:50:19.127Z",
  "createdAt": "2020-05-04T13:50:19.127Z",
  "updatedAt": "2020-05-04T13:50:19.127Z"
}
```

| Field | Description |
| ----- | ----------- |
| id | ID of the intra-cloud ping measurement |
| measurement.id | ID of the intra-cloud measurement |
| measurement.measurementType | Type of the measurement |
| measurement.address | Address of neighbor system(s) pinged |
| measurement.cloud.id | ID of the measured cloud |
| measurement.cloud.operator | Operator of the measured cloud |
| measurement.cloud.name | Name of the measured cloud |
| measurement.cloud.owncloud | Flag to indicate own cloud (meant to be false) |
| measurement.cloud.neighbor | Flag to indicate neighbor cloud |
| measurement.cloud.secure | Flag to indicate security |
| measurement.cloud.authenticationInfo | Base64 encoded public key of the measured cloud |
| measurement.cloud.createdAt | Date of creation of the measured cloud |
| measurement.cloud.updatedAt | Date of update of the measured cloud |
| measurement.lastMeasurementAt | Time of the last measurement |
| measurement.createdAt | Date of creation of the measurement |
| measurement.updatedAt | Date of update of the measurement |
| available | Boolean value of the systems calculated availability|
| minResponseTime | Integer value of milliseconds of the fastest returned ping|
| maxResponseTime | Integer value of milliseconds of the slowest returned ping|
| meanResponseTimeWithTimeout | Integer value of milliseconds of the calculated average of pings including timeouts|
| meanResponseTimeWithoutTimeout | Integer value of milliseconds of the calculated average of pings not including timeouts|
| jitterWithTimeout | Integer value of milliseconds of the calculated standard deviation of pings including timeouts|
| jitterWithoutTimeout | Integer value of milliseconds of the calculated standard deviation of pings not including timeouts|
| lostPerMeasurementPercent | Integer value of calculated lost ping percentage|
| sent | Integer value of sent pings in measurement|
| sentAll | Integer value of sent pings since ping measurement created|
| received | Integer value of received pings in measurement|
| receivedAll | Integer value of received pings since ping measurement created|
| lastAccessAt | TimeStamp value of the systems last known availability|
| countStartedAt | TimeSatmp value of the last reset of sent and received fields|
| createdAt | Date of creation of the ping measurement |
| updatedAt | Date of update of the ping measurement |

### Retrieve Inter-Cloud Relay Echo Measurement <a name="endpoint_post_inter_relay_echo"/>
```
GET /qos_monitor/measurements/intercloud/relay_echo
```

Returns the requested Inter-Cloud Relay Echo Measurement entries by cloud.

**Input:**  <a name="input_post_inter_relay_echo"/>

```json
{
  "operator": "string",
  "name": "string",        
  "ownCloud": false,
  "neighbor": true,
  "secure": true,
  "authenticationInfo": "string"
  "gatekeeperRelayIds": [
    0
  ],
  "gatewayRelayIds": [
    0
  ]
}
```

| Field | Description | Necessity |
| ----- | ----------- | --------- |
| operator | Operator of the cloud | mandatory |
| name | Name of the cloud | mandatory |
| owncloud | Flag to indicate own cloud (meant to be false) | optional |
| neighbor | Flag to indicate neighbor cloud | optional |
| secure | Flag to indicate security | optional |
| authenticationInfo | Base64 encoded public key of the cloud | optional |
| gatekeeperRelayIds | List of gatekeeper relay IDs belonged to the cloud | optional |
| gatewayRelayIds | List of gateway relay IDs belonged to the cloud | optional |
