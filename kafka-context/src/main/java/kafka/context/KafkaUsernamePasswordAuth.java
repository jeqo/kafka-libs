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
    Truststore truststore
  ) {
    return new KafkaUsernamePasswordAuth(
      username,
      passwordHelper().encrypt(password),
      Optional.of(truststore),
      true
    );
  }

  public static KafkaUsernamePasswordAuth build(String username, String password) {
    return new KafkaUsernamePasswordAuth(
      username,
      passwordHelper().encrypt(password),
      Optional.empty(),
      true
    );
  }

  public static KafkaUsernamePasswordAuth build(
    String username,
    String password,
    boolean isSsl
  ) {
    return new KafkaUsernamePasswordAuth(
      username,
      passwordHelper().encrypt(password),
      Optional.empty(),
      isSsl
    );
  }

  static KafkaAuth fromJson(JsonNode auth) {
    return new KafkaUsernamePasswordAuth(
      auth.get("username").textValue(),
      auth.get("password").textValue(),
      Optional.ofNullable(auth.get("truststore")).map(Truststore::parse),
      !auth.has("ssl") || auth.get("ssl").asBoolean()
    );
  }

  @Override
  public AuthType type() {
    return AuthType.SASL_PLAIN;
  }

  @Override
  public JsonNode printJson() {
    var node = (ObjectNode) KafkaAuth.super.printJson();
    return node.put("username", username).put("password", password);
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
          passwordHelper().decrypt(password)
        )
    );
    truststore.ifPresent(value -> value.addProperties(props));
  }
}
