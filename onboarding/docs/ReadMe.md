# Onboarding Controller
<a name="onboardingcontroller" /><br />

## System Design Description Overview
<a name="onboardingcontroller_sdd" /><br />

The purpose of this System is to be the entry board for the onboarding procedure. The onboarding controller sits at the edge of the Arrowhead local cloud. 
It is not only reachable from within the cloud by authorized systems, but also from the public through its "accept all" interfaces. 
Any client may authenticate itself through an Arrowhead certificate, through an authorized manufacturer certificate, or simply through a shared secret.

![onboarding_controller](/documentation/onboarding/onboarding_controller.png)

## Services and Use Cases
<a name="onboardingcontroller_usecases" />

The only use case is the onboarding procedure. 

The onboarding procedure is needed when a new device produced by any vendor (e.g. Siemens, Infineon, Bosch, etc.), containing a security controller (e.g. TPM), 
wants to interact with the Arrowhead local cloud. To assure that the cloud is not compromised upon the arrival of this new device, it is important to establish 
a chain of trust from the new hardware device, to its hosted application systems and their services. Thus, the onboarding procedure makes possible that 
the device, systems and services are authenticated and authorized to connect to the Arrowhead local cloud.

The use cases in which the external actor interacts with the Arrowhead local cloud during onboarding include:
* Initialize Device Onboarding (via the Onboarding Controller system)
* Register a Device in the DeviceRegistry (via the DeviceRegistry system)
* Register a System in the SystemRegistry (via the SystemRegistry system)
* Register a Service in the ServiceRegistry (via the ServiceRegistry system)
* Start normal operation (e.g., service lookup, service consumption, etc.)

The onboarding controller can either relay a ![PKCS #10 Certificate Signing Request](https://tools.ietf.org/html/rfc2986) or generate one on behalf of the client.


## Security
<a name="onboardingcontroller_security" />

This System can be secured via the HTTPS protocol. If it is started in secure mode, it verifies whether the Application System 
- possesses a proper X.509 identity certificate and whether that certificate is Arrowhead compliant
- possesses a proper X.509 identity certificate and whether that certificate is trusted by the CA
- provides a shared secret through ![HTTP Basic Authentication](https://tools.ietf.org/html/rfc7617)

## Endpoints
<a name="onboardingcontroller_endpoints" />

The Onboarding Controller offers two types of authentication (certificate or HTTP Basic) and two types operation (provide CSR or generate CSR), thus having four different endpoint. 
No management or private endpoint exists.

Swagger API documentation is available on: `https://<host>:<port>` <br />
The base URL for the requests: `https://<host>:<port>/onboarding`

The general scheme of the URLs is `https://<host>:<port>/onboarding/<authentication_type>/<operation_type>`

### Client endpoint description<br />
<a name="onboardingcontroller_endpoints" />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#onboardingcontroller_endpoints_get_echo)     | /echo       | GET    | -     | OK     |
| [Onboard with Name](#onboardingcontroller_endpoints_name) | /certificate/name   | POST   | [OnboardingWithNameRequest](#datastructures_onboarding_name_request) | [OnboardingWithNameResponse](#datastructures_onboarding_name_response) |
| [Onboard with Name](#onboardingcontroller_endpoints_name) | /sharedsecret/name | POST   | [OnboardingWithNameRequest](#datastructures_onboarding_name_request) | [OnboardingWithNameResponse](#datastructures_onboarding_name_response) |
| [Onboard with CSR](#onboardingcontroller_endpoints_csr)    | /certificate/csr      | POST   | [OnboardingWithCsrRequest](#datastructures_onboarding_csr_request) | [OnboardingWithCsrResponse](#datastructures_onboarding_csr_response) |
| [Onboard with CSR](#onboardingcontroller_endpoints_csr)    | /sharedsecret/csr      | POST   | [OnboardingWithCsrRequest](#datastructures_onboarding_csr_request) | [OnboardingWithCsrResponse](#datastructures_onboarding_csr_response) |


### Onboard with Name
<a name="onboardingcontroller_endpoints_name" />

```
POST /certificate/name
POST /sharedsecret/name
```

Creates a CSR on behalf of the client and eventually returns an onboarding certificate which may be used in the next step of the onboarding controller.

#### Request
<a name="datastructures_onboarding_name_request" />

```json
{
  "creationRequestDTO": {
    "commonName": "string",
    "keyPairDTO": {
      "keyAlgorithm": "string",
      "keyFormat": "string",
      "privateKey": "string",
      "publicKey": "string"
    }
  }
}
```

| Field | Description |
| ----- | ----------- |
| `commonName` | The common name field for the new certificate |
| `keyAlgorithm` | The key algorithm of the provided keys |
| `keyFormat` | The key format of the provided keys |
| `privateKey` | Base64 encoded private key |
| `publicKey` | Base64 encoded public key |

#### Response
<a name="datastructures_onboarding_name_response" />

```json
{
  "rootCertificate": "string",
  "intermediateCertificate": "string",
  "onboardingCertificate": {
    "certificate": "string",
    "certificateFormat": "string",
    "certificateType": "AH_ONBOARDING",
    "keyPairDTO": {
      "keyAlgorithm": "string",
      "keyFormat": "string",
      "privateKey": "string",
      "publicKey": "string"
    }
  },
  "deviceRegistry": {
    "service": "DEVICE_REGISTRY_ONBOARDING_WITH_NAME_SERVICE",
    "uri": "string"
  },
  "systemRegistry": {
    "service": "SYSTEM_REGISTRY_ONBOARDING_WITH_NAME_SERVICE",
    "uri": "string"
  },
  "serviceRegistry": {
    "service": "SERVICE_REGISTRY_REGISTER_SERVICE",
        "uri": "string"
  },
  "orchestrationService": {
    "service": "ORCHESTRATION_SERVICE",
    "uri": "string"
  }
}
```

| Field | Description |
| ----- | ----------- |
| `rootCertificate` | The Arrowhead master certificate |
| `intermediateCertificate` | The Arrowhead local cloud certificate |
| `onboardingCertificate` | The onboarding certificate for the next step |
| `certificateFormat` | The certificate format (usually X.509) |
| `certificateType` | The certificate type. Always AH_ONBOARDING for this operation |
| `keyAlgorithm` | The key algorithm of the provided keys |
| `keyFormat` | The key format of the provided keys |
| `privateKey` | Base64 encoded private key |
| `publicKey` | Base64 encoded public key |
| `service` | The service which is reachable under `uri` |
| `uri` | The uri under which the depicted `service` is reachable |


### Onboard with CSR
<a name="onboardingcontroller_endpoints_csr" />

```
POST /certificate/csr
POST /sharedsecret/csr
```

Creates a CSR on behalf of the client and eventually returns an onboarding certificate which may be used in the next step of the onboarding controller.

#### Request
<a name="datastructures_onboarding_csr_request" />

```json
{
  "certificateSigningRequest": "string"
}
```

| Field | Description |
| ----- | ----------- |
| `certificateSigningRequest` | Base64 encoded certificate signing request |

#### Response
<a name="datastructures_onboarding_csr_response" />

```json
{
  "rootCertificate": "string",
  "intermediateCertificate": "string",
  "onboardingCertificate": {
    "certificate": "string",
    "certificateFormat": "string",
    "certificateType": "AH_ONBOARDING",
    "keyPairDTO": {
      "keyAlgorithm": "string",
      "keyFormat": "string",
      "privateKey": "string",
      "publicKey": "string"
    }
  },
  "deviceRegistry": {
    "service": "DEVICE_REGISTRY_ONBOARDING_WITH_CSR_SERVICE",
    "uri": "string"
  },
  "systemRegistry": {
    "service": "SYSTEM_REGISTRY_ONBOARDING_WITH_CSR_SERVICE",
    "uri": "string"
  },
  "serviceRegistry": {
    "service": "SERVICE_REGISTRY_REGISTER_SERVICE",
        "uri": "string"
  },
  "orchestrationService": {
    "service": "ORCHESTRATION_SERVICE",
    "uri": "string"
  }
}
```

| Field | Description |
| ----- | ----------- |
| `rootCertificate` | The Arrowhead master certificate |
| `intermediateCertificate` | The Arrowhead local cloud certificate |
| `onboardingCertificate` | The onboarding certificate for the next step |
| `certificateFormat` | The certificate format (usually X.509) |
| `certificateType` | The certificate type. Always AH_ONBOARDING for this operation |
| `keyAlgorithm` | The key algorithm of the provided keys |
| `keyFormat` | The key format of the provided keys |
| `privateKey` | Base64 encoded private key. Always empty for this operation |
| `publicKey` | Base64 encoded public key |
| `service` | The service which is reachable under `uri` |
| `uri` | The uri under which the depicted `service` is reachable |

