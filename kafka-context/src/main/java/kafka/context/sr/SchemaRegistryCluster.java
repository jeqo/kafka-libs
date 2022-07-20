package kafka.context.sr;

import com.fasterxml.jackson.databind.JsonNode;
import kafka.context.sr.auth.SchemaRegistryAuth;

public record SchemaRegistryCluster(String urls, SchemaRegistryAuth auth) {
  static SchemaRegistryCluster parse(JsonNode cluster) {
    return new SchemaRegistryCluster(
      cluster.get("urls").textValue(),
      SchemaRegistryAuth.parse(cluster.get("auth"))
    );
  }

  public JsonNode printJson() {
    final var node = SchemaRegistryContexts.json.createObjectNode().put("urls", urls);
    node.set("auth", auth.printJson());
    return node;
  }
}
