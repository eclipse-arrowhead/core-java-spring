Use case 3 : *Unregister Subscription*

| Name | Description |
| ---- | --------- |
| ID | Unsubscribe |
| Brief Description | The Even Handler is tasked to unregister a subscription |
| Primary Actors | Subscriber, Event Handler|
| Preconditions |  Subscriber has to be registered in Service Registry  |
| Main Flow | - The Subscriber sends an id and a SubscriptionRequest  to the Event Handler's unregister endpoint. <br/>- The Event Handler denies any request on it's Unsubscription endpoint if it is not from the system defined as the SubscriberSystem in the SubscriptionRequest. <br/>- The Event Handler validates: <br/>- - the request format <br/>- - the request parameters validity.<br/>- The Event Handler unregisters the SubscriptionEntryPublisherConnections and the Subscription. |
| Postconditions | -   Subscriber would not receive events |
