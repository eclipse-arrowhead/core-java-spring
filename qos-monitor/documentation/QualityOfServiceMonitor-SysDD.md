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

* [Echo](#echo_service_use_case)

_Private:_

* [Public Key](#public_key_service_use_case)
* [Intra-Cloud Ping Measurement](#intra_ping_service_use_case)
* [Intra-Cloud Ping Median Measurement](#intra_ping_median_service_use_case)
* [Inter-Cloud Direct Ping Measurement](#inter_direct_ping_service_use_case)
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

### Use Case: Echo Service <a name="echo_service_use_case" />

| Name | Description |
| ---- | --------- |
| Brief Description | Returns a short message if the system is alive. |
| Access Control | All system within the local cloud are allowed to access |
| Primary Actors | QoS Monitor |
| Preconditions | - |
|Main Flow| * Request sent by client. <br/>* Request procced and response sent by QoS Monitor. |

### Use Case: Public Key Service <a name="public_key_service_use_case" />

| Name | Description |
| ---- | --------- |
| Brief Description | Returns the Public Key of QoSMointor Core System as a Base64 encoded text. |
| Access Control | All system within the local cloud are allowed to access |
| Primary Actors | QoS Monitor |
| Preconditions | - |
|Main Flow| * Request sent by an allowed core system. <br/>* Request procced and response sent by QoS Monitor. |

### Use Case: Intra-Cloud Ping Measurement Service <a name="intra_ping_service_use_case" />

| Name | Description |
| ---- | --------- |
| Brief Description | Returns the ping related values measured by a scheduled task for systems within the local cloud. |
| Access Control | Only Orchestrator and Gatekeeper Core Systems are allowed to access |
| Primary Actors | QoS Monitor |
| Secondary Actors | Service Registry |
| Preconditions | Service Registry has to be availble. |
| Main Flow | * Intra-Cloud Ping Measurement Task scheduled <br/>* Task queries Service Reqistry for systems and select one to be measured <br/>* Stored or immediate measurement details are provided upon request  |

_Activity diagram: Intra-Cloud Ping Measurement Service:_
![Alt text](figures/IntraPingMeasurementService.png)

_Activity diagram: Intra-Cloud Ping Measurement Task:_
![Alt text](figures/IntraPingMeasurementTask.png)

### Use Case: Intra-Cloud Ping Median Measurement Service <a name="intra_ping_median_service_use_case" />

| Name | Description |
| ---- | --------- |
| Brief Description | Calculates and returns the median ping related values based on the given attribute. |
| Access Control | Only Orchestrator and Gatekeeper Core Systems are allowed to access |
| Primary Actors | QoS Monitor |
| Secondary Actors | - |
| Preconditions | - |
| Main Flow | * Request sent by an allowed core system <br/>* Calculation performed and response sent by QoS Monitor. |

### Use Case: Inter-Cloud Direct Ping Measurement Service <a name="inter_direct_ping_service_use_case" />

| Name | Description |
| ---- | --------- |
| Brief Description | Returns the ping related values measured by a scheduled task for systems located within a neighbor cloud accessible without Gatekeeper Core System. |
| Access Control | Only Orchestrator and Gatekeeper Core Systems are allowed to access |
| Primary Actors | QoS Monitor |
| Secondary Actors |Gatekeeper |
| Preconditions | Gatekeeper has to be availble. |
| Main Flow | * Inter-Cloud Direct Ping Measurement Task scheduled <br/>* Task queries Gatekeeper for clouds with direct access and select one to measure its systems <br/>* Stored measurement details are provided upon request  |
