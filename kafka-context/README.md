# Kafka Context

Manage connection to Kafka clusters as contexts.

Defining the authentication and addresses could become tedious when working with multiple environments and the properties have to be passed across multiple clients.

Kubernetes and other platform defined their connection properties as "Contexts", allowing users to define by name which environment and with which account to authenticate to.

Kafka Context library is an attempt to provide this abstraction on top of configuration properties provided regularly when creating Producer, Consumer, or other clients.

## Features

- Kafka:
  - Plaintext
  - SASL_SSL (e.g. CCloud) with username/password
- Schema Registry:
  - HTTP
    - No Auth
    - Basic Auth (username/password)

### Backlog

- [ ] Custom SSL (using PEM)
- [ ] SASL Scram
- [ ] mTLS
- [ ] Kerberos
- [ ] OAuth

## How to use

Add `kafka-context` as dependency:

```xml
<dependencies>
  <dependency>
    <groupId>io.github.jeqo.kafka</groupId>
    <artifactId>kafka-context</artifactId>
    <version>${kafka-context.version}</version>
  </dependency>
</dependencies>
```

Load Kafka Contexts from file-system (default location: $HOME/.kafka):

```java
import kafka.context.KafkaContexts;
import kafka.context.sr.SchemaRegistryContexts;

public class App {

  public static void main(String[] args) {
    // load by name or default
    var ctx = KafkaContexts.load().getDefault();
    var props = ctx.properties();

    // extend with SR
    var sr = SchemaRegistryContexts.load().getDefault();
    props.putAll(sr.properties());

    // extend properties and
    props.put(GROUP_ID, "test-consumer");
    // create Kafka clients 
    var consumer = new KafkaConsumer(props);
  }
}
```

## Tooling

- [`kfk-ctx`](https://github.com/jeqo/kafka-cli/tree/main/context) CLI: Manages contexts via command-line