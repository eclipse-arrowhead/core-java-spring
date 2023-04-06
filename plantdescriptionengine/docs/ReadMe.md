# Plant Description Engine
 
This supporting core system has the purpose of choreographing the consumers and producers in the plant (System of Systems / Local cloud).
An abstract view, on which systems the plant contains and how they are connected as consumers and producers, is used to populate the [Orchestrator](#orchestrator) with store rules for each of the consumers. The abstract view does not contain any instance specific information, instead meta-data about each system is used to identify the service producers.

The plant description engine (PDE) can be configured with several variants of the plant description of which at most one can be active. The active plant description is used to populate the orchestrator and if no plant description is active the orchestrator does not contain any store rules populated by the PDE. This can be used to establish alternativ plants (plan A, plan B, etc).

The PDE gathers information about the presence of all specified systems in the active plant description. If a system is not present it raises an alarm. If it detects that an unknown system has registered a service in the service registry it also raises an alarm. For a consumer system to be monitored the system must produce the [Monitorable] service and hence also register in the service registry.

Please see the [Plant Description Engine - System of systems Description (SosD)] and [Plant Description Engine HTTP(S)/JSON - System Description (SysD)] for further details.

## Services

The PDE produces two different services:
 + the [Plant Description Management] service - [Plant Description Management JSON]
 + the [Plant Description Monitor] service - [Plant Description Monitor JSON]
 
The PDE consumes the following services:
 + the [Service Discovery] service produced by the [Service Registry] core system
 + the [Orchestration Store Management] service produced by the [Orchestrator] core system
 + the [Orchestration] service produced by the [Orchestrator] core system
 + the [Inventory] service produced by an Inventory system - [Inventory JSON]
 + the [Monitorable] service produced by the systems in the plant - [Monitorable JSON]
    
  
[Authorization]:README.md#authorization
[AuthorizationControl]:README.md#authorization
[Inventory]:/documentation/plant-description-engine/inventory-sd.md
[Inventory JSON]:/documentation/plant-description-engine/inventory-idd-http-json.md
[Monitorable]:/documentation/plant-description-engine/monitorable-sd.md
[Monitorable JSON]:/documentation/plant-description-engine/monitorable-idd-http-json.md
[Orchestrator]:README.md#orchestrator
[Orchestration]:README.md#orchestrator
[Orchestration Store Management]:README.md#orchestrator
[Plant Description Monitor]:/documentation/plant-description-engine/plant-description-monitor-sd.md
[Plant Description Monitor JSON]:/documentation/plant-description-engine/plant-description-monitor-idd-http-json.md
[Plant Description Management]:/documentation/plant-description-engine/plant-description-management-sd.md
[Plant Description Management JSON]:/documentation/plant-description-engine/plant-description-management-idd-http-json.md
[Plant Description Engine HTTP(S)/JSON - System Description (SysD)]:/documentation/plant-description-engine/plant-description-engine-sysd.md
[Plant Description Engine - System of systems Description (SosD)]:/documentation/plant-description-engine/plant-description-engine-sosd.md
[Service Discovery]:README.md#serviceregistry_usecases
[Service Registry]:README.md#serviceregistry
