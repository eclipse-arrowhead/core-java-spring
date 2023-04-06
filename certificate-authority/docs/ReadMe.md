# Certificate Authority

<a name="ca_sdd" />

## System Design Description Overview

The main purpose of the Certificate Authority supporting core system is issuing signed certificates to be used in the local cloud.
The issued certificates may be revoked from the Management Interface. Systems may check whether a certificate has been revoked, and refuse their acceptance.

<a name="ca_provided_services" />

## Provided services

The Certificate Authority provides the following services:
* [Echo](#ca_endpoints_get_echo)
* [Certificate validity checking](#ca_endpoints_check_certificate)
* [Certificate signing](#ca_endpoints_sign)

<a name="ca_usecases" />

## Use cases

1. Issue signed certificates to new consumers coming via the Onboarding Controller.
2. Handle certificate revocation.
3. Act as a trusted public key store.

<a name="ca_endpoints" />

## Endpoints

<a name="ca_endpoints_client" />

### Client endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Echo](#ca_endpoints_get_echo)                                | /echo             | GET  | - | OK |
| [Check certificate validity](#ca_endpoints_check_certificate) | /checkCertificate | POST | [CertificateCheckRequest](#ca_certificate_check_request) | [CertificateCheckResponse](#ca_certificate_check_response) |

<a name="ca_endpoints_private" />

### Private endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Sign CSR with the Cloud Certificate](#ca_endpoints_sign) | /sign | POST | [CertificateSigningRequest](#ca_certificate_signing_request) | [CertificateSigningResponse](#ca_certificate_signing_response) |
| [Check trusted key](#ca_endpoints_check_trusted_key) | /checkTrustedKey | POST | [TrustedKeyCheckRequest](#ca_trusted_key_check_request) | [TrustedKeyCheckResponse](#ca_trusted_key_check_response) |

<a name="ca_endpoints_mgmt" />

### Management endpoint description<br />

| Function | URL subpath | Method | Input | Output |
| -------- | ----------- | ------ | ----- | ------ |
| [Get issued certificates](#ca_endpoints_get_certificates) | /mgmt/certificates      | GET    | - | [IssuedCertificatesResponse](#ca_issued_certificates_response) |
| [Revoke certificate](#ca_endpoints_revoke_certificate)    | /mgmt/certificates/{id} | DELETE | Certificate record id | OK |
| [Get trusted keys](#ca_endpoints_get_trusted_keys)        | /mgmt/keys              | GET    | - | [TrustedKeysResponse](#ca_trusted_keys_response) |
| [Add trusted key](#ca_endpoints_add_trusted_key)          | /mgmt/keys              | PUT    | [AddTrustedKeyRequest](#ca_add_trusted_key_request) | [AddTrustedKeyResponse](#ca_add_trusted_key_response) |
| [Delete trusted key](#ca_endpoints_delete_trusted_key)    | /mgmt/keys/{id}         | DELETE | Key record id | 204 No Content |

<a name="ca_endpoints_get_echo" />

### Echo 
```
GET /certificate-authority/echo
```

Returns a "Got it!" message with the purpose of testing the core service availability.

<a name="ca_endpoints_check_certificate" />

### Check certificate validity

```
POST /certificate-authority/checkCertificate
```

Returns whether the given certificate is valid or has been revoked. The client SHALL not trust a revoked certificate.

<a name="ca_certificate_check_request" />

__CertificateCheckRequest__ is the input:

```json
{
  "version": "integer",
  "certificate": "string"
}
```

| Parameter     | Description                   | Necessity | Format/Limitations                                     |
| ------------- | ----------------------------- | --------- | ------------------------------------------------------ |
| `version`     | Version of the check protocol | mandatory | integer, currently must be `1`                         |
| `certificate` | The certificate to check      | mandatory | Base64 encoded X.509 certificate (PEM without headers) |

<a name="ca_certificate_check_response" />

Returns a __CertificateCheckResponse__:

```json
{
  "version": "integer",
  "producedAt": "string",
  "endOfValidity": "string",
  "commonName": "string",
  "serialNumber": "string",
  "status": "string"
}
```

| Field              | Description                                                    |
| ------------------ | -------------------------------------------------------------- |
| `version`          | Version of the check protocol                                  |
| `producedAt`       | The time at wich the response has been created                 |
| `commonName`       | Common name of the certificate                                 |
| `serialNumber`     | Serial number of the certificate                               |
| `endOfValidity`    | End of validity due to expiration or revocation                |
| `status`           | One of the following: `good`, `revoked`, `expired`, `unknown`  |

<a name="ca_endpoints_sign" />

### Sign CSR with the Cloud Certificate

```
POST /certificate-authority/sign
```

Returns the whole certificate chain beginning with the newly generated leaf certificate and ending with the root certificate.

Each certificate's issuer is the same as the subject of the following one. The issuer of the root certificate is the same as the subject.

The request may contain `validAfter` and `validBefore` fields to limit the validity range of the Certificate to be generated more than the default values set in Certificate Authority.

Metadata of each generated certificate is stored in the database to allow handling revocation and validity checking.

A request for signing a certificate with restricted common name is only accepted if it is requested by `sysop`. Common names starting with the name of a Core System or `sysop` are restricted.

<a name="ca_certificate_signing_request" />

__CertificateSigningRequest__  is the input:

```json
{
  "encodedCSR": "string",
  "validAfter": "string",
  "validBefore": "string"
}
```

| Parameter     | Description                          | Necessity | Format/Limitations        |
| ------------- | ------------------------------------ | --------- | ------------------------- |
| `encodedCSR`  | PKCS #10 Certificate Signing Request | mandatory | Base64 encoded CSR        |
| `validAfter`  | Beginning of Certificate validity    | optional  | ISO 8601 date/time string |
| `validBefore` | End of Certificate validity          | optional  | ISO 8601 date/time string |

<a name="ca_certificate_signing_response" />

Returns __CertificateSigningResponse__:

```json
{
  "id": "integer",
  "certificateChain": [
    "<generated client certificate>",
    "<cloud certificate>",
    "<root certificate>"
  ]
}
```

| Field | Description |
| ----- | ----------- |
| `id`               | ID of the newly generated Certificate in the database          |
| `certificateChain` | The whole certificate chain in an array of Base64 encoded DER strings (PEM without headers) |


<a name="ca_endpoints_check_trusted_key" />

### Check trusted key

```
POST /certificate-authority/checkTrustedKey
```

Returns whether the given public key has been registered as trusted or not.

<a name="ca_trusted_key_check_request" />

__TrustedKeyCheckRequest__ is the input:

```json
{
  "publicKey": "string"
}
```

| Field          | Description             | Necessity | Format/Limitations                                   |
| -------------- | ----------------------- | --------- | ---------------------------------------------------- |
| `publicKey`    | The public key to check | mandatory | Base64 encoded DER public key (PEM without headers)  |

<a name="ca_trusted_key_check_response" />

Returns a __TrustedKeyCheckResponse__:

```json
{
  "id": "integer",
  "createdAt": "string",
  "description": "string"
}
```

| Field         | Description                                    | Format/Limitations        |
| ------------- | ---------------------------------------------- | ------------------------- |
| `id`          | Record Id                                      | integer                   |
| `createdAt`   | The time at which the key has been added       | ISO 8601 date/time string |
| `description` | Description of the key, e.g. device identifier | string                    |


<a name="ca_endpoints_get_certificates" />

### Get issued certificates

```
GET /certificate-authority/mgmt/certificates
```

Returns data about every certificate issued by the Certificate Authority.
If `page` and `item_per_page` are not defined, returns all records.

Query params:

| Parameter       | Description                      | Necessity |
| --------------- | -------------------------------- | --------- |
| `page`          | zero based page index            | optional  |
| `item_per_page` | maximum number of items returned | optional  |
| `sort_field`    | sorts by the given column        | optional  |
| `direction`     | direction of sorting             | optional  |

> **Note:** Default value for `sort_field` is `id`. All possible values are: 
> * `id`
> * `createdAt`
> * `createdBy`
> * `validfrom`
> * `validUntil`
> * `commonName`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC` 

<a name="ca_issued_certificates_response" />

Returns a __IssuedCertificatesResponse__:

```json
{
  "count": "integer",
  "issuedCertificates": [
    "id": "integer",
    "createdAt": "string",
    "createdBy": "string",
    "validfrom": "string",
    "validUntil": "string",
    "revokedAt": "string",
    "commonName": "string",
    "serialNumber": "string",
    "status": "string"
  ]
}
```

| Field              | Description                                                |
| ------------------ | ---------------------------------------------------------- |
| `count`            | Number of issued certificate records                       |
| `id`               | ID of the certificate record                               |
| `createdAt`        | The time at wich the record has been created               |
| `createdBy`        | Name of the system which requested the certificate         |
| `validFrom`        | Beginning of validity of the certificate                   |
| `validUntil`       | End of validity of the certificate                         |
| `revokedAt`        | The time at which the certificate has been revoked or null |
| `commonName`       | Common name of the certificate                             |
| `serialNumber`     | Serial number of the certificate                           |
| `status`           | One of the following: `good`, `revoked`, `expired`         |


<a name="ca_endpoints_revoke_certificate" />

### Revoke certificate

```
DELETE /certificate-authority/mgmt/certificate/{id}
```

Revoke a previously issued certificate.
Revocation means that subsequent requests to the [Check certificate validity](#ca_endpoints_check_certificate) service return with `revoked` status.
The client SHALL not trust the revoked certificate.

Query params:

| Parameter       | Description             | Necessity |
| --------------- | ----------------------- | --------- |
| `id`            | Certificate record ID   | mandatory |

Returns HTTP `200 OK` on success.


<a name="ca_endpoints_get_trusted_keys" />

### Get trusted keys

```
GET /certificate-authority/mgmt/keys
```

Returns the list of public keys that are added to be trusted.
The keys may be used e.g. by the Onboarding Controller to identify a device.
If `page` and `item_per_page` are not defined, returns all records.

Query params:

| Parameter       | Description                      | Necessity |
| --------------- | -------------------------------- | --------- |
| `page`          | zero based page index            | optional  |
| `item_per_page` | maximum number of items returned | optional  |
| `sort_field`    | sorts by the given column        | optional  |
| `direction`     | direction of sorting             | optional  |

> **Note:** Default value for `sort_field` is `id`. All possible values are: 
> * `id`
> * `createdAt`

> **Note:** Default value for `direction` is `ASC`. All possible values are:
> * `ASC`
> * `DESC` 

<a name="ca_trusted_keys_response" />

Returns a __TrustedKeysResponse__:

```json
{
  "count": "integer",
  "trustedKeys": [
    "id": "integer",
    "createdAt": "string",
    "description": "string"
  ]
}
```

| Field         | Description                                    |
| ------------- | ---------------------------------------------- |
| `count`       | The total number of the trusted keys           |
| `id`          | Record ID                                      |
| `createdAt`   | The time at which the key has been added       |
| `description` | Description of the key; e.g. device identifier |


<a name="ca_endpoints_add_trusted_key" />

### Add trusted key

```
PUT /certificate-authority/mgmt/keys
```

Add a public key to the list of trusted keys.
This key may be used by e.g. the Onboarding Controller to onboard a device with known public key.

<a name="ca_add_trusted_key_request" />

__AddTrustedKeyRequest__  is the input:

```json
{
  "publicKey": "string",
  "description": "string",
  "validAfter": "string",
  "validBefore": "string"
}
```

| Field          | Description                                    | Necessity | Format/Limitations                                  |
| -------------- | ---------------------------------------------- | --------- | --------------------------------------------------- |
| `publicKey`    | The public key to add as trusted               | mandatory | Base64 encoded DER public key (PEM without headers) |
| `description`  | Description of the key; e.g. device identifier | mandatory | string                                              |
| `validAfter`   | Beginning of validity                          | mandatory | ISO 8601 date/time string                           |
| `validBefore`  | End of validity                                | mandatory | ISO 8601 date/time string                           |


<a name="ca_add_trusted_key_response" />

Returns an __AddTrustedKeyResponse__ with HTTP `201 Created` on success:

```json
{
  "id": "integer",
  "validAfter": "string",
  "validBefore": "string"
}
```

| Field            | Description                  |
| ---------------- | ---------------------------- |
| `id`             | Record ID                    |
| `validAfter`     | Beginning of validity        |
| `validBefore`    | End of validity              |


<a name="ca_endpoints_delete_trusted_key" />

### Delete trusted key

```
DELETE /certificate-authority/mgmt/keys/{id}
```

Delete a public key from the list of trusted keys.

Query params:

| Parameter       | Description          | Necessity |
| --------------- | -------------------- | --------- |
| `id`            | Key record ID        | mandatory |

Returns HTTP `204 No Content` on success.