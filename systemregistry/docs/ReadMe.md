# System Registry 
<a name="systemregistry" />


## System Design Description Overview
<a name="systemregistry_sdd" />

This System provides the database, which stores information related to the System of the currently actively offered Services within the Local Cloud.

The purpose of this System is therefore to allow:
-	Devices to register which Systems they offer at the moment, making this announcement available to other Application Systems on the network. 
-	They are also allowed to remove or update their entries when it is necessary. 
-   Generate a client certificate which can be used by the System to offer Services


## Services and Use Cases
<a name="systemregistry_usecases" />

This System provides two Core Service the __system registration__ and __de-registration__

The __register__ method is used to register a system. The system will contain various metadata as well as a physical endpoint. 
The various parameters are representing the endpoint information that should be registered.

The __unregister__ method is used to unregister system instances that were previously registered in the Registry. 
The instance parameter is representing the endpoint information that should be removed.


## Security
<a name="systemregistry_security" />

This System can be secured via the HTTPS protocol. If it is started in secure mode, it verifies whether the Application System possesses a proper X.509 identity certificate and whether that certificate is Arrowhead compliant in its making. This certificate structure and creation guidelines ensure:
-	Application System is properly bootstrapped into the Local Cloud
-	The Application System indeed belongs to this Local Cloud
-	The Application System then automatically has the right to register its Services in the Registry.

If these criteria are met, the Application System’s registration or removal message is processed. An Application System can only delete or alter entries that contain the Device as the System Provider in the entry. 


## Endpoints
<a name="systemregistry_endpoints" />

The System Registry offers three types of endpoints. Client, Management and Private.

Swagger API documentation is available on: `https://<host>:<port>` <br />
The base URL for the requests: `http://<host>:<port>/systemregistry`

### Onboarding endpoint description<br />
<a name="systemregistry_endpoints_onboarding" />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Onboard with Name](#systemregistry_endpoints_onboarding_name)    | /onboarding/name      | POST   | [SystemOnboardingWithNameRequest](#datastructures_systemregistry_onboarding_with_name_request) | [SystemOnboardingWithNameResponse](#datastructures_systemregistry_onboarding_with_name_response) |
| [Onboard with CSR](#systemregistry_endpoints_onboarding_csr) | /onboarding/csr   | POST   | [SystemOnboardingWithCsrRequest](#datastructures_systemregistry_onboarding_with_csr_request) | [SystemOnboardingWithCsrResponse](#datastructures_systemregistry_onboarding_with_csr_response) |


### Client endpoint description<br />
<a name="systemregistry_endpoints_client" />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#systemregistry_endpoints_get_echo)     | /echo       | GET    | -     | OK     |
| [Query](#systemregistry_endpoints_post_query)    | /query      | POST   | [SystemQueryForm](#datastructures_systemqueryform) | [SystemQueryList](#datastructures_systemquerylist) |
| [Register](#systemregistry_endpoints_post_register) | /register   | POST   | [SystenRegistryEntry](#datastructures_systemregistryentry) | [SystenRegistryEntry](#datastructures_systemregistryentry) |
| [Unregister](#systemregistry_delete_unregister) | /unregister | DELETE | System Name, Address and Port in query parameters| OK |

### Detailed description<br />

A detailed description of public, management and private endpoints is available in the release notes.

### Onboard with Name
<a name="systemregistry_endpoints_onboarding_name" />

```
POST /onboarding/name
```

Creates a CSR on behalf of the client, registers the system and eventually returns a system certificate which is valid in the Arrowhead local cloud.

#### Request
<a name="datastructures_systemeregistry_onboarding_with_name_request" />

```json
{
  "certificateCreationRequest": {
    "commonName": "string",
    "keyPairDTO": {
      "keyAlgorithm": "string",
      "keyFormat": "string",
      "privateKey": "string",
      "publicKey": "string"
    }
  },
  "provider": {
    "address": "string",
    "authenticationInfo": "string",
    "deviceName": "string",
    "macAddress": "string"
  },
  "system": {
    "address": "string",
    "authenticationInfo": "string",
    "port": 0,
    "systemName": "string"
  },
  "endOfValidity": "string",
  "metadata": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "version": 0
}
```

| Field | Description |
| ----- | ----------- |
| `commonName` | The common name field for the new certificate |
| `keyAlgorithm` | The key algorithm of the provided keys |
| `keyFormat` | The key format of the provided keys |
| `privateKey` | Base64 encoded private key |
| `publicKey` | Base64 encoded public key |
| `address` | The IP address of the device/system |
| `authenticationInfo` | Base64 encoded public key of the certificate this device/system |
| `deviceName` | The device name |
| `macAddress` | The MAC address of the device |
| `systemName` | The system name |
| `port` | The port under which this system's services are available |
| `endOfValidity` | The validity of this entry |
| `metadata` | Various meta information as map |
| `version` | The version of this entry |

#### Response
<a name="datastructures_systemregistry_onboarding_with_name_response" />

```json
{
  "certificateResponse": {
    "certificate": "string",
    "certificateFormat": "string",
    "certificateType": "AH_SYSTEM",
    "keyPairDTO": {
      "keyAlgorithm": "string",
      "keyFormat": "string",
      "privateKey": "string",
      "publicKey": "string"
    }
  },
  "createdAt": "string",
    "provider": {
      "address": "string",
      "authenticationInfo": "string",
      "createdAt": "string",
      "deviceName": "string",
      "id": 0,
      "macAddress": "string",
      "updatedAt": "string"
    },
    "system": {
      "address": "string",
      "authenticationInfo": "string",
      "createdAt": "string",
      "id": 0,
      "port": 0,
      "systemName": "string",
      "updatedAt": "string"
    },
  "endOfValidity": "string",
  "id": 0,
  "metadata": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "updatedAt": "string",
  "version": 0
}
```

| Field | Description |
| ----- | ----------- |
| `certificate` | The Base64 device certificate for the next step |
| `certificateFormat` | The certificate format (usually X.509) |
| `certificateType` | The certificate type. Always AH_SYSTEM for this operation |
| `keyAlgorithm` | The key algorithm of the provided keys |
| `keyFormat` | The key format of the provided keys |
| `privateKey` | Base64 encoded private key |
| `publicKey` | Base64 encoded public key |

Additionally all fields from [SystemRegistryEntry](#datastructures_systemregistryentry) are returned. 

### Onboard with CSR
<a name="systemregistry_endpoints_onboarding_csr" />

```
POST /onboarding/csr
```

Signs the CSR, registers the device and eventually returns a device certificate which may be used in the next step of the onboarding controller.

#### Request
<a name="datastructures_systemregistry_onboarding_with_csr_request" />

```json
{
  "certificateSigningRequest": "string",
  "provider": {
    "address": "string",
    "authenticationInfo": "string",
    "deviceName": "string",
    "macAddress": "string"
  },
  "system": {
    "address": "string",
    "authenticationInfo": "string",
    "port": 0,
    "systemName": "string"
  },
  "endOfValidity": "string",
  "metadata": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "version": 0
}
```

| Field | Description |
| ----- | ----------- |
| `certificateSigningRequest` | Base64 encoded certificate signing request |
| `address` | The IP address of the device/system |
| `authenticationInfo` | Base64 encoded public key of the certificate this device/system |
| `deviceName` | The device name |
| `macAddress` | The MAC address of the device |
| `systemName` | The system name |
| `port` | The port under which this system's services are available |
| `endOfValidity` | The validity of this entry |
| `metadata` | Various meta information as map |
| `version` | The version of this entry |

#### Response
<a name="datastructures_systemregistry_onboarding_with_csr_response" />

```json
{
  "certificateResponse": {
    "certificate": "string",
    "certificateFormat": "string",
    "certificateType": "AH_SYSTEM",
    "keyPairDTO": {
      "keyAlgorithm": "string",
      "keyFormat": "string",
      "privateKey": "string",
      "publicKey": "string"
    }
  },
  "createdAt": "string",
    "provider": {
      "address": "string",
      "authenticationInfo": "string",
      "createdAt": "string",
      "deviceName": "string",
      "id": 0,
      "macAddress": "string",
      "updatedAt": "string"
    },
    "system": {
      "address": "string",
      "authenticationInfo": "string",
      "createdAt": "string",
      "id": 0,
      "port": 0,
      "systemName": "string",
      "updatedAt": "string"
    },
  "endOfValidity": "string",
  "id": 0,
  "metadata": {
    "additionalProp1": "string",
    "additionalProp2": "string",
    "additionalProp3": "string"
  },
  "updatedAt": "string",
  "version": 0
}
```

| Field | Description |
| ----- | ----------- |
| `certificate` | The Base64 device certificate for the next step |
| `certificateFormat` | The certificate format (usually X.509) |
| `certificateType` | The certificate type. Always AH_DEVICE for this operation |
| `keyAlgorithm` | The key algorithm of the provided keys |
| `keyFormat` | The key format of the provided keys |
| `privateKey` | Base64 encoded private key. Always empty for this operation |
| `publicKey` | Base64 encoded public key |

Additionally all fields from [SystemRegistryEntry](#datastructures_systemregistryentry) are returned.
