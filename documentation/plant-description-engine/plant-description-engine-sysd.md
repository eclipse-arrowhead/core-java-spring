# Plant Description Engine HTTP(S)/JSON - System Description (SysD)

## Abstract
This document describes a system useful for choreographing the consumers and producers in the plant (System of Systems / Local cloud).
In particular the system uses an abstract view of the plant and populates the [Orchestrator] with store rules for each of the consumers.

## Overview

This document describes the HTTP/{TLS}/JSON Plant Description Engine (PDE) Arrowhead Framework system.
This supporting core system has the purpose of choreographing the consumers and producers in the plant (System of Systems / Local cloud).
An abstract view, on which systems the plant contains and how they are connected as consumers and producers, is used to populate the [Orchestrator] with store rules for each of the consumers. The abstract view does not contain any instance specific information, instead meta-data about each system is used to identify the service producers.

For a description of how a system of systems that includes a Plant Description Engine (PDE) interacts with the PDE see the [System of Systems description]. 

The plant description engine (PDE) can be configured, using the [Plant Description Management] service, with several variants of the plant description of which at most one can be active.
The active plant description is used to populate the orchestrator and if no plant description is active the orchestrator does not contain any store rules populated by the PDE. This can be used to establish alternative plants (plan A, plan B, etc).

The PDE gathers information about the presence of all specified systems in the active plant description. If a system is not present it raises an alarm. If it detects that an unknown system has registered a service in the service registry it also raises an alarm. For a consumer system to be monitored the system must produce the [Monitorable] service and hence also register in the service registry. The [Plant Description Monitor] service can be used to inspect and manage any raised alarms.

Tentatively, in the future the PDE can gather system specific data from all systems in the plant that produces the [Monitorable] service. Furthermore, the PDE could collect information from an Inventory using the [Inventory] service. Both of these additional data would then be returned by the [Plant Description Monitor] service. 

## Services

The PDE produces two different [HTTP]/[JSON] services:
 + the [Plant Description Management JSON] service
 + the [Plant Description Monitor JSON] service
 
The PDE consumes the following [HTTP]/[JSON] services:
 + the [Service Discovery] service produced by the [Service Registry] core system
 + the [Orchestration Store Management] service produced by the [Orchestrator] core system
 + the [Orchestration] service produced by the [Orchestrator] core system
 + the [Monitorable JSON] service produced by the systems in the plant
 + the [Inventory JSON] service produced by an Inventory system
    
  

[HTTP]:https://doi.org/10.17487/RFC7230
[Inventory]:inventory-sd.md
[Inventory JSON]:inventory-idd-http-json.md
[JSON]:https://doi.org/10.17487/RFC7159
[Monitorable]:monitorable-sd.md
[Monitorable JSON]:monitorable-idd-http-json.md
[Orchestrator]:../../README.md#orchestrator
[Orchestration]:../../README.md#orchestrator
[Orchestration Store Management]:../../README.md#orchestrator
[Plant Description Monitor]:plant-description-monitor-sd.md
[Plant Description Monitor JSON]:plant-description-monitor-idd-http-json.md
[Plant Description Management]:plant-description-management-sd.md
[Plant Description Management JSON]:plant-description-management-idd-http-json.md
[Service Discovery]:../../README.md#serviceregistry_usecases
[Service Registry]:../../README.md#serviceregistry
[System of Systems description]:plant-description-engine-sosd.md