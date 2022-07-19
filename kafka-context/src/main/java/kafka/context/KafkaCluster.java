package kafka.context;

import com.fasterxml.jackson.databind.JsonNode;
import kafka.context.auth.KafkaAuth;

public record KafkaCluster(String bootstrapServers, KafkaAuth auth) {
  static KafkaCluster parse(JsonNode cluster) {
    return new KafkaCluster(
      cluster.get("bootstrapServers").textValue(),
      KafkaAuth.parse(cluster.get("auth"))
    );
  }

  public JsonNode toJson() {
    final var node = KafkaContexts.json
      .createObjectNode()
      .put("bootstrapServers", bootstrapServers);
    node.set("auth", auth.toJson());
    return node;
  }
}
