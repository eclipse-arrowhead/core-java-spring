### Unsubscribe 
```
DELETE /eventhandler/unsubscribe
```
Removes the subscription record specified by parameters.

<a name="datastructures_eventhandlerunsubscriberequest" />

__Unsubscribe query parameters__ are the input :
`https://eventhandler_ip:unsubscribe_port/eventhandler/unsubscribe?address=`192.168.0.1`&event_type=`EVENT_TYPE_1`&port=`9009`&system_name=`test_consumer

| __Unsubscribe__  query parameters |
| ------------------------------------------------------- |

| Parameter | Description | Necessity | Format/Limitations |
| --------- | ----------- | --------- | ----------- |
| `address` |  Domain name or IP of the system. | mandatory | max. length = 255 |
| `event_type` | Type of event to subscribe to | mandatory | max. length = 255 |
| `port` | The port where the system provides it's services | mandatory | max.length = defined by local cloud operator ( default valid range: 1-65535 ) |
| `system_name` | The name of the system. | mandatory | max. length = 255 |
