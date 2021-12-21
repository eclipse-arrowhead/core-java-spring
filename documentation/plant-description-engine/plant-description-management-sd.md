# Plant Description Management - Service Description

## Abstract
This document describes an abstract service, which, if implemented by an application system, can be used to manage Plant Descriptions in the [Plant Description Engine] core system.

## Overview
This document describes an abstract Arrowhead Framework service meant to enable management of Plant Descriptions in the [Plant Description Engine] ([PDE]) core system.

A [Plant Description](#struct-plantdescription) (PD) is used to represent the expected Arrowhead Framework Systems and their interconnections in a plant.

This service is produced by the PDE and can be consumed by, for example, a management system to manage the Plant Descriptions populated in the PDE.

## Service Interfaces
This section lists the interfaces that must be exposed by the [PDE] in alphabetical order. In particular, each subsection names an abstract interface, an input type, an output type and a set of possible exceptions, in that order. The input type is named inside parentheses, while the output type is preceded by a colon. Input and output types are only denoted when accepted or returned, respectively, by the interface in question.
All abstract data types named in this section are defined in the [Information model](#information-model) section.


### interface AddPlantDescription([PlantDescription](#struct-plantdescription)): [PlantDescriptionEntryList](#struct-plantdescriptionentrylist)

Called to add a Plant Description to the PDE.

### interface DeletePlantDescription(id)

Called to delete the __[PlantDescriptionEntry](#struct-plantdescriptionentry)__ specified by the `id` parameter.

### interface GetAllPlantDescriptions(): [PlantDescriptionEntryList](#struct-plantdescriptionentrylist)

Called to acquire a list of Plant Description Entries present in the PDE.

### interface GetPlantDescription(id): [PlantDescriptionEntry](#struct-plantdescriptionentry)

Called to acquire the Plant Description Entry specified by the `id` parameter.

### interface ReplacePlantDescription(id, [PlantDescription](#struct-plantdescription)): [PlantDescriptionEntry](#struct-plantdescriptionentry)

Called to replace the Plant Description Entry specified by the `id` parameter with the Plant Description in the `PlantDescription` parameter.

### interface UpdatePlantDescription(id, [PlantDescriptionUpdate](#struct-plantdescriptionupdate)): [PlantDescriptionEntry](#struct-plantdescriptionentry)

Called to update the Plant Description Entry specified by the `id` parameter with the information in the `PlantDescriptionUpdate` parameter.

## Information model
Here, all data objects that can be part of Plant Description Alarm service calls are listed in alphabetic order. Note that each
subsection, which describes one type of object, begins with the struct or union keywords. The former is used to
denote a collection of named fields, each with its own data type, while the latter is used to express that a value
is allowed to be any one out of a number of listed variant types. As a complement to the explicitly defined types
in this section, there is also a list of implicit [primitive](#primitives) types,
which are used to represent things like dates.

### struct Connection
| Field | Type | Description | Mandatory |
| ----- | ---- | ----------- | --------- |
| `consumer` | [SystemPort](#struct-systemport) | The consumer end SystemPort of the connection | `true` |
| `producer` | [SystemPort](#struct-systemport) | The producer end SystemPort of the connection | `true` |


### struct PlantDescription

| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `plantDescription` | String | Plant description name | `true` | |
| `active` | Boolean | Is this the active plant description | `false` | `false` |
| `include` | Array\<Integer>| Array with Ids of other PDs that are included in this PD | `false` | [] |
| `systems` | Array\<[System](#struct-system)> | Array with systems expected to be present in the plant | `true` ||
| `connections` | Array\<[Connection](#struct-connection)> | Array with connection that should be populated into the Orchestrator | `true` ||

### struct PlantDescriptionEntry

| Field | Type | Description | Mandatory | Note |
| ----- | ---- | ----------- | --------- | ------------- |
| `id` | Integer | Id of the entry | `true` | |
| `plantDescription` | String | Plant description name| `true` | |
| `active` | Boolean | Is this the active plant description | `true` | |
| `include` | Array\<Integer>| Array with Ids of other PDs that are included in this PD | `true` | |
| `systems` | Array\<[System](#struct-system)> | Array with systems expected to be present in the plant | `true` ||
| `connections` | Array\<[Connection](#struct-connection)> | Array with connection that should be populated into the Orchestrator | `true` ||
| `createdAt` | DateTime | Creation date of the entry | `true` | |
| `updatedAt` | DateTime | When the entry was last updated | `true` | |

### struct PlantDescriptionEntryList

| Field | Type | Description |
| ----- | ---- | ----------- |
| `count` | Integer | Number of records found |
| `data` | Array\<[PlanDescriptionEntry](#struct-plantdescriptionentry)> | Array with Plant Description Entries |

### struct PlantDescriptionUpdate

Currently only the following values can be updated. If a field is not present the current value will remain unchanged.

| Field | Type | Description | Mandatory |
| ----- | ---- | ----------- | --------- |
| `plantDescription` | String | Plant description name | `false` |
| `active` | Boolean | Is this the active plant description | `false` |
| `include` | Array\<Integer>| Array with Ids of other PDs that are included in this PD | `false` |
| `systems` | Array\<[System](#struct-system)> | Array with systems expected to be present in the plant | `false` |
| `connections` | Array\<[Connection](#struct-connection)> | Array with connection that should be populated into the Orchestrator | `false` |

### struct Port
| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `portName` | String | Identity of the port | `true` | |
| `metadata` | Object\<String> | Metadata - key-value pairs | `false` | null |
| `serviceDefinition` | String | Service definition identity | `true` | |
| `consumer` | Boolean | Is the port a consumer port | `false` | `false` |

### struct System
| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `systemId` | String | Identity of the system within the PDE | `true` | |
| `systemName` | String | Name of the system | `false` | null |
| `metadata` | Object\<String> | Metadata - key-value pairs | `false` | null |
| `ports` | Array\<[Port](#struct-port)> | Array with service ports exposed by the system | `true` ||

### struct SystemPort
| Field | Type | Description | Mandatory | Default value |
| ----- | ---- | ----------- | --------- | ------------- |
| `systemId` | String | Identity of the system within the PDE | `true` | |
| `systemName` | String | Name of the system | `false` | null |
| `portName` | String | Identity of the port | `true` | |


### Primitives
Types and structures mentioned throughout this document that are assumed to be available to implementations
of this service. The concrete interpretations of each of these types and structures must be provided by any IDD
document claiming to implement this service.

| Type | Description |
| ---- | ----------- |
| Object \<A> | An unordered collection of [String: Value] pairs, where each Value conforms to type A. Empty objects are omitted. |
| Array \<A> | An ordered collection of elements, where each element conforms to type A. Empty arrays are omitted. |
| Boolean | One out of `true` or `false`. |
| DateTime | Pinpoints a specific moment in time. |
| Integer | 32-bit signed two's complement integer, which has a minimum value of -2<sup>31</sup> and a maximum value of 2<sup>31</sup>-1 |
| String | An arbitrary UTF-8 string. |

[PDE]:plant-description-engine-sysd.md
[Plant Description Engine]:plant-description-engine-sysd.md