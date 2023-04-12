# Event Handler

<a name="event_handler_sdd" />

## System Design Overview
![Alt text](/documentation/eventhandler/sysd/event_handler_controller.jpg)

<a name="event_handler_provided_services" />

## Provided services

The Event Handler provides the following services:
* [Echo](#eventhandler_endpoints_get_echo)
* [Publish](#eventhandler_endpoints_post_publish)
* [Subscribe](#eventhandler_endpoints_post_subscribe)
* [Unsubscribe](#eventhandler_endpoints_delete_unsubscribe)
* [AuthUpdate](#eventhandler_endpoints_post_auth_update)

<a name="event_handler_consumed_services" />

## Consumed services

The Event Handler consumes the following services:
* CheckAuthorizationSubscription private service from the Authorization core system
* Notification service from the subscriber client system

<a name="event_handler_usecases" />

## Use cases

The Event Handler has the following use cases:
* [Publish Event](documentation/eventhandler/use_cases/EH_use_case_1.md)
![Alt text](/documentation/eventhandler/use_cases/PublishEvent.png)
* [Register Subscription](documentation/eventhandler/use_cases/EH_use_case_2.md)
![Alt text](/documentation/eventhandler/use_cases/RegisterSubscription.png)
* [Unregister Subscription](documentation/eventhandler/use_cases/EH_use_case_3.md)
![Alt text](/documentation/eventhandler/use_cases/Unsubscribe.png)
* [Update Authorization](documentation/eventhandler/use_cases/EH_use_case_4.md)
![Alt text](/documentation/authorization/SubscriptionAuthUpdate.png)
![Alt text](/documentation/eventhandler/use_cases/PublishAuthUpdate.png)

<a name="event_handler_endpoints" />

## Endpoints

<a name="event_handler_endpoints_client" />

### Client endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#eventhandler_endpoints_get_echo) | /echo | GET    | -    | OK     |
| [Subscribe](#eventhandler_endpoints_post_subscribe) | /subscribe | POST    | -    | OK     |
| [Unsubscribe](#eventhandler_endpoints_delete_unsubscribe) | /unsubscribe | DELETE    | -    | OK     |
| [Publish](#eventhandler_endpoints_post_publish) | /publish | POST    | -    | OK     |

<a name="event_handler_endpoints_management" />

### Management endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get subscriptions](#eventhandler_endpoints_get_subscription_list) | /mgmt/subscriptions | GET | direction && item_per_page && page && sort_field | [Subscription list response](#eventhandler_subscription_list_response) |
| [Get subscription by id](#eventhandler_endpoints_get_subscription) | /mgmt/subscriptions/{id} | GET | id | [Subscription response](#eventhandler_subscription_response) |
| [Update subscription](#eventhandler_endpoints_put_subscription) | /mgmt/subscriptions/{id} | PUT | id && Subscription request| Subscription response |
| [Delete subscription](#eventhandler_endpoints_delete_subscription) | /mgmt/subscriptions/{id} | DELETE | id | OK |

<a name="event_handler_endpoints_private" />

### Private endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [AuthorizationUpdate](#eventhandler_endpoints_post_auth_update) | /publish/authupdate | POST    | -    | OK     |

<a name="eventhandler_endpoints_get_echo" />

### Echo 
```
GET /eventhandler/echo
```

Returns a "Got it!" message with the purpose of testing the core service availability.

<a name="eventhandler_endpoints_post_subscribe" />

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
| `filterMetaData` | The received event has to contain all the "key - value" pairs defined here  | optional | max.length = 65535 |
| `matchMetaData` | A flag to turn on/off metadata filtering | mandatory |  true or false |
| `notifyUri` | Url subpath of the subscriber system's notification endpoint | mandatory | max.length = 65535 |
| `sources` | List of publisher systems | optional (if not defined or empty, all publishers will be able to send requests which are authorized and allowed by the other filtering options )| not defined |
| `startDate` | If startDate is defined, the subscriber system will only receive events when the event's timestamp is after startDate.  | optional ( StartDate must be after the current date/time. ) | UTC time in `yyyy-MM-dd`  `HH`:`mm`:`ss` format |
| `endDate` | If endDate is defined, the subscriber system will only receive events when the event's timestamp is before endDate. | optional ( EndDate must be after the current date/time. If startDate is defined endDate must be after startDate. )|  UTC time in `yyyy-MM-dd`  `HH`:`mm`:`ss` format  |
| `subscriberSystem` | Details of subscriber system | mandatory | as in system |

| __System__  fields |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `systemName` | The name of the system. | mandatory | max. length = 255 |
| `address` |  Domain name or IP of the system. | mandatory | max. length = 255 |
| `authenticationInfo` | Public key of the system. | optional | single line string without the "-----BEGIN PUBLIC KEY-----" prefix and the "-----END PUBLIC KEY-----" suffix |
| `port` | The port where the system provides services | mandatory | max.length = defined by local cloud operator ( default valid range: 1-65535 ) |

<a name="eventhandler_subscription_response" />

__SubscriptionRequest__ output:

```json

{
        "id": 0,
        "eventType": {
        "eventTypeName": "string",
        "createdAt": "string",
        "updatedAt": "string"
      },
      "filterMetaData": {
        "additionalProp1": "string",
        "additionalProp2": "string",
        "additionalProp3": "string"
      },
      "id": 0,
      "matchMetaData": true,
      "notifyUri": "string",
      "sources": [
        {
          "id": 0,
          "systemName": "string",
          "address": "string",
          "authenticationInfo": "string",
          "port": 0,
          "createdAt": "string",
          "updatedAt": "string"
        }
      ],
      "startDate": "string",
      "endDate": "string",
      "subscriberSystem": {
        "id": 0,
        "systemName": "string",
        "address": "string",
        "authenticationInfo": "string",
        "port": 0,
        "createdAt": "string",
        "updatedAt": "string"
      },
      "createdAt": "string",
      "updatedAt": "string"
    }

```

<a name="eventhandler_endpoints_delete_unsubscribe" />

### Unsubscribe 
```
DELETE /eventhandler/unsubscribe
```
Removes the subscription record specified by parameters.

<a name="datastructures_eventhandlerunsubscriberequest" />

__Unsubscribe query parameters__ are the input :
`https://eventhandler_ip:unsubscribe_port/eventhandler/unsubscribe?address=`192.168.0.1`&event_type=`EVENT_TYPE_1`&port=`9009`&system_name=`test_consumer`

| __Unsubscribe__  query parameters |
| ------------------------------------------------------- |

| Parameter | Description | Necessity | Format/Limitations |
| --------- | ----------- | --------- | ----------- |
| `address` |  Domain name or IP of the system. | mandatory | max. length = 255 |
| `event_type` | Type of event to subscribe to | mandatory | max. length = 255 |
| `port` | The port where the system provides services | mandatory | max.length = defined by local cloud operator ( default valid range: 1-65535 ) |
| `system_name` | The name of the system. | mandatory | max. length = 255 |

<a name="eventhandler_endpoints_post_publish" />

### Publish
```
POST /eventhandler/publish
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
| `payload` | String representation of the event. | mandatory | not defined |
| `source` |   Details of the publisher system. | mandatory | as in system |
| `timestamp` | The time of publishing  | mandatory | UTC time in `yyyy-MM-dd`  `HH`:`mm`:`ss` format |

| __System__  fields |
| ------------------------------------------------------- |

| Field | Description | Necessity | Format/Limitations |
| ----- | ----------- | --------- | ----------- |
| `systemName` | The name of the system. | mandatory | max. length = 255 |
| `address` |  Domain name or IP of the system. | mandatory | max. length = 255 |
| `authenticationInfo` | Public key of the system. | optional | single line string without the "-----BEGIN PUBLIC KEY-----" prefix and the "-----END PUBLIC KEY-----" suffix |
| `port` | The port where the system provides services | mandatory | max.length = defined by local cloud operator ( default valid range: 1-65535 ) |

<a name="eventhandler_endpoints_get_subscription_list" />

### Get subscriptions

```
GET /mgmt/eventhandler/subscriptions
```


__Get subscriptions query parameters__  the input :

`https://eventhandler_ip:eventhandler_port/eventhandler/mgmt/subscriptions?dirction=`ASC`&item_per_page=`100`&page=`0`&sort_field=`id

| __Get subscriptions__  query parameters |
| ------------------------------------------------------- |

| Parameter | Description | Necessity | Format/Limitations |
| --------- | ----------- | --------- | ----------- |
| `direction` |  Direction of sorting. | optional | valid values: "ASC", "DESC" - default: "ASC"|
| `item_per_page` | Maximum number of items returned. | optional (mandatory, if page is defined)| integer |
| `page` | Zero based page index. | optional (mandatory, if item_per_page is defined)| integer |
| `sort_field` | The field to sort the results by. | optional | valid values: "id", "updatedAt", "createdAt" - default: "id" |

<a name="eventhandler_subscription_list_response" />

__Get subscriptions query parameters__  the output :

```json
{
  "count": 0,
  "data": [
    {
        "id": 0,
        "eventType": {
        "eventTypeName": "string",
        "createdAt": "string",
        "updatedAt": "string"
      },
      "filterMetaData": {
        "additionalProp1": "string",
        "additionalProp2": "string",
        "additionalProp3": "string"
      },
      "id": 0,
      "matchMetaData": true,
      "notifyUri": "string",
      "sources": [
        {
          "id": 0,
          "systemName": "string",
          "address": "string",
          "authenticationInfo": "string",
          "port": 0,
          "createdAt": "string",
          "updatedAt": "string"
        }
      ],
      "startDate": "string",
      "endDate": "string",
      "subscriberSystem": {
        "id": 0,
        "systemName": "string",
        "address": "string",
        "authenticationInfo": "string",
        "port": 0,
        "createdAt": "string",
        "updatedAt": "string"
      },
      "createdAt": "string",
      "updatedAt": "string"
    }
  ]
}
```

<a name="eventhandler_endpoints_get_subscription" />

### Get subscription by id

```
GET /mgmt/eventhandler/subscriptions/{id}
```

__Get subscriptions query parameters__  the input :

`https://eventhandler_ip:eventhandler_port/eventhandler/mgmt/subscriptions/`1

| __Get subscription by id__   path parameter |
| ------------------------------------------------------- |

| Parameter | Description | Necessity | Format/Limitations |
| --------- | ----------- | --------- | ----------- |
| `id` |  Id of subscription | mandatory | integer |

<a name="eventhandler_subscription_response" />

__Get subscription by id__  the output :

```json
    {
        "id": 0,
        "eventType": {
        "eventTypeName": "string",
        "createdAt": "string",
        "updatedAt": "string"
      },
      "filterMetaData": {
        "additionalProp1": "string",
        "additionalProp2": "string",
        "additionalProp3": "string"
      },
      "id": 0,
      "matchMetaData": true,
      "notifyUri": "string",
      "sources": [
        {
          "id": 0,
          "systemName": "string",
          "address": "string",
          "authenticationInfo": "string",
          "port": 0,
          "createdAt": "string",
          "updatedAt": "string"
        }
      ],
      "startDate": "string",
      "endDate": "string",
      "subscriberSystem": {
        "id": 0,
        "systemName": "string",
        "address": "string",
        "authenticationInfo": "string",
        "port": 0,
        "createdAt": "string",
        "updatedAt": "string"
      },
      "createdAt": "string",
      "updatedAt": "string"
    }
```

<a name="eventhandler_endpoints_put_subscription" />

### Update subscription

```
PUT /mgmt/eventhandler/subscriptions/{id}
```

__Update subscription request__  the input :

`https://eventhandler_ip:eventhandler_port/eventhandler/mgmt/subscriptions/`1

| __Update subscription__   path parameter |
| ------------------------------------------------------- |

| Parameter | Description | Necessity | Format/Limitations |
| --------- | ----------- | --------- | ----------- |
| `id` |  Id of subscription | mandatory | integer |

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
| `filterMetaData` | The received event has to contain all the "key - value" pairs defined here  | optional | max.length = 65535 |
| `matchMetaData` | A flag to turn on/off metadata filtering | mandatory |  true or false |
| `notifyUri` | Url subpath of the subscriber system's notification endpoint | mandatory | max.length = 65535 |
| `sources` | List of publisher systems | optional (if not defined or empty, all publishers will be able to send requests which are authorized and allowed by the other filtering options )| not defined |
| `startDate` | If startDate is defined, the subscriber system will only receive events when the event's timestamp is after startDate.  | optional ( StartDate must be after the current date/time. ) | UTC time in `yyyy-MM-dd`  `HH`:`mm`:`ss` format |
| `endDate` | If endDate is defined, the subscriber system will only receive events when the event's timestamp is before endDate. | optional ( EndDate must be after the current date/time. If startDate is defined endDate must be after startDate. )|  UTC time in `yyyy-MM-dd`  `HH`:`mm`:`ss` format  |
| `subscriberSystem` | Details of subscriber system | mandatory | as in system |

<a name="eventhandler_endpoints_delete_subscription" />

### Delete subscription

```
DELETE /mgmt/eventhandler/subscriptions/{id}
```

__Delete subscription parameters__  the input :

`https://eventhandler_ip:eventhandler_port/eventhandler/mgmt/subscriptions/`1

| __Get subscription by id__   path parameter |
| ------------------------------------------------------- |

| Parameter | Description | Necessity | Format/Limitations |
| --------- | ----------- | --------- | ----------- |
| `id` |  Id of subscription | mandatory | integer |

<a name="eventhandler_endpoints_post_auth_update" />

### Publish Auth Update <br />

This service can only be used by other core services, therefore this is not part of the public API.    

<a name="datamanager" />