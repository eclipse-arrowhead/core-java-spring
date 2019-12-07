### Unsubscribe 
```
DELETE /eventhandler/unsubscribe
```
Removes the subscription record specified by parameters.

| __Unsubscribe__  query parameters |
| ------------------------------------------------------- |
| Parameter | Description | Necessity | Format/Limitations |
| --------- | ----------- | --------- | ----------- |
| `event_type` | Type of event to subscribe to | mandatory | max. length = 255 |
| `system_name` | The name of the system. | mandatory | max. length = 255 |
| `address` |  Domain name or Ip of the system. | mandatory | max. length = 255 |
| `port` | The port where the system servs it's services | mandatory | max.length = difined by local cloud operator ( default valid range: 1-65535 ) |