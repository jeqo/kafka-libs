package kafka.context;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;

public record KafkaContext(String name, KafkaCluster cluster) implements Context {
  static KafkaContext fromJson(JsonNode node) {
    final var name = node.get("name").textValue();
    return new KafkaContext(name, KafkaCluster.fromJson(node.get("cluster")));
  }

  JsonNode toJson() {
    final var node = KafkaContexts.json.createObjectNode().put("name", this.name);
    node.set("cluster", cluster.toJson());
    return node;
  }

  @Override
  public Properties properties() {
    final var props = new Properties();
    props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, cluster.bootstrapServers());
    cluster.auth().addProperties(props);
    return props;
  }

  KafkaContext withName(String newName) {
    return new KafkaContext(newName, this.cluster);
  }
}
