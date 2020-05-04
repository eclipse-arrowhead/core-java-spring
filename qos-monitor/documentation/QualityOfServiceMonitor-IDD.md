# QUALITY OF SERVICE MONITOR
### Interface Design Description

QoS-Monitor offers three types of endpoints. Client, Management and Private.

Swagger API documentation is available on: `https://<host>:<port>`.

The base URL for the requests: `http://<host>:<port>/qos_monitor`.

## Client Endpoint Description

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#endpoint_get_echo) | /echo | GET    | -    | OK     |

## Private Endpoint Description

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Retrieve Intra-Cloud Ping Measurement](#endpoint_get_intra_ping) | /measurements/intracloud/ping/{id} | GET | id | Response DTO |

## Management Endpoint Description

todo

---

### Echo <a name="endpoint_get_echo"/>
```
GET /qos_monitor/echo
```

Returns a "Got it" message with the purpose of testing the core service availability.

### Retrieve Intra-Cloud Ping Measurement <a name="endpoint_get_intra_ping"/>
```
GET /qos_monitor/measurements/intracloud/ping/{id}
```

Returns the requested Intra-Cloud Ping Measurement entry by system id.

**Output:**

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
