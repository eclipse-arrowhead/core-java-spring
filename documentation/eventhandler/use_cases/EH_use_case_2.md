Use case 2 : *Register Subscription*

| Name | Description |
| ---- | --------- |
| ID | Subscription |
| Brief Description | The Even Handler is tasked to register a subscription |
| Primary Actors | Subscriber, Event Handler, Authorization Core System |
| Preconditions | Subscriber has to be registered in Service Registry |
| Main Flow | - The Subscriber sends a SubscriptionRequest  to the Event Handler. <br/>- The Event Handler denies any request on it's Subscription endpoint if it is not from the system defined as the SubscriberSystem in the SubscriptionRequest<br/>- The Event Handler validates: <br/>- - the request format <br/>- - the request parameters validity. <br/>-  The Event Handler queries the Authorization Core Service for the authorized providers of the subscriber as a consumer.<br/>- The Event Handler registers the Subscription and the SubscriptionEntryPublisherConnections. |
| Postconditions | -   Subscriber would receive events if the parameters in the subscription match the parameters of an event and the publisher of the event has an authorized status in the SubscriptionEntryPublisherConnections.<br/>- The Subscriber would not receive events from unauthorized publishers. |
