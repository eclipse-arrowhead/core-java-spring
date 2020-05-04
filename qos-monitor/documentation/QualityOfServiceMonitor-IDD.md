# QUALITY OF SERVICE MONITOR
### Interface Design Description

QoS-Monitor offers three types of endpoints. Client, Management and Private.

Swagger API documentation is available on: `https://<host>:<port>`
The base URL for the requests: `http://<host>:<port>/qos_monitor`

## Client Endpoint Description

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#endpoint_get_echo) | /echo | GET    | -    | OK     |

## Private Endpoint Description

todo

## Management Endpoint Description

todo

### Echo <a name="endpoint_get_echo"/>
```
GET /qos_monitor/echo
```

Returns a "Got it" message with the purpose of testing the core service availability.
