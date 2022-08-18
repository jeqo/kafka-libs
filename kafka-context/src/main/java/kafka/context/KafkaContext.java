package kafka.context;

import static kafka.context.ContextHelper.passwordHelper;

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

  @Override
  public String kcat() {
    return switch (cluster.auth().type()) {
      case SASL_PLAIN -> """
                    kcat -b %s -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \\
                     -X sasl.username=$KAFKA_USERNAME -X sasl.password=$KAFKA_PASSWORD \\
                     -X api.version.request=true\040""".formatted(
          cluster.bootstrapServers()
        );
      default -> "kcat -b %s ".formatted(cluster.bootstrapServers());
    };
  }

  @Override
  public String env(boolean includeAuth) {
    return switch (cluster.auth().type()) {
      case SASL_PLAIN -> {
        final var auth = (KafkaUsernamePasswordAuth) cluster.auth();
        yield includeAuth
          ? """
                      export KAFKA_BOOTSTRAP_SERVERS=%s
                      export KAFKA_USERNAME=%s
                      export KAFKA_PASSWORD=%s""".formatted(
              cluster.bootstrapServers(),
              auth.username(),
              passwordHelper().decrypt(auth.password())
            )
          : "export KAFKA_BOOTSTRAP_SERVERS=%s".formatted(cluster.bootstrapServers());
      }
      default -> "export KAFKA_BOOTSTRAP_SERVERS=%s".formatted(
          cluster.bootstrapServers()
        );
    };
  }

  KafkaContext withName(String newName) {
    return new KafkaContext(newName, this.cluster);
  }
}
