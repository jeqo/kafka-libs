package kafka.context;

import com.fasterxml.jackson.databind.JsonNode;

public record KafkaCluster(String bootstrapServers, KafkaAuth auth) {
  static KafkaCluster fromJson(JsonNode cluster) {
    return new KafkaCluster(cluster.get("bootstrapServers").textValue(), KafkaAuth.parse(cluster.get("auth")));
  }

  JsonNode toJson() {
    final var node = KafkaContexts.json.createObjectNode().put("bootstrapServers", bootstrapServers);
    node.set("auth", auth.toJson());
    return node;
  }
}
