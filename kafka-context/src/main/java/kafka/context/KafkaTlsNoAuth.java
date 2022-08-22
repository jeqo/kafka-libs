package kafka.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;

public record KafkaTlsNoAuth(Truststore truststore) implements KafkaAuth {
  @Override
  public AuthType type() {
    return AuthType.TLS;
  }

  public static KafkaTlsNoAuth build(Truststore truststore) {
    return new KafkaTlsNoAuth(truststore);
  }

  public static KafkaTlsNoAuth fromJson(JsonNode node) {
    return new KafkaTlsNoAuth(Truststore.fromJson(node));
  }

  @Override
  public JsonNode toJson() {
    var node = (ObjectNode) KafkaAuth.super.toJson();
    node.set("truststore", truststore.toJson());
    return node;
  }

  @Override
  public void addProperties(Properties props) {
    props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
    truststore.addProperties(props);
  }
}
