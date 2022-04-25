# AH-Ditto Application HTTP/TLS/JSON - Interface Design Description


## Abstract
This document describes the HTTP/{TLS}/JSON variant of a service that can be used to access digital twins in the Eclipse Ditto framework by the AH-Ditto adapter system. It provides a way to meet the security requirements of both Eclipse Ditto and Eclipse Arrowhead to make digital twin's services available in Eclipse Arrowhead framework.

## Overview
This document describes the HTTP/{TLS}/JSON variant of the Ah-Ditto Application services, which allows application systems in Eclipse Arrowhead Framework to consume digital twin services from Eclipse Ditto framework.

## Service Interfaces
This section describes the interfaces that must be exposed by AH-Ditto Application services. In particular, the below subsection first names the HTTP method and path used to call the interface.

<!-- ### Things
AH-Ditto provides the following services to manage things or digital twins in the eclipse ditto framework. -->

### GET {baseURI}/things/{thingId}/features/{featureId}/properties/ {propertyPath} 
 - __Interface:	GetSpecificFeaturePropertyOfSpecificThing
 - __Output: [Thing](#thing)__


Example of valid invocation:
```json
GET /ditto/things/my.test:pi/thermometer/properties/temperature 
HTTP/1.1
Accept: application/json
```

Example of valid response:
```json
HTTP/1.1 200 OK
Content-Type: application/json
{
    "thermometer": {
        "properties": {
            "temperature": 0
        }
    }
}
```
