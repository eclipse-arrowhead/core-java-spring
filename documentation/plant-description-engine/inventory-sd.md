# Inventory - Service Description

## Abstract
This document describes an abstract service, which, if implemented by an application system, can be used query to an Inventory system for Inventory data related to application systems.

## Overview
This document describes an abstract Arrowhead Framework service meant to enable querying for Inventory data related to application systems.


## Service Interfaces
This section lists the interfaces that must be exposed by Inventory systems in alphabetical order. In particular, each
subsection names an abstract interface, an input type, an output type and a set of possible exceptions, in that
order. The input type is named inside parentheses, while the output type is preceded by a colon. Input and
output types are only denoted when accepted or returned, respectively, by the interface in question.
All abstract data types named in this section are defined in the [Information model](#information-model) section.

### interface GetInventoryData(InventoryId): [InventoryData](#struct-inventorydata)
Called to acquire the application system's inventory data, identified by `ÌnventoryId`.

### interface GetInventorySystems(): [SystemList](#struct-systemlist)
Called to acquire for systems present in the Inventory system.

## Information model
Here, all data objects that can be part of Inventory service calls are listed in alphabetic order. Note that each
subsection, which describes one type of object, begins with the struct or union keywords. The former is used to
denote a collection of named fields, each with its own data type, while the latter is used to express that a value
is allowed to be any one out of a number of listed variant types. As a complement to the explicitly defined types
in this section, there is also a list of implicit [primitive](#primitives) types,
which are used to represent things like hashes and identifiers.

### struct InventoryData

| Field | Type | Description |
| ----- | ---- | ----------- |
| `data` | Custom | System specific data - key-value pairs |

### struct System

| Field | Type | Description | Mandatory |
| ----- | ---- | ----------- | --------- |
| `inventoryId` | String | The system's Id in this Inventory system | `true` |
| `systemName` | String | Identity of the system | `false` |
| `metadata` | Custom | Metadata - key-value pairs | `false` |

### struct SystemList

| Field | Type | Description | Mandatory |
| ----- | ---- | ----------- | --------- |
| `count` | Integer | Number of records found | `true` |
| `data` | Array\<[System](#struct-system)> | Array of [Systems](#struct-system) that the Inventory contains | true |

### Primitives
Types and structures mentioned throughout this document that are assumed to be available to implementations
of this service. The concrete interpretations of each of these types and structures must be provided by any IDD
document claiming to implement this service.

| Type | Description |
| ---- | ----------- |
| Array \<A> | An ordered collection of elements, where each element conforms to type A. Empty arrays are omitted. |
| Custom | Any suitable type chosen by the implementor of the service.|
| String | An arbitrary UTF-8 string. |
