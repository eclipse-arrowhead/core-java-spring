# QUALITY OF SERVICE MONITOR
### System Design Description

The purpose of QoS Monitor supporting core system is providing QoS (Quality of Service) measurements to the QoS Manager (which is part of the Orchestrator core system).

![#1589F0](https://placehold.it/15/1589F0/000000?text=+) `AH Service Registry`
![#f03c15](https://placehold.it/15/f03c15/000000?text=+) `AH Authorization` 
![#c5f015](https://placehold.it/15/c5f015/000000?text=+) `AH Orchestrator / QoS Manager`
![#ffcc44](https://placehold.it/15/a33c00/000000?text=+) `AH QoS Monitor`
![Alt text](figures/overview.png)

## Services and Use Cases
### Provided Services

The QoS Monitor provides the following services:

_Client:_

* [Echo](todo)
* [Public Key](todo)

_Private:_

* [Intra-Cloud Ping Measurement](todo)
* [Intra-Cloud Ping Median Measurement](todo)
* [Inter-Cloud Direct Ping Measurement](todo)
* [Inter-Cloud Relay Echo Measurement](todo)
* [Init Relay Test](todo)
* [Join Relay Test](todo)

### Consumed Services

_Service Registry Core System:_

* [Query](todo)

_Gatekeeper Core System:_

* [Pull Clouds](todo)
* [Get Cloud](todo)
* [Collect Access Types](todo)
* [Collect System Addresses](todo)
* [Relay Test](todo)
