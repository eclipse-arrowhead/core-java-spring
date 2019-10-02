# Arrowhead Framework 4.1.3

[Arrowhead](http://www.arrowhead.eu/) (and its continuation, [Productive4.0](https://productive40.eu/)) is an ambitious holistic innovation project,
 meant to open the doors to the potentials of Digital Industry and to maintain a leadership position of the industries in Europe. All partners involved will work on creating the capability to efficiently design and integrate hardware and software of Internet of Things (IoT) devices. Linking the real with the digital world takes more than just adding software to the hardware.
 

## Disclaimer
Please be aware, that 4.1.3 is __NOT__ backwards compatible with 4.1.2. If you have older systems please refer to the [Migration Guide](#migration) 
 
## Table of Contents
1. [Quick Start Guide](#quickstart)
2. [Migration Guide 4.1.2 -> 4.1.3](#migration)
3. [How to Contribute](#howtocontribute)
4. [Documentation](#documentation) 
    1. [Service Registry](#serviceregistry)
       * [System Design Description Overview](#serviceregistry_sdd)
       * [Services and Use Cases](#serviceregistry_usecases)
       * [Security](#serviceregistry_security)
       * [Endpoints](#serviceregistry_endpoints)
           * [Client](#serviceregistry_endpoints_client)
           * [Management](#serviceregistry_endpoints_mgmt)
           * [Private](#serviceregistry_endpoints_private) 
    2. [Authorization](#authorization)
       * [System Design Description Overview](#authorization_sdd)
           * [Token Generation](#asd) 
       * [Endpoints](#authorization_endpoints)
 
<a name="quickstart" />

## Quick Start Guide

### Docker

Placeholder

### Compile source code and manually install MySQL and Maven.
#### Requirements

The project has the following dependencies:
* JRE/JDK 11 [Download from here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
* Maven 3.5+ [Download from here](http://maven.apache.org/download.cgi) | [Install guide](https://www.baeldung.com/install-maven-on-windows-linux-mac)
* MySQL server 5.7+ (other SQL databases can work with Hibernate ORM, but the `common module pom.xml` has to include the appropriate connector 
dependency to use them)

Verify that you have Java (```java -version```), Maven (```mvn -version```), MySQL installed properly!

Pull this code and enter the directory. 
```git clone https://github.com/arrowhead-f/core-java-spring.git```

Run the MySQL script which is in the ```scripts``` folder. If you won't run this script first, the project won't build. 

```cd core-java-spring```

Execute ```mvn install``` command. Wait until the build succeeds. 
This command builds all available projects. <br />

After succeeding enter the scripts folder and execute ```start_core_systems.sh``` or ```start_core_systems.bat``` depending on your operating system.
- serviceregistry/target directory.
```cd serviceregistry/target``` <br />and execute: ```java -jar arrowhead-serviceregistry-4.1.3.jar```
- authorization/target directory. ```cd authorization/target``` <br />and execute: ```java -jar arrowhead-authorization-4.1.3.jar``` 
- orchestrator/target directory. ```orchestrator/target``` <br />and execute: ```java -jar arrowhead-orchestrator-4.1.3.jar```


Wait until servers start...

Service Registry will be available on ```localhost:8443``` <br />
Authorization will be available on ```localhost:8445``` <br />
Orchestrator will be available on ```localhost:8441``` <br />
Event Handler will be available on ```localhost:8455``` <br />
Gatekeeper will be available on ```localhost:8449``` <br />
Gateway will be available on ```localhost:8453``` <br />

Swagger with API documentation is available in the root route.

Enjoy! ;)


<a name="migration" />

## Migration Guide 4.1.2 -> 4.1.3

4.1.3 is NOT backwards compatible with 4.1.2! Earlier it was redundant and contained gaps. Now the database and the endpoints are redesigned, they are clean, more logical and easier to use.

You can migrate your existing database manually. See the [Quick Start Guide](#quickstart), how to deploy the Core Systems.

Major endpoint changes:<br />

### Service Registry:

The following endpoints no longer exist, instead use the ones on the right:

 * PUT /mgmt/services -> POST /serviceregistry/mgmt/services
 * PUT /mgmt/systems -> POST /serviceregistry/mgmt/systems
 * GET /serviceregistry/mgmt/systemId/{systemId} -> GET /serviceregistry/mgmt/systems/{id}
 * GET /serviceregistry/mgmt/serviceId/{serviceId}/providers
 * PUT /serviceregistry/mgmt/query -> POST /serviceregistry/query
 * PUT /serviceregistry/mgmt/subscriptions/{id}
 * PUT /serviceregistry/support/remove -> DELETE /serviceregistry/unregister
 * DELETE /serviceregistry/mgmt/all
 
 
 * __/register__ - data structure changed
 
Old payload, which is no longer usable
 ```json
{
  "providedService" : {
    "serviceDefinition" : "IndoorTemperature",
    "interfaces" : [ "JSON", "XML" ],
    "serviceMetadata" : {
      "unit" : "celsius"
    }
  },
  "provider" : {
    "systemName" : "InsecureTemperatureSensor",
    "address" : "192.168.0.2",
    "port" : 8080
  },
  "serviceURI" : "temperature",
  "version" : 1,
  "udp" : false,
  "ttl" : 0
} 
 ```

New payload - you can easily map the old fields to the new ones.
 ```json
{
  "serviceDefinition": "IndoorTemperature",
  "providerSystem": {
  "systemName": "InsecureTemperatureSensor",
  "address": "192.168.0.2",
  "port": 8080,
  "authenticationInfo": "eyJhbGciOiJIUzI1Ni..."
 },
  "serviceUri": "temperature",
  "endOfValidity": "2019-12-05T12:00:00",
  "secure": "TOKEN",
  "metadata": {
    "unit": "celsius"
 },
  "version": 1,
  "interfaces": [
    "HTTP-SECURE-JSON"
 ]
}
```
 
### Authorization
 * __/mgmt/intracloud__ - data structure changed
 * __/mgmt/intercloud__ - data structure changed
 
### Orchestration
 Store based orchestration is available for now.
 * __/mgmt/store__ - data structure changed
 

<a name="howtocontribute" />

## How to Contribute

### Open Development

All work on Arrowhead repositories happens directly on GitHub. Both core team members and external contributors send pull requests which go through the same review process.

### Branch Organization

The latest version of the core systems are available in the ```master``` branch. The code for the next release is merged in the ```development``` branch. If you would like to contribute, please check out the ```development``` branch. Create a new branch from ```development```. Don't forget do write documentation, unit and integration tests. When finished, create a pull request back into ```development```. If accepted, your contribution will be in the next release. :)

### Bugs

#### Where To Find Known Issues
We are using [GitHub Issues](https://github.com/arrowhead-f/core-java-spring/issues) for our public bugs. We keep a close eye on this and try to make it clear when we have an internal fix in progress. Before filing a new task, try to make sure your problem doesn’t already exist.

#### Reporting New Issues
The best way to get your bug fixed is to provide a reduced test case.

#### How to Get in Touch
Join our developer team on Slack. Write an email to [szvetlin@aitia.ai](mailto:szvetlin@aitia.ai) for an invite. 

<a name="documentation" />

## Documentation 
 
<a name="serviceregistry" />
 
### Service Registry 
 
<a name="serviceregistry_sdd" />
 
#### System Design Description Overview

This System provides the database, which stores information related to the currently actively offered Services within the Local Cloud.

The purpose of this System is therefore to allow:
-	Application Systems to register what Services they offer at the moment, making this announcement available to other Application Systems on the network. 
-	They are also allowed to remove or update their entries when it is necessary. 
-	All Application Systems can utilize the lookup functionality of the Registry to find Public Core System Service offerings in the network, otherwise the Orchestrator has to be used. 

However, it is worth noting, that within this generation the lookup functionality of Services is integrated within the “orchestration process”. Therefore, in the primary scenario, when an Application System is looking for a Service to consume, it shall ask the Orchestrator System via the Orchestration Service to locate one or more suitable Service Providers and help establish the connection based on metadata submitted in the request. Direct lookups from Application Systems within the network is not advised in this generation, due to security reasons.

However, the lookup of other Application Systems and Services directly is not within the primary use, since access will not be given without the Authorization JWT (JSON Web Token). The use of the TokenGeneration is restricted to the Orchestrator for general System accountability reasons. 

<a name="serviceregistry_usecases" />

#### Services and Use Cases

This System only provides one Core Service the __Service Discovery__

There are two use case scenarios connected to the Service Registry. 
-	Service registration, de-registration
-	Service Registry querying (lookup)

The __register__ method is used to register services. The services will contain various metadata as well as a physical endpoint. 
The various parameters are representing the endpoint information that should be registered.

The __unregister__ method is used to unregister service instances that were previously registered in the Registry. 
The instance parameter is representing the endpoint information that should be removed.

The __query__ method is used to find and translate a symbolic service name into a physical endpoint, for example an IP address and a port.
The query parameter is used to request a subset of all the registered services fulfilling the demand of the user of the service.
The returned listing contains service endpoints that have been fulfilling the query.


There is another functionality that does not bound to any Services, just an internal part of the Service Registry. There are two optional cleanup tasks within the Service Registry, which can be used to remove old, inactive service offerings. The first task is based on pinging the service provider and if the provider does not respond to the ping, its offered services will be deleted. The second task is based on a feature, called “Time to Live”. Service providers upon registration can provide a timestamp called “end_of_validity” number, which specifies how long the service will be offered by the provider, making the service de-registrations unnecessary, if this task is active. The task is used to remove expired services. The third task is using a feature called "Heartbeat" (Not yet implemented), where the Service provider periodically signals to the Service Registry that it is still alive. When it misses it will be removed. All of these internal tasks can be configured in the application.properties file.

<a name="serviceregistry_security" />

#### Security

This System can be secured via the HTTPS protocol. If it is started in secure mode, it verifies whether the Application System possesses a proper X.509 identity certificate and whether that certificate is Arrowhead compliant in its making. This certificate structure and creation guidelines ensure:
-	Application System is properly bootstrapped into the Local Cloud
-	The Application System indeed belongs to this Local Cloud
-	The Application System then automatically has the right to register its Services in the Registry.

If these criteria are met, the Application System’s registration or removal message is processed. An Application System can only delete or alter entries that contain the Application System as the Service Provider in the entry. 

<a name="serviceregistry_endpoints />

#### Endpoints

The Service Registry offers three types of endpoints. Client, Management and Private.

Swagger API documentation is available on: https://<host>:<port>
The base URL for the requests: http://<host>:<port>/serviceregistry

Client endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#serviceregistry_endpoints_get_echo)     | /echo       | GET    | -     | OK     |
| [Query](#serviceregistry_endpoints_post_query)    | /query      | POST   | [ServiceQueryForm](#datastructures_servicequeryform) | [ServiceQueryList](#datastructures_servicequerylist) |
| [Register](#serviceregistry_endpoints_post_register) | /register   | POST   | [ServiceRegistryEntry](#datastructures_serviceregistryentry) | [ServiceRegistryEntry](#datastructures_serviceregistryentry) |
| [Unregister](#serviceregistry_delete_unregister) | /unregister | DELETE | Address, Port, Service Definition, System Name in query parameters| OK |

Private endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| Query System | /query/system| POST | System | System |
| Query System By ID | /query/system/{id} | GET | ID | System|

Management endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| Get all entries | /mgmt/ | GET | - | ServiceRegistryEntryList |
| Add an entry | /mgmt/ | POST | ServiceRegistryEntry | ServiceRegistryEntry |
| Get an entry by ID | /mgmt/{id} | GET | ServiceID | ServiceRegistryEntry |
| Replace an entry by ID | /mgmt/{id} | PUT | ServiceRegistryEntry | ServiceRegistryEntry |
| Modify an entry by ID | /mgmt/{id} | PATCH | Key value pairs of ServiceRegistryEntry | ServiceRegistryEntry |
| Delete and entry by ID | /mgmt/{id} | DELETE | ServiceRegistryEntryID | - |
| Get grouped view | /mgmt/grouped | GET | - | ServiceRegistryGrouped |
| Get Service Registry Entries by Service Definition | /mgmt/servicedef/<br />{serviceDefinition} | GET | ServiceDefinition | ServiceRegistryEntryList |
| Get all services | /mgmt/services | GET | - | ServiceList |
| Add a service | /mgmt/services | POST | Service | Service |
| Get a service by ID | /mgmt/services/{id} | GET | ServiceID | Service |
| Replace a service by ID | /mgmt/services/(id} | PUT | Service | Service |
| Modify a service by ID | /mgmt/services/{id} | PATCH | Key value pairs of Service | Service |
| Delete a service by ID | /mgmt/services/{id} | DELETE | ServiceID | - |
| Get all systems | /mgmt/systems | GET | - | SystemList |
| Add a system | /mgmt/systems | POST | System | System |
| Get a system by ID | /mgmt/systems/{id} | GET | SystemID | System |
| Replace a system by ID | /mgmt/systems/{id} | PUT | System | System |
| Modify a system by ID | /mgmt/systems/{id} | PATCH | Key value pairs of System | System |
| Delete a system by ID | /mgmt/systems/{id} | DELETE | SystemID | - |

<a name="serviceregistry_endpoints_get_echo" />

##### Echo 
```
GET /serviceregistry/echo
```

Returns a "Got it" message with the purpose of testing the core service availability.

> **Note:** 4.1.2 version: GET /serviceregistry

<a name="serviceregistry_endpoints_post_query" />

##### Query
```
POST /serviceregistry/query
```

Returns ServiceQueryList that fits the input specification. Mainly used by the Orchestrator.

<a name="datastructures_servicequeryform" />

__ServiceQueryForm__ is the input
```json
{
 "serviceDefinitionRequirement": "string",
 "interfaceRequirements": [
   "string"
 ],
 "securityRequirements": [
   "NOT_SECURE"
 ],
 "metadataRequirements": {
   "additionalProp1": "string",
   "additionalProp2": "string",
   "additionalProp3": "string"
 },
 "versionRequirement": 0,
 "maxVersionRequirement": 0,
 "minVersionRequirement": 0,
 "pingProviders": true
}
```

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `serviceDefinitionRequirement` | Name of the required Service Definition | yes |
| `interfaceRequirements` | List of required interfaces | no |
| `securityRequirements` | List of required security settings | no |
| `metadataRequirements` | Key value pairs of required metadata | no |
| `versionRequirement` | Required version number | no |
| `maxVersionRequirement` | Maximum version requirement | no |
| `minVersionRequirement` | Minimum version requirement | no |
| `pingProviders` | Return only available providers | no |

> **Note:** Valid `interfaceRequirements` name pattern: protocol-SECURE or INSECURE format. (e.g.: HTTPS-SECURE-JSON)

> **Note:** Possible values for `securityRequirements` are:
> * NOT_SECURE
> * SECURE
> * TOKEN
> * not defined, if you don't want to filter on security type

<a name="datastructures_servicequerylist" />

Returns a __ServiceQueryList__
```json
{
 "serviceQueryData": [
   {
     "id": 0,
     "serviceDefinition": {
       "id": 0,
       "serviceDefinition": "string",
       "createdAt": "string",
       "updatedAt": "string"
     },
     "provider": {
       "id": 0,
       "systemName": "string",
       "address": "string",
       "port": 0,
       "authenticationInfo": "string",
       "createdAt": "string",
       "updatedAt": "string"
     },
     "serviceUri": "string",
     "endOfValidity": "string",
     "secure": "NOT_SECURE",
     "metadata": {
       "additionalProp1": "string",
       "additionalProp2": "string",
       "additionalProp3": "string"
     },
     "version": 0,
     "interfaces": [
       {
         "id": 0,
         "interfaceName": "string",
         "createdAt": "string",
         "updatedAt": "string"
       }
     ],
     "createdAt": "string",
     "updatedAt": "string"
    }
 ],
 "unfilteredHits": 0
}
```

| Field | Description |
| ----- | ----------- |
| `serviceQueryData` | The array of objects containing the data |
| `id` | ID of the entry, used by the Orchestrator |
| `serviceDefinition` | Service Definition |
| `provider` | Provider System |
| `serviceUri` | URI of the service |
| `endOfValidity` | Service is available until this timestamp. |
| `secure` | Security info |
| `metadata` | Metadata |
| `version` | Version of the Service |
| `interfaces` | List of interfaces the Service supports |
| `createdAt` | Creation date of the entry |
| `updatedAt` | When the entry was last updated |
| `unfilteredHits` | Number of hits based on service definition without filters |

> **Note:** 4.1.2 version: PUT /serviceregistry /query <br />
            This version always returned the records in an array of JSON objects. The response did not contain any information about the unfiltered hits and the objects did not contain any modification related timestamp information. Interfaces and metadata were bound to the service definition and security type was not defined. Service Registry object did contain an unnecessary "udp" flag beside the interface definition.

<a name="serviceregistry_endpoints_post_register" />

##### Register
```
POST /serviceregistry/register
```

Registers a service. A provider is allowed to register only its own services. It means that provider
system name and certificate common name must match for successful registration.

<a name=datastructures_serviceregistryentry" />

__ServiceRegistryEntry__ is the input
```json
{
  "serviceDefinition": "string",
  "providerSystem": {
    "systemName": "string",
    "address": "string",
    "port": 0,
    "authenticationInfo": "string"
  },
  "serviceUri": "string",
  "endOfValidity": "string",
  "secure": "NOT_SECURE",
  "metadata": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "version": 0,
  "interfaces": [
    "string"
  ]
}
```

| Field | Description | Mandatory |
| ----- | ----------- | --------- |
| `serviceDefinition` | Service Definition | yes |
| `providerSystem` | Provider System | yes |
| `serviceUri` |  URI of the service | yes |
| `endOfValidity` | Service is available until this timestamp | no |
| `secure` | Security info | no |
| `metadata` | Metadata | no |
| `version` | Version of the Service | no |
| `interfaces` | List of the interfaces the Service supports | yes |

> **Note:** Valid `interfaces` name pattern: protocol-SECURE or INSECURE format. (e.g.: HTTPS-SECURE-JSON)

> **Note:** Possible values for `secure` are:
> * NOT_SECURE (default value if field is not defined)
> * SECURE
> * TOKEN

Returns a __ServiceRegistryEntry__

```json
{
  "id": 0,
  "serviceDefinition": {
    "id": 0,
    "serviceDefinition": "string",
    "createdAt": "string",
    "updatedAt": "string"
  },
  "provider": {
    "id": 0,
    "systemName": "string",
    "address": "string",
    "port": 0,
    "authenticationInfo": "string",
    "createdAt": "string",
    "updatedAt": "string"
  },
  "serviceUri": "string",
  "endOfValidity": "string",
  "secure": "NOT_SECURE",
  "metadata": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "version": 0,
  "interfaces": [
    {
      "id": 0,
      "interfaceName": "string",
      "createdAt": "string",
      "updatedAt": "string"
 }
 ],
 "createdAt": "string",
 "updatedAt": "string"
}
```

| Field | Description |
| ----- | ----------- |
| `id` | ID of the ServiceRegistryEntry |
| `serviceDefinition` | Service Definition |
| `provider` | Provider System |
| `serviceUri` |  URI of the service |
| `endOfValidity` | Service is available until this timestamp |
| `secure` | Security info |
| `metadata` | Metadata |
| `version` | Version of the Service |
| `interfaces` | List of the interfaces the Service supports |
| `createdAt` | Creation date of the entry |
| `updatedAt` | When the entry was last updated |

> **Note:** 4.1.2 version: POST /serviceregistry /register <br />
            In this version interfaces and metadata were bound to the service definition and security type was not
            defined at all. The response object did not contain any modification related time stamp information.
            Service Registry object did contain an unnecessary "udp" flag beside the interface definition.
            
<a name="serviceregistry_delete_unregister" />
            
##### Unregister 
```
DELETE /serviceregistry/unregister
```

Removes a registered service. A provider is allowed to unregister only its own services. It means
that provider system name and certificate common name must match for successful unregistration.

Query params:
* service_definition - name of the service to be removed
* system_name - name of the provider
* address 
* port

> **Note:** 4.1.2 version: PUT /serviceregistry/remove <br />
            In this version the input was a JSON object with many unnecessary information.