# Device Hub (Eclipse Hono <-> Arrowhead integration)

## Introduction
[Eclipse Hono](https://www.eclipse.org/hono/) connects a huge number of constrained devices that may use a variety of different protocols, such as HTTP, MQTT, CoAP and AMQP.
As an Arrowhead local cloud might involve a heterogeneous system architecture, Eclipse Hono can help translating between these endpoints and also provides a central and reliable messaging layer.
It functions as an intermediary layer between devices and applications and guarantees successful transmission by employing an AMQP 1.0 message broker.
Therefore, AMQP 1.0 functions as a universal common protocol to which each incoming message is translated before further transmission takes place.

```
        +-------------+
        | Application |
        +-----++------+
              || -------------------------- Northbound API (AMQP Broker)
+-------------++------------+
|                           |
|       Eclipse Hono        |
|                           |
+---+---+---+---+---+---+---+
    |   |   |   |   |   | ----------------- Soutbound API
    +---+   +-+-+   +-+-+                   (Protocol Adapters,
              |       |                     e.g.: HTTP, MQTT, CoAP, AMQP)
      +-------+       |
      |               |
+-----+----+     +----+-----+
| Device 1 | ... | Device N |
+----------+     +----------+
```

Eclipse Hono's architecture consists of a microservice approach and exposes a northbound and a southbound API.
Therefore, Eclipse Hono assumes a classification of all systems in one of two groups, depending on whether the system uses the northbound or the southbound API.
First, there are the so-called business applications, connecting to the northbound API, second, there are the devices that are assumed to be in the field and which support heterogeneous protocols.
These devices may also be limited in their hardware capabilities being one of the reasons for the protocol heterogeneity.
As you can see, Eclipse Hono assumes a rather hierarchic approach where the distributed devices mostly provide data to central applications which then process these data and produce commands for the devices.
At the same time, Arrowhead advocates for a more distributed and flat approach where every system is an equal participant. 
However, the two views are not mutually exclusive because an Arrowhead system could act as a device and as a northbound application at the same time, when associated with different use cases.
In most cases, a local cloud consists of a heterogeneous set of systems, where smaller, less powerful devices specialize for one task (sensing data).

The following documentation explains how to integrate Eclipse Hono with the orchestration and authentication/authorization mechanisms of the Eclipse Arrowhead Framework.


## Eclipse Hono Concepts
To understand how we can integrate Eclipse Hono with Arrowhead, it is important to understand a few concepts of Eclipse Hono.

### Message types
Messages circulating through Eclipse Hono are categorized by their purpose, resulting in three different types.
- **telemetry messages** conceptually come from devices (connected to the southbound API) and contain sensor data, which applications on the northbound API then process. These messages are sent at most once.
- **event messages**, in contrast, indicate some form of information, where successful message transfer is crucial and is hence acknowledged to the device (QoS level is at least once).
- **command & control messages** come from business applications and are used to trigger actions on devices. They can either follow a one-way or request-response pattern. The QoS level in both cases is at least once.

Telemetry messages and Event messages are always sent through the southbound API and can be consumed at the northbound API. For Command & Control messages the flow is vice-versa, so they are given to Eclipse Hono through the northbound API and are then delivered to a client which is listening at the southbound API for incoming Command & Control messages.

### Microservice architecture
As mentioned above, Eclipse Hono consists of a couple of microservices interacting with each other.
Each of these components could be substituted by a custom implementation if necessary for a aspecific use case.
The Eclipse Hono documentation has a good overview of the respective [components](https://www.eclipse.org/hono/docs/architecture/component-view/).

#### Device registry
As the name suggests, the device registry handles the registration and management of all devices that whish to connect to the southbound API.
As mentioned above, for Eclipse Hono the concept of a device is an entity which can send *telemetry* or *event* message and receive *command & control* messages through the southbound API.
In order for Eclipse Hono to accept messages at the southbound API, the respective device needs to be registered with the device registry.
The device registry offers a specific [device registry management API](https://www.eclipse.org/hono/docs/api/management/) for this registration procedure.
With the same API it is also possible to assign credentials to the devices.
The following types of credentials are available: `PasswordCredentials`, `PSKCredentials`, `X509CertificateCredentials`.
Since, certificates are also the preferred way of authentication in the Arrowhead Framework, e.g. for the authentication system, the integration outlined below relies on certificates as well.

#### Auth Server
The auth server is responsible for validating the identity of all devices (southbound) and business applications (northbound).

#### Protocol adapters
The protocol adapters face the devices, forming the southbound API of Eclipse Hono.
To cope with the heterogenity of protocols used by the different devices, there are various preimplemented protocol adapters, such as for HTTP, MQTT, CoAP or AMQP 1.0.
Therefore, the main task for the protocol adapters is to translate between the device protocol and the protocol of the messaging network (AMQP 1.0), but also to communicate with the device registry and auth server to guarantee compliance with the authentication & authorization scheme.
The implementation of the protocol adapters further aims to maintain as less state per connection as possible.
Because of that it becomes easy to scale them vertically to serve an increasing number of devices.
Moreover, it is possible to integrate new adapters that support more (even proprietary) protocols.

#### Messaging network
The messaging network forms a pivotal point in Eclipse Hono's architecture and guarantees a reliable message exchange.
This messaging network is connected to the respective protocol adatpers and also forms the northbound API, as it is directly exposed to the business applications.
To communicate with the messaging network, the business applications use AMQP 1.0 and the procotol adapters translate between their respective procotol and AMQP 1.0 (e.g.: HTTP <-> AMQP 1.0). In newer releases Eclipse Hono offers the option to use Apache Kafka instead of the AMQP 1.0 messaging network. However, this guide focuses on the integration using an AMQP 1.0 messaging network while many of the described concepts apply for a setup with Apache Kafka as well. 

For further details on Eclipse Hono's functioning and architecture, please see [the Hono documentation](https://www.eclipse.org/hono/docs).

## Eclipse Hono deployment

### Pre-Conditions

#### tl;dr:
- Kubernetes cluster
- Kubernetes CLI (kubectl) installed
- Helm CLI installed
- certificates to comply with the Arrowheaed authencation scheme

For deploying Eclipse Hono, an available [Kubernetes](https://kubernetes.io/) cluster is necessary.
The [CNCF Landscape](https://landscape.cncf.io/category=platform&format=card-mode&grouping=category) shows a variety of different Kubernetes providers/distributions.
Kubernetes provides a standardized way of abstracting the underlying platform and offers benefits, such as handling automated scaling and zero-downtime.
Take a look at section `Eclipse Hono at the edge` to find out more about the possibilities of setting up a local cluster yourself, instead of relying on a cloud provider.
You will also need the [Kubernetes CLI](https://kubernetes.io/docs/tasks/tools/install-kubectl/) installed on your machine to communicate with your cluster.

Moreover, we are using Helm for the deployment operation.
Helm allows to create and manage more complex Kubernetes deployments, for more information see [here](https://helm.sh/).
Please install the [Helm CLI](https://helm.sh/docs/intro/install/) as well which is required to deploy Eclipse Hono. 

To integrate Eclipse Hono with the Arrowhead authentication scheme, we need to [generate new certificates/keys](https://github.com/eclipse-arrowhead/core-java-spring#certificates).
Within this document, we assume that you already have a cloud certificate present, otherwise please generate a new one.
We need the following certificates (if you need to rename them, you have to adjust the paths in the `values.yaml` and the files within the templates folder accordingly):
- `local-cloud.pem`: the cloud certificate of the Arrowhead local cloud (used as truststore)
- `adapter-cert.pem`/`adapter-key.pem`: the certificate/key used by the adapters to authenticate at the dispatch router
- `router-cert.pem`/`router-key.pem`: the certificate/key used by the dispatch router to authenticate to the adapters and the business applications at the northbound side
- `<adapter>-keystore.p12`: the respective keystore used by the adapters to authenticate to all devices, e.g. `http-keystore.p12`

Note, that if you change the keystore passwords - `keyStorePassword` - (which is highly recommended), you have to change the `values.yaml` accordingly.
If you want to enable more adapters in Eclipse Hono, such as the CoAP adapter, please configure it in the `values.yaml` similar to the HTTP and MQTT adapters.

For more details consult the documentation of the Helm chart of Eclipse Hono.

### Deployment
As Eclipse Hono consists of a number of interconnected microservices which are all capable of scaling independently, it makes sense to orchestrate them as containers.
Therefore, the preferred deployment method is to use Kubernetes and the Eclipse Hono [Helm chart](https://github.com/eclipse/packages/tree/master/charts/hono) from the Eclipse IoT Packages project.
The maintance of the Helm Chart happens as part of the Eclipse IoT Packages project which aims to provide packages of joint Eclipse projects in the form of Helm Charts.

In order to make Eclipse Hono work together with the other Arrowhead core systems, we need to make a few modifications to the initial Helm chart.
You may decide to build your own custom Helm chart and then define the Eclipse IoT Packages Helm chart as a dependency there. Alternatively, you can checkout the Eclipse Hono Chart with GIT and apply your changes directly to your local copy.
This directory already comes with resources that can be used in a modified or custom Helm chart. For instance, the given `values.yaml` predefines a few values we need to overwrite in the `values.yaml` of Eclipse Hono file, e.g. enable or disable protocol adapters according to your use case. 

This guide does not focus on a specific version of the Eclipse Hono Helm Chart since this is subject to possible changes even within the same version of Eclipse Hono. Instead we rather focus on describing the general concepts and configurations. Because of that, for a more recent version of the Eclipse Hono Helm Chart there might be slight variations in the naming of the parameters.

In addition, you need to mount your specific Arrowhead certificates and configurations into the respective containers. The Eclipse Hono Helm Chart utilizes so-called `extraSecretMounts` to do this as you can see in the provided `values.yaml`. We thus also provide a `templates` folder which contains the templates for those secrets which in turn reference the content of the folders `certs` and `router`. 

To let Eclipse Hono integrate with the Arrowhead authentication scheme, we need to inject the Arrowhead local cloud certificate into the system.
Place your local cloud certificate (PKCS8) under `./hono-deployment/static/certs` and name it `local-cloud.pem`.
This certificate will be used as truststore by the protocol adapters to validate the identity of the dispatch router.

Also place the adapter's certificate and key under `static/certs` and name it `adapter-cert.pem` and `adapter-key.pem` respectively.
These files will authenticate the adapters at the dispatch router.

Vice versa, we define the certificates the router uses for authentication with the adapters.
Therefore, place two files `router-cert.pem` and `router-key.pem` in the `static/certs` directory.

Next, we need to define the certificates the adapters use for TLS at their southbound side.
You need to place a keystore file (PKCS12) for every adapter you want to use in the `static/certs` directory and name it `<adapter>-keytore.p12`. So e.g. `mqtt-keystore.p12`.

If you add the certificates for another adapter, you need to add a new entry to the `adapter-keystore.yaml` Secret file.
By default, the http and the mqtt adapters are enabled in the `values.yaml` and there exist entries in the `adapter-keystore.yaml`.
So make sure you either generate certificates for them or disable them and delete the entries.
Also make sure to extend the secret and the `values.yaml` for every new adapter you integrate.

Now, we are ready for the actual deployment for which you execute the following steps in the same directory where your custom Helm Chart or local copy of the Eclipse Hono Helm chart is placed:

First, load the necessary dependencies for the helm chart: `helm dep up hono-deployment` (You may change the `hono-deployment` to the name of your used Helm chart.)

Then, create a new namespace in your cluster: `kubectl create namespace hono`.
Note, that you can replace `hono` with any other name for your namespace if it suits better to your use case.
You then need to adapt the namespace in the next commands as well.

In order to install the chart on your Kubernetes cluster, run `helm install hono hono-deployment -n hono`

Now, we are ready to configure Eclipse Hono to use it with your local cloud and the Arrowhead core systems.

### Eclipse Hono Setup and wiring up with Arrowhead
In order to make Eclipse Hono accessible for other systems within the Arrowhead local cloud, we need to register its protocol adapter endpoints at the service registry.
This is necessary to enable new devices to find the protocol adapter and then connect to them.

Please replace all `<placeholders>` in the snippets (see the table below for more information).
```
curl -H "Content-Type: application/json" \
-X POST \
--cacert <CA_cert> \
--cert <client_cert> \
--key <private_key> \
https://<service_registry_ip>:<service_registry_port>/serviceregistry/register \
--data-binary @- << EOF
{
    "interfaces": [
        "<interface>"
    ],
    "providerSystem": {
        "address": "<address>",
        "authenticationInfo": "<auth_info>",
        "port": <port>,
        "systemName": "<system_name>"
    },
    "secure": "CERTIFICATE",
    "serviceDefinition": "<definition>",
    "serviceUri": "<uri>",
    "version": <version>
}
EOF
```
We choose the certificate-based authentication scheme, as Eclipse Hono is capable of handling client certificate based authentication.

| Attribute | Description   | example HTTP | example MQTT |
| --------- | ------------- | ------------ | -------------|
| `CA_cert`             | the truststore, containing the cloud certficate | `ca_cert.pem` | `ca_cert.pem` |
| `client_cert`         | the certificate of the adapter | `client_cert.pem` | `client_cert.pem` |
| `private_key`         | the private key corresponding with the certificate | `private_key.pem` | `private_key.pem` |
| `interface`           | an array of all Arrowhead service interfaces (name pattern: (protocol)-(SECURE/INSECURE)-(payload-format)) | `["HTTP-SECURE-JSON"]` | `["MQTT-SECURE-JSON"]` |
| `address`             | the base address of the Eclipse Hono procotol adapter | `localhost` | `localhost` |
| `auth_info`           | usually the public key of the service provider for JWT generation; however, as we use the certificate based authentication scheme, this can be omited | `""` | `""` |
| `port`                | the port of the Eclipse Hono protocol adapter | `8443` | `8883` |
| `system_name`         | the name of the protocol adapter (must match the first part of the CN of the used protocol) | `hono_amqp` | `hono_mqtt` |
| `definition`          | the service definition to specify more details about how the service is used | `""` | `""` |
| `uri`                 | the specific service uri; the following values are allowed: telemetry, event, command | `/telemetry`, `/event`, `/command` | topic `telemetry` or `event` for publish, topic `command///req/#` for subscribe |
| `version`             | version of the service API | `1.0.0` | `1.0.0` |

To retrieve the address of the protocol adapter you can run `kubectl get service -n hono` and extract the respective public IP.
Note, that this command will only return a public IP if your Kubernetes provider supports the Kubernetes service type 'LoadBalancer' (read more about the possibilities of running a Kubernetes cluster at the edge in the section **Eclipse Hono at the edge**).
For further information of how to connect to services running within the cluster consult the documentation of the specific provider.

To let Eclipse Hono know which CAs to trust, we need to store the root certificate (in this case the Arrowhead cloud certificate) for our tenant in the Eclipse Hono device registry.
This can be done during the tenant creation, using Eclipse Hono's [device registry management API](https://www.eclipse.org/hono/docs/api/management/).
```
curl -H "Content-Type: application/json" \
-X POST \
--cacert <CA_cert> \
--cert <client_cert> \
--key <private_key> \
http://<hono_device_registry_ip>:<port>/v1/tenants/<tenant_name> \
--data-binary @- << EOF
{
    "adapters": [
        {
            "type": "<protocol-adapter-1>",
            "enabled": true
        },
        {
            "type": "<protocol-adapter-2>",
            "enabled": true
        }
    ],
    "trusted-ca": [
        {
            "cert": "<arrowhead-cloud-cert>",
            "auto-provisioning-enabled": false
        }
    ]
}
EOF
```

Make sure to enable the right protocol adapters for your tenant by inserting the correct type of your adatper, e.g. `hono-http` or `hono-mqtt` (in general `hono-<protocol>`), and setting `enabled` to `true`.
Also add the arrowhead cloud certificate (removing the line breaks) to the `trusted-ca` property.

For the device to be able to authenticate at the protocol adapter, we first have to register the device in Eclipse Hono's device registry and create new credentials.
Therefore, two REST calls are necessary.
Call to create a new device:
```
curl -H "Content-Type: application/json" \
-X POST \
--cacert <CA_cert> \
--cert <client_cert> \
--key <private_key> \
http://<hono_device_registry_ip>:<port>/v1/devices/<tenant_name>/<device_id>
```

Note down the device ID, as we will need it for the further steps.
We could also let Eclipse Hono generate a device ID for us by removing the `<device_id>` part from the URL.

The second call is necessary to generate credentials for the newly created device:
```
curl -H "Content-Type: application/json" \
-X POST \
--cacert <CA_cert> \
--cert <client_cert> \
--key <private_key> \
http://<hono_device_registry_ip>:<port>/v1/credentials/<tenant_name>/<device_id> \
--data-binary @- << EOF
[
    {
        "auth-id": "<subject_dn_from_cert>",
        "type": "x509-cert",
        "secrets": [
            {
                "enabled": true
            }
        ]
    }
]
EOF
```

The `auth-id` property must equal the subject DN from your certificate, e.g. `CN=device.example.corp.arrowhead.eu,OU=AHT,O=arrowhead.eu,L=Berlin,ST=Berlin,C=DE`.

A new device, connecting (and sending data) to any protocol adapter, now needs to present its Arrowhead system certificate and will be granted access if it is valid.
Of course, the corresponding tenant must be configured to use the Arrowhead cloud certificate as a CA (see the request for creating the tenant above).
Subsequently, the protocol adapter forwards the received message to a listening application.

Eclipse Hono also allows the automatic provisioning of Eclipse Hono device instances once they first send a message to the protocol adapter, resulting in the generation of a random device ID.
However, as we need to register the service/device at the Arrowhead service registry, we need to know the specific newly generated device ID.

In order to enable business applications to connect to Eclipse Hono devices, we also need to make sure that the northbound API is registered at the service registry.
Generally, there are two possible ways to achieve that goal.
Either by registering the northbound API in general and relying on the intelligence of the business applications to find a respective device and its deviceID on their own, or by registering each device separately (For more on that, see the section **final remarks**).
Here, we register a device endpoint directly using the Eclipse Hono northbound base address, but also specifying the individual device ID.

```
curl -H "Content-Type: application/json" \
-X POST \
--cacert <CA_cert> \
--cert <client_cert> \
--key <private_key> \
https://<service_registry_ip>:<service_registry_port>/serviceregistry/register \
--data-binary @- << EOF
{
    "interfaces": [
        "<interface>"
    ],
    "providerSystem": {
        "address": "<address>",
        "authenticationInfo": "<auth_info>",
        "port": <port>,
        "systemName": "<system_name>"
    },
    "secure": "CERTIFICATE",
    "serviceDefinition": "<definition>",
    "serviceUri": "<uri>",
    "version": <version>
}
EOF
```

| Attribute | Description   | example (entire northbound API)   | example (each device separately) |
| --------- | ------------- | --------------------------------- | -------------------------------- |
| `CA_cert`                 | the truststore, containing the cloud certficate | `ca_cert.pem` | `ca_cert.pem` |
| `client_cert`             | the certificate of the adapter | `client_cert.pem` | `client_cert.pem` |
| `private_key`             | the private key corresponding with the certificate | `private_key.pem` | `private_key.pem` |
| `interface`               | an array of all Arrowhead service interfaces (name pattern: (protocol)-(SECURE/INSECURE)-(payload-format)) | `["AMQP-SECURE-JSON"]` | `["AMQP-SECURE-JSON"]` |
| `address`                 | the base address of the Eclipse Hono northbound API | `localhost` | `localhost` |
| `auth_info`      | usually the public key of the service provider for JWT generation; however, as we use the certificate based authentication scheme, this can be omited | `""` | `""` |
| `port`                    | the port of the Eclipse Hono northbound API | `15671` | `15671` |
| `system_name`             | the name of the northbound API/device (must match the first part of the CN of the used certificate) | `hono_northbound` | `hono_device_2` |
| `definition`              | the service definition to specify more details about how the service is used; in the second case, we need to include the device ID | `""` | `"{'device_id': '<deviceID>'}"` |
| `uri`                     | the specific service uri; the following values are allowed: telemetry, event, command | `telemetry/<tenant_id>` or `event/<tenant_id>` or `command/<tenant_id>`  | `telemetry/<tenant_id>` or `event/<tenant_id>` or `command/<tenant_id>` |
| `version`                 | version of the service API | `1.0.0` | `1.0.0` |

In case each device is registered separately, the call could be executed by the device itself.
Otherwise, a helper application can run the command once during startup.

## Eclipse Hono at the edge
As Eclipse Hono orginially was designed for cloud environments as deployment target, among other things due to scaling requirements, you might wonder whether it is suitable for industrial use cases at the edge, as well.
Often, industrial processes employ software modules that are running on-premise and not in the cloud, due to security restrictions and requirements.
However, it is also possible to deploy Eclipse Hono at the edge, using a local Kubernetes distribution, such as [k3s](https://k3s.io/) or [microk8s](https://microk8s.io/).
For more info see the following [tutorial](https://www.eclipse.org/packages/prereqs/).
This would enable you to exploit the benefits of Kubernetes, but still keeping on to your data.
However, be aware of the fact that you will need to take care of load balancing yourself.
Therefore you might want to deploy an API gateway that proxies your ingress to the right endpoint, i.e. the protocol adapter.

## Final remarks
The described workflow fully integrates with Arrowhead orchestration, authentication, and partly authorization schemes.

### Entries in Service Registry
Generally, it is possible to just register the Eclipse Hono northbound base address at the service registry and rely on separate mechanisms of the business applications on the northbound to be aware of which devices/services are reachable.
Conversely, it its also possible to add an entry for each device at the service registry, which would then consist of Eclipse Hono's northbound base address, their tenant ID and the device ID.
An application trying to interact with one of the devices then requests orchestration and receives the stored address, as depicted in the sequence diagram.
Now the application can connect to Eclipse Hono's northbound API, which itself uses certificates for authenticating the application.

### Fine Grained Access Control
With the configuration from this repository there is no more fine-grained authorization scheme possible and Eclipse Hono does not keep track or differentiates between the different systems requesting access at the northbound side.
The authentication and authorization mechanism only checks whether the certificate is valid, meaning it was issued by the Arrowhead Cloud CA.
As a consequence, every application with a valid certificate for the local cloud can authorize itself to consume data from all devices connected to Eclipse Hono.
This, however, is a more theoretic problem, as the orchestrator would not return an orchestration request in case there is no orchestration rule or valid authorization present for the application that intends to consume the northbound API.
Of course, if a system owns a valid Arrowhead local cloud certificate this measurements does not prevent a apotential attacker to find other service addresses through network scanning and execute malicious requests.
Also, this problem accounts only for telemetry and event messages, sent by devices to Eclipse Hono.
Command & Control messages (messages send by an application to a device) could potentially require a valid Arrowhead JWT as part of the message payload, which could be issued by Arrowhead's authorization system.
The device itself would then be responsible for validating the token, which means that every non-authorized message has to be evaluated by the device and processed by the whole messaging infrastructure including potential radio links.
This however, requires the device to be able to perform these kinds of fairly complex tasks.

In case you heavily depend on an architecture, where direct device-to-device communication is categorically necessary, or in use cases where a large number of devices on the northbound side necessarily requires sophisticated authorization integrated with Arrowhead, Eclipse Hono alone might not be the best choice.
However, Eclipse Hono solves a lot of your challenges ranging from many dimensions, such as protocol heterogeneity, scaling and authentication.

As the authorization topic remains, we recommend to use Eclipse Hono in combination with Eclipse Ditto, which seamlessly integrates with Eclipse Hono's northbound API and enables more fine-grained authorization policies.
```
+--------+ +--------------+ +------------------------------------------+ +-------------+
|        | |              | |                   Hono                   | |             |
| Device | | Orchestrator | +------------+-+------------+-+------------+ | Application |
|        | |              | |  Adapter   | |  Registry  | |    AMQP    | |             |
+----+---+ +-------+------+ +-----+------+ +------+-----+ +-----+------+ +------+------+
     |             |              |               |             |               |
     |             +<-----------------------------------------------------------+
     |             |              |               |             | orchestration |
     |             +----------------------------------------------------------->+
     |             |              |               |             |               |
     |             |              |               |             +<--------------+
     |             |              |               |             |   subscribe   |
     +------------>+              |               |             |               |
     |orchestration|              |               |             |               |
     +<------------+              |               |             |               |
     |             |              |               |             |               |
     +--------------------------->+  device       |             |               |
     |             | {"temp": 5}  |  authorized?  |             |               |
     |             | + cert       +-------------->+             |               |
     |             |              |               |             |               |
     |             |              +<--------------+             |               |
     |             |              |               | {"temp": 5} |               |
     |             |              +---------------------------->+               |
     |             |              |               |             |  {"temp": 5}  |
     |             |              |               |             +-------------->+
     |             |              |               |             |               |
     +             +              +               +             +               +
```