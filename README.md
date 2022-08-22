# Kafka libraries

> Previously located at [poc-apache-kafka/clients](https://github.com/jeqo/poc-apache-kafka/tree/main/clients)

## Modules

GraalVM:

- [avro-graalvm](./avro-graalvm)
- [kafka-clients-graalvm](./kafka-clients-graalvm)

Connection:

- [kafka-context](./kafka-context): Manage Kafka clusters as named contexts.

Schemas:

- [connect-schema-serde](./connect-schema-serde): Serializer/Deserializer for Kafka Connect records, schemaless (`Map<String, Object>`) or schema-based (`Struct`).
- [datagen-schemas](./datagen-schemas): POJO classes from Schemas used by [Datagen](https://github.com/confluentinc/kafka-connect-datagen/tree/master/src/main/resources)

Producer:

- [kafka-producer-progress-control](./kafka-producer-progress-control): Emit control messages to mark progress on idle time periods.
