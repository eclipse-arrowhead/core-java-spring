### Publish
```
GET /eventhandler/publish
```

Start the publishing process to deliver the event to the subscribers.

<a name="eventhandler_endpoints_publish" />

__PublishRequest__ is the input:

```json
{
  "eventType": "string",
  "metaData": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "payload": "string",
  "source": {
    "address": "string",
    "authenticationInfo": "string",
    "port": 0,
    "systemName": "string"
  },
  "timeStamp": "string"
}
```

| __PublishRequest__  fields |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `eventType` | Type of event. | mandatory | max. length = 255 |
| `metaData` |  The "key - value" pairs for event filtering. | optional | max.length = 65535 |
| `payload` | String representation of the event. | mandatory | specified at system deployment time |
| `source` |   Details of the publisher system. | mandatory | as in system |
| `timestamp` | The time of publishing  | mandatory | UTC time in `yyyy-MM-dd`  `HH`:`mm`:`ss` format |

| __System__  fields |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `systemName` | The name of the system. | mandatory | max. length = 255 |
| `address` |  Domain name or IP of the system. | mandatory | max. length = 255 |
| `authenticationInfo` | Public key of the system. | optional | single line string without the "-----BEGIN PUBLIC KEY-----" prefix  and the "-----END PUBLIC KEY-----" suffix |
| `port` | The port where the system provides services | mandatory | max.length = defined by local cloud operator ( default valid range: 1-65535 ) |
