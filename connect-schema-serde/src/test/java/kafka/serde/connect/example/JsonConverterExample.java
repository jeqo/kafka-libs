package kafka.serde.connect.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import kafka.serde.connect.SchemaAndValueSerde;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.json.JsonConverter;

public class JsonConverterExample {

  public static void main(String[] args) {
    var mapper = new ObjectMapper();
    var jsonNode = mapper.createObjectNode().put("test", "t1").put("value", "v1");
    var map = mapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {});

    var converter = new JsonConverter();
    converter.configure(Map.of("schemas.enabled", "false", "converter.type", "value"));
    var serde = new SchemaAndValueSerde(converter);
    var bytes = serde.serializer().serialize("test", new SchemaAndValue(null, map));
    System.out.println(new String(bytes));
  }
}
