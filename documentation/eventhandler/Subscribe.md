### Subscribe 
```
POST /eventhandler/subscribe
```

Creates a subscription record specified by parameters.

<a name="datastructures_subscriptionrequest" />

__SubscriptionRequest__ is the input.

```json
{
  "eventType": "string",
  "filterMetaData": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "matchMetaData": true,
  "notifyUri": "string",
  "sources": [
    {
      "systemName": "string",
      "address": "string",
      "authenticationInfo": "string",
      "port": 0
    }
  ],
  "startDate": "string",
  "endDate": "string",
  "subscriberSystem": {
    "systemName": "string",
    "address": "string",
    "authenticationInfo": "string",
    "port": 0
  }
}
```
| __SubscriptionRequest__  fields |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `eventType` | Type of event to subscribe to | mandatory | max. length = 255 |
| `filterMetaData` | The recievied event has to contain all the "key - value" pairs defined here  | optional | max.length = 65535 |
| `matchMetaData` | A flag to turn on/off metadata filtering | mandatory |  true or false |
| `notifyUri` | Url subpath of the subscriber sytem's notification endpoint | mandatory | max.length = 65535 |
| `sources` | List of publisher systems | optional (if not defined or empty, all publishers will be able to send requests which are authorized and allowed by the other filtering options )| specified at system deployment time |
| `startDate` | If startDate is defined, the subscriber system will only receive events when the events timestamp is after startDate.  | optional ( StartDate must be after the current datetime. ) | UTC time in `yyyy-MM-dd`  `HH`:`mm`:`ss` format |
| `endDate` | If endDate is defined, the subscriber system will only receive events when the events timestamp is before endDate. | optional ( EndDate must be after the current datetime. If startDate is defined endDate must be after startDate. )|  UTC time in `yyyy-MM-dd`  `HH`:`mm`:`ss` format  |
| `subscriberSystem` | Details of subscriber system | mandatory | as in system |

| __System__  fields |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `systemName` | The name of the system. | mandatory | max. length = 255 |
| `address` |  Domain name or IP of the system. | mandatory | max. length = 255 |
| `authenticationInfo` | Public key of the system. | optional | single line string without the "-----BEGIN PUBLIC KEY-----" prefix  and the "-----END PUBLIC KEY-----" suffix |
| `port` | The port where the system provides it's services | mandatory | max.length = defined by local cloud operator ( default valid range: 1-65535 ) |
