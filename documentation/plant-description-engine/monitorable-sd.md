# Monitorable - Service Description

## Abstract
This document describes an abstract service, which, if implemented by an application system, can be used by core systems to monitor the application system.

## Overview
This document describes an abstract Arrowhead Framework service meant to enable monitoring of application systems by core systems.
The service should be produced by all systems that are intended to be monitored by a core system.



## Service Interfaces
This section lists the interfaces that must be exposed by Monitorable systems in alphabetical order. In particular, each
subsection names an abstract interface, an input type, an output type and a set of possible exceptions, in that
order. The input type is named inside parentheses, while the output type is preceded by a colon. Input and
output types are only denoted when accepted or returned, respectively, by the interface in question.
All abstract data types named in this section are defined in the [Information model](#information-model) section.

### interface GetInventoryId(): [InventoryId](#struct-inventoryid)
Called to acquire the application systems inventory Id.

### interface GetSystemData(): [SystemData](#struct-systemdata)
Called to acquire the latest copy of the application systems monitorable system data.

### interface Ping(): OK
Called to check that the application system is alive and responding to service requests.

## Information model
Here, all data objects that can be part of Monitorable service calls are listed in alphabetic order. Note that each
subsection, which describes one type of object, begins with the struct or union keywords. The former is used to
denote a collection of named fields, each with its own data type, while the latter is used to express that a value
is allowed to be any one out of a number of listed variant types. As a complement to the explicitly defined types
in this section, there is also a list of implicit [primitive](#primitives) types,
which are used to represent things like hashes and identifiers.

### struct InventoryId

| Field | Type | Description |
| ----- | ---- | ----------- |
| `id` | String | The system's Id in an Inventory system |

### struct SystemData

| Field | Type | Description |
| ----- | ---- | ----------- |
| `data` | Custom | System specific data - key-value pairs |


### Primitives
Types and structures mentioned throughout this document that are assumed to be available to implementations
of this service. The concrete interpretations of each of these types and structures must be provided by any IDD
document claiming to implement this service.

| Type | Description |
| ---- | ----------- |
| Custom | Any suitable type chosen by the implementor of the service.|
| String | An arbitrary UTF-8 string. |

