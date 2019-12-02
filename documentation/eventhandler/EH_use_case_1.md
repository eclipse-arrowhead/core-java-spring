Use case 1: *Publish Event*

| Name | Description |
| ---- | --------- |
| ID | Publish-Event |
| Brief Description | The Even Handler is tasked to deliver Event to all relevant Subscribers |
| Primary Actors | Publisher, Event Handler, Subscribers |
| Preconditions | Subscriber have to be subscribed with a filter of matching event type, date, and metadata. Subscriber has to be an authorized consumer of the publisher system. |
| Main Flow | - The Publisher sends an event to the Event Handler. <br/>- The Event Handler validate the Event source and the Event TimeStamp. <br/>- The Event Handler select all Authorized Subscribers by Even Type <br/>- The Event Handler filters Subscribers by timestamp <br/>- The Event Handler filters Subscribers by metadata <br/>- The Event Handler sends the Event to the remaining subscribers. |