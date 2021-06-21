### QUALITY OF SERVICE MONITOR <br/>Interface Design Description

---
# Security

This System can be secured via HTTPS. If it is started in secure mode (default behaviour), it verifies whether the Application System possesses a proper X.509 identity certificate and whether that certificate is Arrowhead compliant in its making. This certificate structure and creation guidelines ensure:
-	Requester system is properly bootstrapped into the Local Cloud
-	Requester system indeed belongs to this Local Cloud
-	Requester system has right to access to the specific endpoint

# Communication Profile

QoS-Monitor offers three types of endpoints. Client, Management and Private.

Swagger API documentation is available on: `https://<host>:<port>`.<br/>
The base URL for the requests: `http://<host>:<port>/qos_monitor`.

## Client Endpoint Description

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#endpoint_get_echo) | /echo | GET    | -    | OK     |
| [Notification](#endpoint_post_ping_event_notification) | /externalpingmonitorevent | POST    | [EventDTO](#input_post_ping_event_notification)    | OK     |

## Private Endpoint Description

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get Public Key](#endpoint_get_publickey) | /publickey | GET | - | [String](#output_get_publickey) |
| [Retrieve Intra-Cloud Ping Measurement](#endpoint_get_intra_ping) | /measurements/intracloud/ping/{id} | GET | [id](#input_get_intra_ping) | [Response DTO](#output_get_intra_ping) |
| [Calculate Intra-Cloud Ping Median Measurement](#endpoint_get_intra_median_ping) | /measurements/intracloud/ping_median/{attribute} | GET | [attribute](#input_get_intra_median_ping) | [Response DTO](#output_get_intra_median_ping) |
| [Retrieve Inter-Cloud Direct Ping Measurement](#endpoint_post_inter_direct_ping) | /measurements/intercloud/ping | POST | [Request DTO](#input_post_inter_direct_ping) | [Response DTO](#output_post_inter_direct_ping) |
| [Retrieve Inter-Cloud Relay Echo Measurement](#endpoint_post_inter_relay_echo) | /measurements/intercloud/relay_echo | POST | [Request DTO](#input_post_inter_relay_echo) | [Response DTO](#output_post_inter_relay_echo) |
| [Init Relay Test](#endpoint_post_init_relay_test) | /init_relay_test | POST | [Request DTO](#input_post_init_relay_test) | [Created status](#output_post_init_relay_test) |
| [Join Relay Test](#endpoint_post_join_relay_test) | /join_relay_test | POST | [Request DTO](#input_post_join_relay_test) | [Response DTO](#output_post_join_relay_test) |

## Management Endpoint Description

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get Intra-Cloud Ping Measurements](#endpoint_mgmt_get_intra_ping) | /mgmt/measurements/intracloud/ping | GET | [Page params](#input_mgmt_get_intra_ping) | [Response DTO](#output_mgmt_get_intra_ping) |
| [Get Intra-Cloud Ping Measurements By System ID](#endpoint_mgmt_get_intra_ping_by_system_id) | /mgmt/measurements/intracloud/ping/{id} | GET | [id](#input_mgmt_get_intra_ping_by_system_id) | [Response DTO](#output_mgmt_get_intra_ping_by_system_id) |
| [Get Inter-Cloud Direct Ping Measurements](#endpoint_mgmt_get_inter_direct_ping) | /mgmt/measurements/intercloud/ping | GET | [Page params](#input_mgmt_get_inter_direct_ping) | [Response DTO](#output_mgmt_get_inter_direct_ping) |
| [Get Inter-Cloud Direct Ping Measurements By Cloud-System Pair](#endpoint_mgmt_post_inter_direct_ping_by_cloud_system) | /mgmt/measurements/intercloud/ping/pair_results | POST | [Request DTO](#input_mgmt_post_inter_direct_ping_by_cloud_system) | [Response DTO](#output_mgmt_post_inter_direct_ping_by_cloud_system) |
| [Get Inter-Cloud Relay Echo Measurements](#endpoint_mgmt_get_inter_relay_echo) | /mgmt/measurements/intercloud/relay_echo | GET | [Page params](#input_mgmt_get_inter_relay_echo) | [Response DTO](#output_mgmt_get_inter_relay_echo) |
| [Get Inter-Cloud Relay Echo Measurements By Cloud-Relay Pair](#endpoint_mgmt_post_inter_relay_echo_by_cloud_relay) | /mgmt/measurements/intercloud/relay_echo/pair_results | POST | [Request DTO](#input_mgmt_post_inter_relay_echo_by_cloud_relay) | [Response DTO](#output_mgmt_post_inter_relay_echo_by_cloud_relay) |
| [Get Inter-Cloud Relay Echo Best Measurements By Cloud](#endpoint_mgmt_post_inter_relay_echo_best_by_cloud) | /mgmt/measurements/intercloud/relay_echo/best_relay | POST | [Request DTO](#input_mgmt_post_inter_relay_echo_best_by_cloud) | [Response DTO](#output_mgmt_post_inter_relay_echo_best_by_cloud) |

---

### Echo <a name="endpoint_get_echo"/>
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

System ID of the intra-cloud ping measurement as a path variable.

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
POST /qos_monitor/measurements/intercloud/ping
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
| id | ID of the inter-cloud direct ping measurement |
| measurement.id | ID of the inter-cloud measurement |
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
POST /qos_monitor/measurements/intercloud/relay_echo
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
  "authenticationInfo": "string",
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

**Output:** <a name="output_post_inter_relay_echo"/>

```json
{
  "count": 0,
  "data": [
    {
      "id": 0,      
      "measurement": {
	"id": 0,
	"measurementType": "RELAY_ECHO",
	"status": "NEW",
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
        "relay": {
          "id": 0,
          "address": "string",
	  "port": 0,
          "type": "GATEWAY_RELAY",
          "exclusive": true,          
          "secure": true,          
	  "createdAt": "string",
          "updatedAt": "string"
        },        
        "lastMeasurementAt": "2020-05-05T08:23:47.774Z",
        "createdAt": "2020-05-05T08:23:47.774Z",   
        "updatedAt": "2020-05-05T08:23:47.774Z"
      },
      "minResponseTime": 0,
      "maxResponseTime": 0,
      "meanResponseTimeWithTimeout": 0,
      "meanResponseTimeWithoutTimeout": 0,
      "jitterWithTimeout": 0,
      "jitterWithoutTimeout": 0,      
      "lostPerMeasurementPercent": 0, 
      "received": 0,
      "receivedAll": 0,
      "sent": 0,
      "sentAll": 0,
      "lastAccessAt": "2020-05-05T08:23:47.774Z",
      "countStartedAt": "2020-05-05T08:23:47.774Z",
      "createdAt": "2020-05-05T08:23:47.774Z",
      "updatedAt": "2020-05-05T08:23:47.774Z"
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| count | Number of total elements |
| id | ID of the inter-cloud relay echo measurement |
| measurement.id | ID of the inter-cloud measurement |
| measurement.measurementType | Type of the measurement |
| measurement.status | Status of the measurement |
| measurement.cloud.id | ID of the measured cloud |
| measurement.cloud.operator | Operator of the measured cloud |
| measurement.cloud.name | Name of the measured cloud |
| measurement.cloud.owncloud | Flag to indicate own cloud (meant to be false) |
| measurement.cloud.neighbor | Flag to indicate neighbor cloud |
| measurement.cloud.secure | Flag to indicate security |
| measurement.cloud.authenticationInfo | Base64 encoded public key of the measured cloud |
| measurement.cloud.createdAt | Date of creation of the measured cloud |
| measurement.cloud.updatedAt | Date of update of the measured cloud |
| measurement.relay.id | ID of the measured relay |
| measurement.relay.address | Address of the measured relay |
| measurement.relay.port | Port of the measured relay |
| measurement.relay.type | Type of the measured relay |
| measurement.relay.exclusive | Flag to indicate exclusivity |
| measurement.relay.secure | Flag to indicate security |
| measurement.relay.createdAt | Date of creation of the measured relay |
| measurement.relay.updatedAt | Date of update of the measured relay |
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

### Init Relay Test <a name="endpoint_post_init_relay_test"/>
```
POST /qos_monitor/init_relay_test
```

Creates message queues for testing the connection between this cloud and the target cloud through the given relay.

**Input:**  <a name="input_post_init_relay_test"/>

```json
{
  "queueId": "string",
  "peerName": "string",
  "receiverQoSMonitorPublicKey": "string",
  "targetCloud": {
    "operator": "string",
    "name": "string",
    "neighbor": true,    
    "secure": true,
    "authenticationInfo": "string",
    "gatekeeperRelayIds": [
      0
    ],
    "gatewayRelayIds": [
      0
    ]
  },
  "relay": {
    "address": "string",
    "port": 0,
    "type": "string",
    "exclusive": true,    
    "secure": true
  }
}
```

| Field | Description | Necessity |
| ----- | ----------- | --------- |
| queueId | ID of the created message queue in the Relay | mandatory |
| peerName | Server Common Name of the target cloud's QoS Monitor | mandatory |
| receiverQoSMonitorPublicKey | Base64 encoded public key of the target cloud's QoS Monitor | mandatory |
| targetCloud.operator | Operator of the cloud | mandatory |
| targetCloud.name | Name of the cloud | mandatory |
| targetCloud.neighbor | Flag to indicate neighbor cloud | optional |
| targetCloud.secure | Flag to indicate security | optional |
| targetCloud.authenticationInfo | Base64 encoded public key of the cloud | optional |
| targetCloud.gatekeeperRelayIds | List of gatekeeper relay IDs belonged to the cloud | optional |
| targetCloud.gatewayRelayIds | List of gateway relay IDs belonged to the cloud | optional |
| relay.address | Address of the measured relay | mandatory |
| relay.port | Port of the relay being measured | mandatory |
| relay.type | Type of the relay being measured | mandatory |
| relay.exclusive | Flag to indicate exclusivity | optional |
| relay.secure | Flag to indicate security | optional |

**Output:**  <a name="output_post_init_relay_test"/>

Http Created(201) status.

### Join Relay Test <a name="endpoint_post_join_relay_test"/>
```
POST /qos_monitor/join_relay_test
```

Creates message queues for testing the connection between this cloud and the requester cloud through the given relay and return the necessary connection informations.

**Input:**  <a name="input_post_join_relay_test"/>

```json
{
  "senderQoSMonitorPublicKey": "string",
  "requesterCloud": {
    "operator": "string",
    "name": "string",
    "neighbor": true,    
    "secure": true,
    "authenticationInfo": "string",
    "gatekeeperRelayIds": [
      0
    ],
    "gatewayRelayIds": [
      0
    ]
  },
  "targetCloud": {
    "operator": "string",
    "name": "string",
    "neighbor": true,    
    "secure": true,
    "authenticationInfo": "string",
    "gatekeeperRelayIds": [
      0
    ],
    "gatewayRelayIds": [
      0
    ]
  },
  "relay": {
    "address": "string",
    "port": 0,
    "type": "string",
    "exclusive": true,    
    "secure": true
  }
}
```

| Field | Description | Necessity |
| ----- | ----------- | --------- |
| senderQoSMonitorPublicKey | Base64 encoded public key of the requester cloud's QoS Monitor | mandatory |
| requesterCloud.operator | Operator of the cloud | mandatory |
| requesterCloud.name | Name of the cloud | mandatory |
| requesterCloud.neighbor | Flag to indicate neighbor cloud | optional |
| requesterCloud.secure | Flag to indicate security | optional |
| requesterCloud.authenticationInfo | Base64 encoded public key of the cloud | optional |
| requesterCloud.gatekeeperRelayIds | List of gatekeeper relay IDs belonged to the cloud | optional |
| requesterCloud.gatewayRelayIds | List of gateway relay IDs belonged to the cloud | optional |
| targetCloud.operator | Operator of the cloud | mandatory |
| targetCloud.name | Name of the cloud | mandatory |
| targetCloud.neighbor | Flag to indicate neighbor cloud | optional |
| targetCloud.secure | Flag to indicate security | optional |
| targetCloud.authenticationInfo | Base64 encoded public key of the cloud | optional |
| targetCloud.gatekeeperRelayIds | List of gatekeeper relay IDs belonged to the cloud | optional |
| targetCloud.gatewayRelayIds | List of gateway relay IDs belonged to the cloud | optional |
| relay.address | Address of the measured relay | mandatory |
| relay.port | Port of the relay being measured | mandatory |
| relay.type | Type of the relay being measured | mandatory |
| relay.exclusive | Flag to indicate exclusivity | optional |
| relay.secure | Flag to indicate security | optional |

**Output:**  <a name="output_post_join_relay_test"/>

```json
{
  "queueId": "string",
  "peerName": "string",
  "receiverQoSMonitorPublicKey": "string"
}
```

| Field | Description |
| ----- | ----------- |
| queueId | ID of the created message queue in the Relay |
| peerName | Server Common Name of the target cloud's QoS Monitor |
| receiverQoSMonitorPublicKey | Base64 encoded public key of the target cloud's QoS Monitor |

### Get Intra-Cloud Ping Measurements <a name="endpoint_mgmt_get_intra_ping"/>
```
GET /qosmonitor/mgmt/measurements/intracloud/ping
```

Return requested Ping-Measurements entries by the given page parameters.

**Input:**  <a name="input_mgmt_get_intra_ping"/>

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| page | zero based page index | no |
| item_per_page | maximum number of items returned | no |
| sort_field | sorts by the given column | no |
| direction | direction of sorting | no |

> **Note:** Default value for `sort_field` is `id`. All possible values are: 
> * `id`
> * `createdAt`
> * `updatedAt`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC` 

**Output:**  <a name="output_mgmt_get_intra_ping"/>

```json
{
  "count": 0,
  "data": [
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
  ]
}
```

| Field | Description |
| ----- | ----------- |
| count | Number of total elements |
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

### Get Intra-Cloud Ping Measurements By System ID <a name="endpoint_mgmt_get_intra_ping_by_system_id"/>
```
GET /qosmonitor/mgmt/measurements/intracloud/ping/{id}
```

Return requested Ping-Measurement entry by system id.

**Input:** <a name="input_mgmt_get_intra_ping_by_system_id"/>

System ID of the intra-cloud ping measurement as a path variable.

**Output:** <a name="output_mgmt_get_intra_ping_by_system_id"/>

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

### Get Inter-Cloud Direct Ping Measurements <a name="endpoint_mgmt_get_inter_direct_ping"/>
```
GET /qosmonitor/mgmt/measurements/intercloud/ping
```

Returns requested Inter-Cloud direct ping measurements entries by the given page parameters.

**Input:**  <a name="input_mgmt_get_inter_direct_ping"/>

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| page | zero based page index | no |
| item_per_page | maximum number of items returned | no |
| sort_field | sorts by the given column | no |
| direction | direction of sorting | no |

> **Note:** Default value for `sort_field` is `id`. All possible values are: 
> * `id`
> * `createdAt`
> * `updatedAt`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC` 

**Output:**  <a name="output_mgmt_get_inter_direct_ping"/>

```json
{
  "count": 0,
  "data": [
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
  ]
}
```

| Field | Description |
| ----- | ----------- |
| count | Number of total elements |
| id | ID of the inter-cloud direct ping measurement |
| measurement.id | ID of the inter-cloud measurement |
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

### Get Inter-Cloud Direct Ping Measurements By Cloud-System Pair <a name="endpoint_mgmt_post_inter_direct_ping_by_cloud_system"/> 
```
POST /qosmonitor/mgmt/measurements/intercloud/ping/pair_results
```

Returns requested Inter-Cloud direct ping measurement entry by cloud and system.

**Input:**  <a name="input_mgmt_post_inter_direct_ping_by_cloud_system"/>

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

**Output:** <a name="output_mgmt_post_inter_direct_ping_by_cloud_system"/>

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
| id | ID of the inter-cloud direct ping measurement |
| measurement.id | ID of the inter-cloud measurement |
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

### Get Inter-Cloud Relay Echo Measurements <a name="endpoint_mgmt_get_inter_relay_echo"/>
```
GET /qosmonitor/mgmt/measurements/intercloud/relay_echo
```

Returns requested Inter-Cloud Relay-Echo measurments entries by the given parameters.

**Input:**  <a name="input_mgmt_get_inter_relay_echo"/>

Query params:

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| page | zero based page index | no |
| item_per_page | maximum number of items returned | no |
| sort_field | sorts by the given column | no |
| direction | direction of sorting | no |

> **Note:** Default value for `sort_field` is `id`. All possible values are: 
> * `id`
> * `createdAt`
> * `updatedAt`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC` 

**Output:**  <a name="output_mgmt_get_inter_relay_echo"/>

```json
{
  "count": 0,
  "data": [
    {
      "id": 0,      
      "measurement": {
	"id": 0,
	"measurementType": "RELAY_ECHO",
	"status": "NEW",
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
        "relay": {
          "id": 0,
          "address": "string",
	  "port": 0,
          "type": "GATEWAY_RELAY",
          "exclusive": true,          
          "secure": true,          
	  "createdAt": "string",
          "updatedAt": "string"
        },        
        "lastMeasurementAt": "2020-05-05T08:23:47.774Z",
        "createdAt": "2020-05-05T08:23:47.774Z",   
        "updatedAt": "2020-05-05T08:23:47.774Z"
      },
      "minResponseTime": 0,
      "maxResponseTime": 0,
      "meanResponseTimeWithTimeout": 0,
      "meanResponseTimeWithoutTimeout": 0,
      "jitterWithTimeout": 0,
      "jitterWithoutTimeout": 0,      
      "lostPerMeasurementPercent": 0, 
      "received": 0,
      "receivedAll": 0,
      "sent": 0,
      "sentAll": 0,
      "lastAccessAt": "2020-05-05T08:23:47.774Z",
      "countStartedAt": "2020-05-05T08:23:47.774Z",
      "createdAt": "2020-05-05T08:23:47.774Z",
      "updatedAt": "2020-05-05T08:23:47.774Z"
    }
  ]
}
```

| Field | Description |
| ----- | ----------- |
| count | Number of total elements |
| id | ID of the inter-cloud relay echo measurement |
| measurement.id | ID of the inter-cloud measurement |
| measurement.measurementType | Type of the measurement |
| measurement.status | Status of the measurement |
| measurement.cloud.id | ID of the measured cloud |
| measurement.cloud.operator | Operator of the measured cloud |
| measurement.cloud.name | Name of the measured cloud |
| measurement.cloud.owncloud | Flag to indicate own cloud (meant to be false) |
| measurement.cloud.neighbor | Flag to indicate neighbor cloud |
| measurement.cloud.secure | Flag to indicate security |
| measurement.cloud.authenticationInfo | Base64 encoded public key of the measured cloud |
| measurement.cloud.createdAt | Date of creation of the measured cloud |
| measurement.cloud.updatedAt | Date of update of the measured cloud |
| measurement.relay.id | ID of the measured relay |
| measurement.relay.address | Address of the measured relay |
| measurement.relay.port | Port of the measured relay |
| measurement.relay.type | Type of the measured relay |
| measurement.relay.exclusive | Flag to indicate exclusivity |
| measurement.relay.secure | Flag to indicate security |
| measurement.relay.createdAt | Date of creation of the measured relay |
| measurement.relay.updatedAt | Date of update of the measured relay |
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

### Get Inter-Cloud Relay Echo Measurements By Cloud-Relay Pair <a name="endpoint_mgmt_post_inter_relay_echo_by_cloud_relay"/>
```
POST /qosmonitor/mgmt/measurements/intercloud/relay_echo/pair_results
```

Returns requested Inter-Cloud Relay-Echo measurment entry by cloud and relay.

**Input:**  <a name="input_mgmt_post_inter_relay_echo_by_cloud_relay"/>

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
  "relay": {
    "id": 0,
    "address": "string",
    "port": 0,
    "type": "GATEWAY_RELAY",
    "exclusive": true,    
    "secure": true,
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
| relay.id | ID of the relay | mandatory |
| relay.address | Address of the relay | mandatory |
| relay.port | Port of the relay | mandatory |
| relay.type | Type of the relay | optional |
| relay.exclusive | Flag to indicate exclusivity | optional |
| relay.secure | Flag to indicate security | optional |
| relay.createdAt | Date of creation of the relay | optional |
| relay.updatedAt | Date of update of the relay | optional |

**Output:** <a name="output_mgmt_post_inter_relay_echo_by_cloud_relay"/>

```json
{
  "id": 0,      
  "measurement": {
    "id": 0,
    "measurementType": "RELAY_ECHO",
    "status": "NEW",
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
    "relay": {
      "id": 0,
      "address": "string",
      "port": 0,
      "type": "GATEWAY_RELAY",
      "exclusive": true,          
      "secure": true,          
      "createdAt": "string",
      "updatedAt": "string"
    },        
    "lastMeasurementAt": "2020-05-05T08:23:47.774Z",
    "createdAt": "2020-05-05T08:23:47.774Z",   
    "updatedAt": "2020-05-05T08:23:47.774Z"
  },
  "minResponseTime": 0,
  "maxResponseTime": 0,
  "meanResponseTimeWithTimeout": 0,
  "meanResponseTimeWithoutTimeout": 0,
  "jitterWithTimeout": 0,
  "jitterWithoutTimeout": 0,      
  "lostPerMeasurementPercent": 0, 
  "received": 0,
  "receivedAll": 0,
  "sent": 0,
  "sentAll": 0,
  "lastAccessAt": "2020-05-05T08:23:47.774Z",
  "countStartedAt": "2020-05-05T08:23:47.774Z",
  "createdAt": "2020-05-05T08:23:47.774Z",
  "updatedAt": "2020-05-05T08:23:47.774Z"
}
```

| Field | Description |
| ----- | ----------- |
| id | ID of the inter-cloud relay echo measurement |
| measurement.id | ID of the inter-cloud measurement |
| measurement.measurementType | Type of the measurement |
| measurement.status | Status of the measurement |
| measurement.cloud.id | ID of the measured cloud |
| measurement.cloud.operator | Operator of the measured cloud |
| measurement.cloud.name | Name of the measured cloud |
| measurement.cloud.owncloud | Flag to indicate own cloud (meant to be false) |
| measurement.cloud.neighbor | Flag to indicate neighbor cloud |
| measurement.cloud.secure | Flag to indicate security |
| measurement.cloud.authenticationInfo | Base64 encoded public key of the measured cloud |
| measurement.cloud.createdAt | Date of creation of the measured cloud |
| measurement.cloud.updatedAt | Date of update of the measured cloud |
| measurement.relay.id | ID of the measured relay |
| measurement.relay.address | Address of the measured relay |
| measurement.relay.port | Port of the measured relay |
| measurement.relay.type | Type of the measured relay |
| measurement.relay.exclusive | Flag to indicate exclusivity |
| measurement.relay.secure | Flag to indicate security |
| measurement.relay.createdAt | Date of creation of the measured relay |
| measurement.relay.updatedAt | Date of update of the measured relay |
| measurement.lastMeasurementAt | Time of the last measurement |
| measurement.createdAt | Date of creation of the measurement |
| measurement.updatedAt | Date of update of the measurement |
| available | Boolean value of the systems calculated availability|
| minResponseTime | Integer value of milliseconds of the fastest returned ping|
| maxResponseTime | Integer value of milliseconds of the slowest returned ping|
| meanResponseTimeWithTimeout | Integer value of milliseconds of the calculated average of pings including timeouts|

### Get Inter-Cloud Relay Echo Best Measurements By Cloud <a name="endpoint_mgmt_post_inter_relay_echo_best_by_cloud"/>
```
POST /qosmonitor/mgmt/measurements/intercloud/relay_echo/best_relay
```

Returns best Inter-Cloud Relay-Echo measurment entry by cloud and attribute.

**Input:**  <a name="input_mgmt_post_inter_relay_echo_best_by_cloud"/>

```json
{
  "attribute": "string",
  "cloud" : {
    "id": 0,
    "operator": "string",
    "name": "string",        
    "ownCloud": false,
    "neighbor": true,
    "secure": true,
    "authenticationInfo": "string",
    "createdAt": "string",
    "updatedAt": "string"
  }
}
```

| Field | Description | Necessity |
| ----- | ----------- | --------- |
| attribute | Attribute of the measurement for the ranking | mandatory |
| cloud.id | ID of the cloud | mandatory |
| cloud.operator | Operator of the cloud | mandatory |
| cloud.name | Name of the cloud | mandatory |
| cloud.owncloud | Flag to indicate own cloud (meant to be false) | optional |
| cloud.neighbor | Flag to indicate neighbor cloud | optional |
| cloud.secure | Flag to indicate security | optional |
| cloud.authenticationInfo | Base64 encoded public key of the cloud | optional |
| cloud.gatekeeperRelayIds | List of gatekeeper relay IDs belonged to the cloud | optional |
| cloud.gatewayRelayIds | List of gateway relay IDs belonged to the cloud | optional |
| cloud.createdAt | Date of creation of the cloud | optional |
| cloud.updatedAt | Date of update of the cloud | optional |

**Output:** <a name="output_mgmt_post_inter_relay_echo_best_by_cloud"/>

```json
{
  "id": 0,      
  "measurement": {
    "id": 0,
    "measurementType": "RELAY_ECHO",
    "status": "NEW",
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
    "relay": {
      "id": 0,
      "address": "string",
      "port": 0,
      "type": "GATEWAY_RELAY",
      "exclusive": true,          
      "secure": true,          
      "createdAt": "string",
      "updatedAt": "string"
    },        
    "lastMeasurementAt": "2020-05-05T08:23:47.774Z",
    "createdAt": "2020-05-05T08:23:47.774Z",   
    "updatedAt": "2020-05-05T08:23:47.774Z"
  },
  "minResponseTime": 0,
  "maxResponseTime": 0,
  "meanResponseTimeWithTimeout": 0,
  "meanResponseTimeWithoutTimeout": 0,
  "jitterWithTimeout": 0,
  "jitterWithoutTimeout": 0,      
  "lostPerMeasurementPercent": 0, 
  "received": 0,
  "receivedAll": 0,
  "sent": 0,
  "sentAll": 0,
  "lastAccessAt": "2020-05-05T08:23:47.774Z",
  "countStartedAt": "2020-05-05T08:23:47.774Z",
  "createdAt": "2020-05-05T08:23:47.774Z",
  "updatedAt": "2020-05-05T08:23:47.774Z"
}
```

| Field | Description |
| ----- | ----------- |
| id | ID of the inter-cloud relay echo measurement |
| measurement.id | ID of the inter-cloud measurement |
| measurement.measurementType | Type of the measurement |
| measurement.status | Status of the measurement |
| measurement.cloud.id | ID of the measured cloud |
| measurement.cloud.operator | Operator of the measured cloud |
| measurement.cloud.name | Name of the measured cloud |
| measurement.cloud.owncloud | Flag to indicate own cloud (meant to be false) |
| measurement.cloud.neighbor | Flag to indicate neighbor cloud |
| measurement.cloud.secure | Flag to indicate security |
| measurement.cloud.authenticationInfo | Base64 encoded public key of the measured cloud |
| measurement.cloud.createdAt | Date of creation of the measured cloud |
| measurement.cloud.updatedAt | Date of update of the measured cloud |
| measurement.relay.id | ID of the measured relay |
| measurement.relay.address | Address of the measured relay |
| measurement.relay.port | Port of the measured relay |
| measurement.relay.type | Type of the measured relay |
| measurement.relay.exclusive | Flag to indicate exclusivity |
| measurement.relay.secure | Flag to indicate security |
| measurement.relay.createdAt | Date of creation of the measured relay |
| measurement.relay.updatedAt | Date of update of the measured relay |
| measurement.lastMeasurementAt | Time of the last measurement |
| measurement.createdAt | Date of creation of the measurement |
| measurement.updatedAt | Date of update of the measurement |
| available | Boolean value of the systems calculated availability|
| minResponseTime | Integer value of milliseconds of the fastest returned ping|
| maxResponseTime | Integer value of milliseconds of the slowest returned ping|
| meanResponseTimeWithTimeout | Integer value of milliseconds of the calculated average of pings including timeouts|
