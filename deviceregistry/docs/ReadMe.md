# Device Registry 
<a name="deviceregistry"/><br />

## System Design Description Overview
<a name="deviceregistry_sdd" /><br />

This System provides the database, which stores information related to the Devices within the Local Cloud.

The purpose of this System is therefore to allow:
-	Devices to register themselves, making this announcement available to other Application Systems on the network. 
-	They are also allowed to remove or update their entries when it is necessary. 
-   Generate a client certificate which can be used by the Device to register its Systems

## Services and Use Cases
<a name="deviceregistry_usecases" />

This System provides two Core Service: the __device registration__ and __de-registration__.
Further it provides two Onboarding Services: the __onboarding with name__ and __onboarding with CSR__.

The __register__ method is used to register a device. The device will contain various metadata as well as a physical endpoint. 
The various parameters are representing the endpoint information that should be registered.

The __unregister__ method is used to unregister device instances that were previously registered in the Registry. 
The instance parameter is representing the endpoint information that should be removed.

The __onboarding__ methods are used to register a device and to retrieve a device certificate which must be used on the next step of the onboarding procedure.

## Security
<a name="deviceregistry_security" />

This System can be secured via the HTTPS protocol. If it is started in secure mode, it verifies whether the Application System possesses a proper X.509 identity certificate and whether that certificate is Arrowhead compliant in its making. This certificate structure and creation guidelines ensure:
-	Application System is properly bootstrapped into the Local Cloud
-	The Application System indeed belongs to this Local Cloud
-	The Application System then automatically has the right to register its Systems in the Registry.

If these criteria are met, the Application System’s registration or removal message is processed. An Application System can only delete or alter entries that contain the Device as the System Provider in the entry. 


## Endpoints
<a name="deviceregistry_endpoints" />

The System Registry offers four types of endpoints. Onboarding, Client, Management and Private.

Swagger API documentation is available on: `https://<host>:<port>` <br />
The base URL for the requests: `http://<host>:<port>/deviceregistry`

### Onboarding endpoint description<br />
<a name="deviceregistry_endpoints_onboarding" />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Onboard with Name](#deviceregistry_endpoints_onboarding_name)    | /onboarding/name      | POST   | [DeviceOnboardingWithNameRequest](#datastructures_deviceregistry_onboarding_with_name_request) | [DeviceOnboardingWithNameResponse](#datastructures_deviceregistry_onboarding_with_name_response) |
| [Onboard with CSR](#deviceregistry_endpoints_onboarding_csr) | /onboarding/csr   | POST   | [DeviceOnboardingWithCsrRequest](#datastructures_deviceregistry_onboarding_with_csr_request) | [DeviceOnboardingWithCsrResponse](#datastructures_deviceregistry_onboarding_with_csr_response) |


### Client endpoint description<br />
<a name="deviceregistry_endpoints_onboarding" />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#deviceregistry_endpoints_get_echo)     | /echo       | GET    | -     | OK     |
| [Query](#deviceregistry_endpoints_post_query)    | /query      | POST   | [DeviceQueryForm](#datastructures_systemqueryform) | [DeviceQueryList](#datastructures_devicequerylist) |
| [Register](#deviceregistry_endpoints_post_register) | /register   | POST   | [DeviceRegistryEntry](#datastructures_deviceregistryentry) | [DeviceRegistryEntry](#datastructures_deviceregistryentry) |
| [Unregister](#deviceregistry_delete_unregister) | /unregister | DELETE | Device Name and Mac Address in query parameters| OK |

### Detailed description<br />

A detailed description of management and private endpoints is available in the release notes.

### Onboard with Name
<a name="deviceregistry_endpoints_onboarding_name" />

```
POST /onboarding/name
```

Creates a CSR on behalf of the client, registers the device and eventually returns a device certificate which may be used in the next step of the onboarding controller.

#### Request
<a name="datastructures_deviceregistry_onboarding_with_name_request" />

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
  "device": {
    "address": "string",
    "authenticationInfo": "string",
    "deviceName": "string",
    "macAddress": "string"
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
| `address` | The optional IP address of the device |
| `authenticationInfo` | Base64 encoded public key of the certificate this device |
| `deviceName` | The device name |
| `macAddress` | The MAC address of the device |
| `endOfValidity` | The validity of this entry |
| `metadata` | Various meta information as map |
| `version` | The version of this entry |

#### Response
<a name="datastructures_deviceregistry_onboarding_with_name_response" />

```json
{
  "certificateResponse": {
    "certificate": "string",
    "certificateFormat": "string",
    "certificateType": "AH_DEVICE",
    "keyPairDTO": {
      "keyAlgorithm": "string",
      "keyFormat": "string",
      "privateKey": "string",
      "publicKey": "string"
    }
  },
  "createdAt": "string",
  "device": {
    "address": "string",
    "authenticationInfo": "string",
    "createdAt": "string",
    "deviceName": "string",
    "id": 0,
    "macAddress": "string",
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
| `privateKey` | Base64 encoded private key |
| `publicKey` | Base64 encoded public key |

Additionally all fields from [DeviceRegistryEntry](#datastructures_deviceregistryentry) are returned. 

### Onboard with CSR
<a name="deviceregistry_endpoints_onboarding_csr" />

```
POST /onboarding/csr
```

Signs the CSR, registers the device and eventually returns a device certificate which may be used in the next step of the onboarding controller.

#### Request
<a name="datastructures_deviceregistry_onboarding_with_csr_request" />

```json
{
  "certificateSigningRequest": "string",
  "device": {
    "address": "string",
    "authenticationInfo": "string",
    "deviceName": "string",
    "macAddress": "string"
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
| `address` | The optional IP address of the device |
| `authenticationInfo` | Base64 encoded public key of the certificate this device |
| `deviceName` | The device name |
| `macAddress` | The MAC address of the device |
| `endOfValidity` | The validity of this entry |
| `metadata` | Various meta information as map |
| `version` | The version of this entry |

#### Response
<a name="datastructures_deviceregistry_onboarding_with_csr_response" />

```json
{
  "certificateResponse": {
    "certificate": "string",
    "certificateFormat": "string",
    "certificateType": "AH_DEVICE",
    "keyPairDTO": {
      "keyAlgorithm": "string",
      "keyFormat": "string",
      "privateKey": "string",
      "publicKey": "string"
    }
  },
  "createdAt": "string",
  "device": {
    "address": "string",
    "authenticationInfo": "string",
    "createdAt": "string",
    "deviceName": "string",
    "id": 0,
    "macAddress": "string",
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

Additionally all fields from [DeviceRegistryEntry](#datastructures_deviceregistryentry) are returned. 
