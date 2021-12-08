# Plant Description Engine - System of Systems Description (SosD)

## Abstract
This document describes how a system of systems that includes a [Plant Description Engine] (PDE) interacts with the PDE.

## Overview

This document describes how a system of systems that includes the Plant Description Engine (PDE) Arrowhead Framework system interacts.

A plant (System of Systems / Local cloud) is assumed to include the following systems:
 - [Service Registry]
 - [Authorization]
 - [Orchestrator]
 - [Plant Description Engine]
 - Some Producer systems
 - Some Consumer systems

We will as an example consider a plant that has the four core systems and six custom systems:
 - Operations Center that manages and monitors the Plant Description Engine
 - A that produces service X
 - B that produces service Y
 - C that produces service Z
 - D that consumes services X, Y and Z.
   This system should always be connected to a system producing X.
   It should also be connected to either a system that provides Y or a system the provides Z but not both.
 - Invent system that produces the [Inventory] service.

The four custom systems A-D also provides the [Monitorable] service.


## The plant description(s) describing the example system

The basic Arrow Head core systems are connected to each other

IMAGE

This corresponds to a Plant Description with the core systems:

```json
{
	"id": 1,
	"plantDescription": "ArrowHead core",
	"systems": [
		{
			"systemName": "serviceregistry",
			"systemId": "service_registry",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery"}
			]
		},
		{
			"systemName": "authorization",
			"systemId": "authorization",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "Token Generation"},
				{ "portName": "authorizationControl", "serviceDefinition": "Authorization Control"}
			]
		},
		{
			"systemName": "orchestration",
			"systemId": "orchestration",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "tokenGeneration", "serviceDefinition": "Token Generation", "consumer": true },
				{ "portName": "authorizationControl", "serviceDefinition": "Authorization Control", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "OrchestrationService"},
				{ "portName": "orchestrationStoreManagement", "serviceDefinition": "OrchestrationStoreManagement", "consumer": true }
			]
		},
		{
			"systemId": "plant_description_engine",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "OrchestrationService", "consumer": true },
				{ "portName": "orchestrationStoreManagement", "serviceDefinition": "OrchestrationStoreManagement", "consumer": true },
				{ "portName": "inventory", "serviceDefinition": "Inventory", "consumer": true },
				{ "portName": "monitor", "serviceDefinition": "Plant Description Monitor"},
				{ "portName": "management", "serviceDefinition": "Plant Description Management"}
			]
		}
	],
	"connections": [
		{ "consumer": { "systemId": "authorization", "portName": "service_discovery" },
		  "producer": { "systemId": "service_registry", "portName": "service_discovery" }},

		{ "consumer": { "systemId": "orchestration", "portName": "service_discovery" },
		  "producer": { "systemId": "service_registry", "portName": "service_discovery" }},
		{ "consumer": { "systemId": "orchestration", "portName": "tokenGeneration" },
		  "producer": { "systemId": "authorization", "portName": "tokenGeneration" }},
		{ "consumer": { "systemId": "orchestration", "portName": "authorizationControl" },
		  "producer": { "systemId": "authorization", "portName": "authorizationControl" }},

		{ "consumer": { "systemId": "plant_description_engine", "portName": "service_discovery" },
		  "producer": { "systemId": "service_registry", "portName": "service_discovery" }},
		{ "consumer": { "systemId": "plant_description_engine", "portName": "orchestrationService" },
		  "producer": { "systemId": "orchestration", "portName": "orchestrationService" }},
		{ "consumer": { "systemId": "plant_description_engine", "portName": "orchestrationStoreManagement" },
		  "producer": { "systemId": "orchestration", "portName": "orchestrationStoreManagement" }}
	]
}
```

All custom systems are assumed to be connected to the core systems and register their provides services in the [Service Registry].

IMAGE

This corresponds to a bare plant description that include the core and contains the custom systems and how they are connected to the core systems:

```json
{
	"id": 2,
	"plantDescription": "Example plant - bare",
	"include": [ 1 ],
	"systems": [
		{
			"systemName": "operationscenter",
			"systemId": "operations_center",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "orchestrationService", "serviceDefinition": "OrchestrationService", "consumer": true },
				{ "portName": "monitor", "serviceDefinition": "Plant Description Monitor", "consumer": true },
				{ "portName": "management", "serviceDefinition": "Plant Description Management", "consumer": true }
			]
		},
		{
			"systemName": "a",
			"systemId": "a",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "x", "serviceDefinition": "X"},
				{ "portName": "monitorable", "serviceDefinition": "Monitorable"}
			]
		},
		{
			"systemName": "b",
			"systemId": "b",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "y", "serviceDefinition": "Y"},
				{ "portName": "monitorable", "serviceDefinition": "Monitorable"}
			]
		},
		{
			"systemName": "c",
			"systemId": "c",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "z", "serviceDefinition": "Z"},
				{ "portName": "monitorable", "serviceDefinition": "Monitorable"}
			]
		},
		{
			"systemName": "d",
			"systemId": "d",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "monitorable", "serviceDefinition": "Monitorable"},
				{ "portName": "x", "serviceDefinition": "X", "consumer": true },
				{ "portName": "y", "serviceDefinition": "Y", "consumer": true },
				{ "portName": "z", "serviceDefinition": "Z", "consumer": true }
			]
		},
		{
			"systemName": "invent",
			"ports": [
				{ "portName": "service_discovery", "serviceDefinition": "Service Discovery", "consumer": true },
				{ "portName": "inventory", "serviceDefinition": "Inventory"}
			]
		}

	],
	"connections": [
		{ "consumer": { "systemId": "a", "portName": "service_discovery" },
		  "producer": { "systemId": "service_registry", "portName": "service_discovery" }},

		{ "consumer": { "systemId": "b", "portName": "service_discovery" },
		  "producer": { "systemId": "service_registry", "portName": "service_discovery" }},

		{ "consumer": { "systemId": "c", "portName": "service_discovery" },
		  "producer": { "systemId": "service_registry", "portName": "service_discovery" }},


		{ "consumer": { "systemId": "d", "portName": "service_discovery" },
		  "producer": { "systemId": "service_registry", "portName": "service_discovery" }},
		{ "consumer": { "systemId": "d", "portName": "orchestrationService" },
		  "producer": { "systemId": "orchestration", "portName": "orchestrationService" }},

		{ "consumer": { "systemId": "operations_center", "portName": "service_discovery" },
		  "producer": { "systemId": "service_registry", "portName": "service_discovery" }},
		{ "consumer": { "systemId": "operations_center", "portName": "orchestrationService" },
		  "producer": { "systemId": "orchestration", "portName": "orchestrationService" }},

		{ "consumer": { "systemId": "invent", "portName": "service_discovery" },
		  "producer": { "systemId": "service_registry", "portName": "service_discovery" }},
		{ "consumer": { "systemId": "plant_description_engine", "portName": "inventory" },
		  "producer": { "systemId": "invent", "portName": "inventory" }}
	]
}
```

The bare plant is extended with the connections that should always be there

```json
{
	"id": 3,
	"plantDescription": "Example plant - base",
	"include": [ 2 ],
	"systems": [],
	"connections": [
		{ "consumer": { "systemId": "d", "portName": "x" },
		  "producer": { "systemId": "a", "portName": "x" }},

		{ "consumer": { "systemId": "operations_center", "portName": "management" },
		  "producer": { "systemId": "plant_description_engine", "portName": "management" }},
		{ "consumer": { "systemId": "operations_center", "portName": "monitor" },
		  "producer": { "systemId": "plant_description_engine", "portName": "monitor" }},

		{ "consumer": { "systemId": "plant_description_engine", "portName": "monitorable" },
		  "producer": { "systemId": "a", "portName": "monitorable" }},
		{ "consumer": { "systemId": "plant_description_engine", "portName": "monitorable" },
		  "producer": { "systemId": "b", "portName": "monitorable" }},
		{ "consumer": { "systemId": "plant_description_engine", "portName": "monitorable" },
		  "producer": { "systemId": "c", "portName": "monitorable" }},
		{ "consumer": { "systemId": "plant_description_engine", "portName": "monitorable" },
		  "producer": { "systemId": "d", "portName": "monitorable" }}
	]
}
```


We now end up with two different variants that include the base variant, one that connects D to service Y in B

```json
{
	"id": 4,
	"plantDescription": "Example plant variant 1",
	"active": true,
	"include": [ 3 ],
	"systems": [],
	"connections": [
		{ "consumer": { "systemId": "d", "portName": "y" },
		  "producer": { "systemId": "b", "portName": "y" }}
	]
}
```

and one that connects D to service Z in C

```json
{
	"id": 5,
	"plantDescription": "Example plant variant 2",
	"include": [ 3 ],
	"systems": [],
	"connections": [
		{ "consumer": { "systemId": "d", "portName": "z" },
		  "producer": { "systemId": "c", "portName": "z" }}
	]
}
```

### Identifying systems
The name of a system may not always be known at plant description design time. In that case, *metadata* can be used instead:

```json
{
	"systemId": "e",
	"metadata": {"a": "1", "b": "2"},
	"ports": [
		{ "portName": "z", "serviceDefinition": "Z"}
	]
}
```
This metadata is forwarded to the Orchestrator as part of the rules created by the PDE, and can thus be used to identify systems during orchestration.

### Specifying service interfaces
A service interface requirement can be given as part of a system port:


```json
{
	"portName": "x",
	"serviceDefinition": "X",
	"serviceInterface": "HTTP-SECURE-JSON"
}
```

If a service interface is specified on a port, the same service interface must be given on all ports that it is connected to. This service interface requirement is forwarded to the Orchestrator.


### Providing multiple instances of a service
A system may need to provide multiple instances of the same service, where each instance should be accessible only to a specific set of consumers. This can be expressed by adding metadata to the provider system's ports:

```json
{
	"systemName": "e",
	"systemId": "e",
	"ports": [
		{
			"portName": "z1",
			"serviceDefinition": "Z",
			"metadata": {"instance": "1"}
		},
		{
			"portName": "z2",
			"serviceDefinition": "Z",
			"metadata": {"instance": "2"}
		},
	]
}
```

### Prioritizing connections
It is also possible to assign a priority to each connection. This will decide the order of the list of services that the consumer receives during orchestration. Priorities are assigned to connections as follows:

```json
[
	{
		"consumer": { "systemId": "a", "portName": "z" },
		"producer": { "systemId": "b", "portName": "z" },
		"priority": 1
	},
	{
		"consumer": { "systemId": "a", "portName": "z" },
		"producer": { "systemId": "c", "portName": "z" },
		"priority": 2
	}
]
```
A lower value signifies a higher priority.

### Bootstrapping problem
In the plant description above we have a bootstrapping problems. In order for the Operations Center to be allowed to use the Plant Description Management service from the PDE, a rule allowing it to do so must be added to the Orchestrator. Otherwise it will not be able to add the first plant description allowing it to use the service.

### Simplified plant description

The plant description shown above includes all links between systems in the plant. However, for some of those communication links the Orchestrator is not called and therefore they are not strictly required in the plant description.

A merged and cleaned plant description would look like this

```json
{
	"id": 3,
	"plantDescription": "Example plant - base",
	"systems": [
		{
			"systemName": "plantdescriptionengine",
			"systemId": "plant_description_engine",
			"ports": [
				{ "portName": "monitor", "serviceDefinition": "Plant Description Monitor"},
				{ "portName": "management", "serviceDefinition": "Plant Description Management"},
				{ "portName": "monitorable", "serviceDefinition": "Monitorable", "consumer": true },
				{ "portName": "inventory", "serviceDefinition": "Inventory", "consumer": true }
			]
		},
		{
			"systemName": "operationscenter",
			"systemId": "operations_center",
			"ports": [
				{ "portName": "monitor", "serviceDefinition": "Plant Description Monitor", "consumer": true },
				{ "portName": "management", "serviceDefinition": "Plant Description Management", "consumer": true }
			]
		},
		{
			"systemName": "a",
			"systemId": "a",
			"ports": [
				{ "portName": "x", "serviceDefinition": "X"},
				{ "portName": "monitorable", "serviceDefinition": "Monitorable"}
			]
		},
		{
			"systemName": "b",
			"systemId": "b",
			"ports": [
				{ "portName": "y", "serviceDefinition": "Y"},
				{ "portName": "monitorable", "serviceDefinition": "Monitorable"}
			]
		},
		{
			"systemName": "c",
			"ports": [
				{ "portName": "z", "serviceDefinition": "Z"},
				{ "portName": "monitorable", "serviceDefinition": "Monitorable"}
			]
		},
		{
			"systemName": "d",
			"systemId": "d",
			"ports": [
				{ "portName": "monitorable", "serviceDefinition": "Monitorable"},
				{ "portName": "x", "serviceDefinition": "X", "consumer": true },
				{ "portName": "y", "serviceDefinition": "Y", "consumer": true },
				{ "portName": "z", "serviceDefinition": "Z", "consumer": true }
			]
		},
		{
			"systemName": "invent",
			"systemId": "invent",
			"ports": [
				{ "portName": "inventory", "serviceDefinition": "Inventory"}
			]
		}

	],
	"connections": [
		{ "consumer": { "systemId": "d", "portName": "x" },
		  "producer": { "systemId": "a", "portName": "x" }},

		{ "consumer": { "systemId": "operations_center", "portName": "management" },
		  "producer": { "systemId": "plant_description_engine", "portName": "management" }},
		{ "consumer": { "systemId": "operations_center", "portName": "monitor" },
		  "producer": { "systemId": "plant_description_engine", "portName": "monitor" }},

		{ "consumer": { "systemId": "plant_description_engine", "portName": "monitorable" },
		  "producer": { "systemId": "a", "portName": "monitorable" }},
		{ "consumer": { "systemId": "plant_description_engine", "portName": "monitorable" },
		  "producer": { "systemId": "b", "portName": "monitorable" }},
		{ "consumer": { "systemId": "plant_description_engine", "portName": "monitorable" },
		  "producer": { "systemId": "c", "portName": "monitorable" }},
		{ "consumer": { "systemId": "plant_description_engine", "portName": "monitorable" },
		  "producer": { "systemId": "d", "portName": "monitorable" }},

		{ "consumer": { "systemId": "plant_description_engine", "portName": "inventory" },
		  "producer": { "systemId": "invent", "portName": "inventory" }}

	]
}
```


## Interactions between systems

To setup a fully functional system of systems that includes a PDE there are a number of message interaction between the systems necessary.

### Service registration for systems that produce a service

Whenever a service provider system starts up, it must register its services in the [Service Registry]. To do this it locates the Service Registry either by configuration or by using the [DNS]. The system connects to the [Service Discovery] service and uses the [Register](../../README.md#serviceregistry_endpoints_post_register) end point to register its provided services. The system also uses the [Query](../../README.md#serviceregistry_endpoints_post_query) end point to retrieve information about the [Authorization] system. Especially the public key in the `authenticationInfo` is needed to verify authorization tokens when other systems connect to its service.

### Service lookup for systems that consume a service

Whenever a consumer system starts up, it must locate the [Orchestration] end point to query the [Orchestrator] about which system/systems to connect to. This is done by locating the Service Registry, either by configuration or by using the [DNS]. The system then connects to the [Service Discovery] service and uses the [Query](../../README.md#serviceregistry_endpoints_post_query) to retrieve information about the [Orchestration] service.

It then connects to the Orchestrator and uses the [Orchestration] end point to query about each specific service it needs. The Orchestrator in turn queries the Service Discovery service for providers of the requested service. Using its store rules the Orchestrator selects the systems that should be used to provide the service. It then uses the [Authorization] system's [Check an Intracloud rule](../../README.md#authorization_endpoints_post_intracloud_check) end point to authorize the consuming system and the [Genrate Token](../../README.md#authoritation_endpoints_post_token) end point to generate the needed token for each of the providing systems.

When the consuming system receives an Orchestration Response from the Orchestrator it decodes the authorization tokens received and connects to the providing system sending along the decrypted authorization token which is checked by the provider using the Authorization systems public key. If that matches, a connection between consumer and provider is established.


### Updating the Plant description

The PDE should contain at least one Plant Description (PD) of the plant. The operator (operation center system) uses the Orchestrator to look up the system (PDE) providing the [Plant Description Management] service. Then connects to the PDE and uses the [AddPlantDescription](plant-description-management-sd.md#interface-addplantdescriptionplantdescription-plantdescriptionentrylist) end point to add a PD.
When the operator has activated a PD using {[Add](plant-description-management-sd.md#interface-addplantdescriptionplantdescription-plantdescriptionentrylist)/[Replace](plant-description-management-sd.md#interface-replaceplantdescriptionid-plantdescription-plantdescriptionentry)/[Update](plant-description-management-sd.md#interface-updateplantdescriptionid-plantdescriptionupdate-plantdescriptionentry)}PlantDescription end point, the PDE connects to the [Orchestration Store Management] service.

TODO: Use a real reference here
The PDE then removes any existing store rules, using the [Delete All Store Entries](TODO: Add a reference to this endpoint). It then adds new store rules, using the [Add Store Entries](TODO: Add a reference to this endpoint) end point, for all the connections present in the newly activated PD.

Whenever the Orchestrator is updated it uses the [Orchestration Push] service, of all the systems that has registered as a producer of that service, to inform them about any updates that concerns them. If a consumer system has not registered the [Orchestration Push] service it must poll the Orchestrator regularly to keep updated.

At the moment the [Orchestration Store Management] service requires `consumerSystemId` as part of the `StoreRule`. This means that in order to be able to add a store rule for a consumer system, it must be registered in the systems registry.  Hence either the PDE must know instance information (ip address and port) for all systems and register them, or the systems must register before the PDE can add store rules. In the next version of the Orchestrator this will be changed and instead the consumer system will be identified using `systemName`. Furthermore, both consumer and provider systems will be possible to identify using `MetaData` instead of `systemName`.

### Monitoring the plant

The PDE queries the Orchestrator about any systems that it should monitor using the [Monitorable] service. The PDE regularly [Pings](monitorable-sd.md#interface-ping-ok) the monitored systems and raises an alarm if the system does not respond.

If the monitored system provides any [SystemData](monitorable-sd.md#interface-getsystemdata-systemdata) and/or an [InventoryId](monitorable-sd.md#interface-getinventoryid-inventoryid), this data is stored by the PDE and returned as part of the Plant Description Entries provided by the [Plant Description Monitor] service.

If there is an Inventory system present in the plant that produces the [Inventory] service and the PDE is connected to it in the active PD, the PDE connects to the [Inventory] service. The PDE queries it for [InventoryData] for the systems that the PDE monitors, according to the active PD. If the system has provided an [Inventory ID](monitorable-sd.md#interface-getinventoryid-inventoryid), this is used in the Inventory query. Otherwise, only metadata about the system is used. Any found [InventoryData] is stored by the PDE and returned as part of the Plant Description Entries provided by the [Plant Description Monitor] service.





[Authorization]:../../README.md#authorization
[AuthorizationControl]:../../README.md#authorization
[DNS]:https://en.wikipedia.org/wiki/Domain_Name_System
[Inventory]:inventory-sd.md
[InventoryData]:inventory-sd.md#struct-inventorydata
[Monitorable]:monitorable-sd.md
[Orchestrator]:../../README.md#orchestrator
[Orchestration]:../../README.md#orchestrator_endpoints_post_orchestration
[Orchestration Store Management]:../../README.md#orchestrator
[Orchestration Push]:../../README.md#orchestrator_usecases
[Plant Description Engine]:plant-description-engine-sysd.md
[Plant Description Monitor]:plant-description-monitor-sd.md
[Plant Description Management]:plant-description-management-sd.md
[Service Discovery]:../../README.md#serviceregistry_usecases
[Service Registry]:../../README.md#serviceregistry
