# Kafka Context

Manage connection to Kafka clusters as contexts.

Defining the authentication and addresses could become tedious when working with multiple environments and the properties have to be passed across multiple clients.

Kubernetes and other platform defined their connection properties as "Contexts", allowing users to define by name which environment and with which account to authenticate to.

Kafka Context library is an attempt to provide this abstraction on top of configuration properties provided regularly when creating Producer, Consumer, or other clients.

## How to use

```xml

```