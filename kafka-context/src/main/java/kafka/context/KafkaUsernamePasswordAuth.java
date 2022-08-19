package kafka.context;

import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.plain.internals.PlainSaslServer;

public record KafkaUsernamePasswordAuth(
  String username,
  String password,
  Optional<Truststore> truststore,
  boolean isSsl
)
  implements KafkaAuth {
  public static KafkaUsernamePasswordAuth build(
    String username,
    String password,
    Truststore truststore,
    boolean isSsl
  ) {
    return new KafkaUsernamePasswordAuth(username, password, Optional.of(truststore), isSsl);
  }

  public static KafkaUsernamePasswordAuth build(String username, String password, boolean isSsl) {
    return new KafkaUsernamePasswordAuth(username, password, Optional.empty(), isSsl);
  }

  static KafkaAuth fromJson(JsonNode auth) {
    return new KafkaUsernamePasswordAuth(
      auth.get("username").textValue(),
      passwordHelper().decrypt(auth.get("password").textValue()),
      Optional.ofNullable(auth.get("truststore")).map(Truststore::fromJson),
      !auth.has("ssl") || auth.get("ssl").asBoolean()
    );
  }

  @Override
  public AuthType type() {
    return AuthType.SASL_PLAIN;
  }

  @Override
  public JsonNode toJson() {
    var node = (ObjectNode) KafkaAuth.super.toJson();
    node.put("username", username).put("password", passwordHelper().encrypt(password)).put("ssl", isSsl);
    truststore.ifPresent(ts -> node.set("truststore", ts.toJson()));
    return node;
  }

  @Override
  public void addProperties(Properties props) {
    props.put(
      CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
      isSsl ? SecurityProtocol.SASL_SSL.name : SecurityProtocol.SASL_PLAINTEXT.name
    );
    props.put(SaslConfigs.SASL_MECHANISM, PlainSaslServer.PLAIN_MECHANISM);
    props.setProperty(
      SaslConfigs.SASL_JAAS_CONFIG,
      "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";".formatted(
          username,
          password
        )
    );
    truststore.ifPresent(value -> value.addProperties(props));
  }
}
