# Developer Guide

## Modification of the dispatch router configuration file (qdrouterd.json)
The `qdrouterd.json` file allows the configuration of the Apache Qpid dispatch router, which forms the central entry point to the messaging network (currently Apache Artemis) and also exposes the northbound API of Eclipse Hono.
The following configration options are currently used and modified by example in `static/router/qdrouterd.json` description:

### sslProfile "server"
This is the SSL/TLS profile for the external (northbound side) API of the messaging network.
| Attribute             | Description           | Default Value         |
| ---                   | ---                   | ---                   |
| caCertFile            | path to the truststore used for validating client certificates | `/qpid/truststore.pem` |
| certFile              | path to the certificate the server is using | `/qpid/router-cert.pem` |
| privateKeyFile        | path to the private key the server is using combined with the certFile | `/qpid/router-key.pem` |
| uidFormat             | defines which relative distinguished names (RDN) are used to check with the ones defined in the client certificates | `ou` |


### sslProfile "internal"
This is the SSL/TLS profile for the internal API the protocol adapers use to communicate with the messaging network.
| Attribute             | Description           | Default Value         |
| ---                   | ---                   | ---                   |
| caCertFile            | path to the truststore used for validating client certificates | `/qpid/truststore.pem` |
| certFile              | path to the certificate the server is using | `/qpid/router-cert.pem` |
| privateKeyFile        | path to the private key the server is using combined with the certFile | `/qpid/router-key.pem` |
| uidFormat             | defines which relative distinguished names (RDN) are used to check with the ones defined in the client certificates | `ou` |

As you can see we use the same setting for both endpoints (internal/external). However, if needed, you can differentiate between the two and set e.g. different certificates for the external API.


### listener on port 5671
This is the listener spec for the external encrypted API using the server SSL/TLS profile.
| Attribute             | Description           | Default Value         |
| ---                   | ---                   | ---                   |
| sslProfile            | the defined SSL/TLS profile that should be used (see above) | `server` |
| requireSsl            | indicates that we want to secure our endpoint | `true` |
| port                  | the port on which the endpoint is exposed | `5671` |
| saslMechanisms        | the [SASL](https://tools.ietf.org/html/rfc4422) mechanism to be used to authenticate clients | `EXTERNAL` equivalents to client certificate authentication |


### listener on port 5673
This is the listener spec for the internal encrypted API using the server SSL/TLS profile.
| Attribute             | Description           | Default Value         |
| ---                   | ---                   | ---                   |
| sslProfile            | the defined SSL/TLS profile that should be used (see above) | `server` |
| requireSsl            | indicates that we want to secure our endpoint | `true` |
| port                  | the port on which the endpoint is exposed | `5671` |
| saslMechanisms        | the [SASL](https://tools.ietf.org/html/rfc4422) mechanism to be used to authenticate clients | `EXTERNAL` equivalents to client certificate authentication |


### vhost hono
This defines the default AMQP vhost (for more see [this documentation](https://www.rabbitmq.com/vhosts.html), note that even though we don't use RabbitMQ the concept is the same) for the external endpoint and specifies the access rules.
| Attribute             | Description           | Default Value         |
| ---                   | ---                   | ---                   |
| groups | define some groups and specify their role/access level | $default |
| groups.$default.users | the user field will be extracted from the certificate subject, as defined in the `sslProfile.uidFormat` (e.g. ou will extract the organization name and the organization unit, see [here](https://qpid.apache.org/releases/qpid-dispatch-1.4.1/man/qdrouterd.conf.html) for more info) | `ou` |
| groups.$default.sources | the source routes the client at the northbound side is allowed to consume (outbound) | `telemetry/*`, `event/*`, `command_response/*` |
| groups.$default.targets | the target routes the client at the northbound side is allowed to consume (inbound) | `command/*` |