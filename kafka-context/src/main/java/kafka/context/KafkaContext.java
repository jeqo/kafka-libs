package kafka.context;

import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Properties;
import kafka.context.auth.KafkaUsernamePasswordAuth;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.plain.internals.PlainSaslServer;

public record KafkaContext(String name, KafkaCluster cluster) {
  static KafkaContext parse(JsonNode node) {
    final var name = node.get("name").textValue();
    return new KafkaContext(name, KafkaCluster.parse(node.get("cluster")));
  }

  public JsonNode toJson() {
    final var node = KafkaContexts.json.createObjectNode().put("name", this.name);
    node.set("cluster", cluster.toJson());
    return node;
  }

  public Properties properties() {
    final var props = new Properties();
    props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, cluster.bootstrapServers());
    switch (cluster.auth().type()) {
      case SASL_PLAIN -> {
        props.put(
          CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
          SecurityProtocol.SASL_SSL.name
        );
        props.put(SaslConfigs.SASL_MECHANISM, PlainSaslServer.PLAIN_MECHANISM);
        var auth = (KafkaUsernamePasswordAuth) cluster.auth();
        props.setProperty(
          SaslConfigs.SASL_JAAS_CONFIG,
          "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";".formatted(
              auth.username(),
              passwordHelper().decrypt(auth.password())
            )
        );
      }
      default -> {}
    }
    return props;
  }

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

  public String env(boolean includeAuth) {
    return switch (cluster.auth().type()) {
      case SASL_PLAIN -> includeAuth
        ? """
          export KAFKA_BOOTSTRAP_SERVERS=%s
          export KAFKA_USERNAME=%s
          export KAFKA_PASSWORD=%s""".formatted(
            cluster.bootstrapServers(),
            ((KafkaUsernamePasswordAuth) cluster.auth()).username(),
            passwordHelper()
              .decrypt(((KafkaUsernamePasswordAuth) cluster.auth()).password())
          )
        : "export KAFKA_BOOTSTRAP_SERVERS=%s".formatted(cluster.bootstrapServers());
      default -> "export KAFKA_BOOTSTRAP_SERVERS=%s".formatted(
          cluster.bootstrapServers()
        );
    };
  }
}
