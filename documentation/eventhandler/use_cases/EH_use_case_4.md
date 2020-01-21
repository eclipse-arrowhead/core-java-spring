Use case 4: *Publish Authorization Update Event*

| Name | Description |
| ---- | --------- |
| ID | Publish-Auth-Update-Event |
| Brief Description | The Event Handler is tasked to update the allowed providers for the given Subscriber |
| Primary Actors | Authorization Core Service, Event Handler |
| Preconditions | -   Subscriber has to be registered to Event Handler  |
| Main Flow | - The Authorization Core System sends an AuthorizationUpdateEvent to the Event Handler. <br/>- The Event Handler denies any request on it's Publish-Auth-Update endpoint if it is not from the Authorization Core Service<br/>- The Event Handler validate: <br/>- - the request format <br/>- - the Event source <br/>- - the Event TimeStamp. <br/>- - the AuthorizationUpdateEvent Type. <br/>- - the AuthorizationUpdateEvent Payload. <br/>- The Event Handler selects the involved Subscriber by the request payload.<br/>- The Event Handler queries the Authorization Core Service for the authorized providers of the subscriber as a consumer.<br/>- The Event Handler updates the authorization status of the subscribers SubscriptionEntryPublisherConnections. |
| Postconditions | -   Subscriber could not receive events from unauthorized publishers  |
