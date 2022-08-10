package kafka.serde.connect.example;

import io.confluent.connect.avro.AvroConverter;
import io.confluent.connect.avro.AvroData;
import java.util.Map;
import kafka.serde.connect.SchemaAndValueSerde;

public class AvroConverterExample {

  public static void main(String[] args) {
    var data = ksql.StockTrade
      .newBuilder()
      .setAccount("123")
      .setPrice(100)
      .setQuantity(1)
      .setSymbol("USD")
      .setSide("A")
      .setUserid("U001")
      .build();
    var avro = new AvroData(10);
    var schemaAndValue = avro.toConnectData(data.getSchema(), data);
    var converter = new AvroConverter();
    converter.configure(Map.of("schema.registry.url", "http://localhost:8081"), false);
    try (var serde = new SchemaAndValueSerde(converter)) {
      var bytes = serde.serializer().serialize("test", schemaAndValue);
      System.out.println(new String(bytes));
    }
  }
}
