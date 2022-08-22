# Connect Schema Serializer/Deserializer

Piggybacks on Connect converters to implement Kafka applications based on _generic_ types instead of specific POJOs. 

- Type: `SchemaAndValue`
- Value: either `Map<String, Object>` for Schemaless values (e.g. JSON), or `Struct` for Schema Registry-based values (e.g. Avro, Protobuf).

## How to use

Add dependency:

```xml
    <dependency>
      <groupId>io.github.jeqo.kafka</groupId>
      <artifactId>connect-schema-serde</artifactId>
      <version>${kafka-libs.version}</version>
    </dependency>
```

And converter dependency, e.g. JSON:

```xml
    <dependency>
      <groupId>org.apache.kafka</groupId>
      <artifactId>connect-json</artifactId>
      <version>${kafka.version}</version>
    </dependency>
```

Define the Serde by passing a Converter implementation, e.g. JSON converter:

```java
    var converter = new JsonConverter();
    converter.configure(Map.of("schemas.enable", "false", "converter.type", "value"));
    var valueSerde = new SchemaAndValueSerde(converter);
```

Then use it to implement a generic client, e.g. with Kafka Streams:

```java
    builder
      .stream("test-input", Consumed.with(Serdes.String(), valueSerde))
      .mapValues((s, schemaAndValue) -> Requirements.requireMapOrNull(schemaAndValue.value(), "testing"))
      .filter((s, map) -> !map.isEmpty())
      .filter((s, map) -> map.get("countryCode").toString().equals("PE"))
      .mapValues((s, map) -> new SchemaAndValue(null, map))
      .to("test-output", Produced.with(Serdes.String(), valueSerde));
```
