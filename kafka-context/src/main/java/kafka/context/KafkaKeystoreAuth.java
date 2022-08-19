package kafka.context;

import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.common.config.SslConfigs;

public record KafkaKeystoreAuth(Keystore keystore, String keyPassword, Optional<Truststore> truststore)
  implements KafkaAuth {
  public static KafkaKeystoreAuth build(Keystore keystore, String keyPassword) {
    return new KafkaKeystoreAuth(keystore, keyPassword, Optional.empty());
  }

  public static KafkaKeystoreAuth build(Keystore keystore, String keyPassword, Truststore truststore) {
    return new KafkaKeystoreAuth(keystore, keyPassword, Optional.of(truststore));
  }

  static KafkaAuth fromJson(JsonNode auth) {
    return new KafkaKeystoreAuth(
      Keystore.parseJson(auth.get("keystore")),
      passwordHelper().decrypt(auth.get("keyPassword").textValue()),
      Optional.ofNullable(auth.get("truststore")).map(Truststore::fromJson)
    );
  }

  @Override
  public JsonNode toJson() {
    var node = (ObjectNode) KafkaAuth.super.toJson();
    node.set("keystore", keystore.toJson());
    node.put("keyPassword", passwordHelper().encrypt(keyPassword));
    truststore.ifPresent(ts -> node.set("truststore", ts.toJson()));
    return node;
  }

  @Override
  public AuthType type() {
    return AuthType.SSL_KEYSTORE;
  }

  @Override
  public void addProperties(Properties props) {
    props.setProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, keystore.type());
    props.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keystore.location().toString());
    props.setProperty(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystore.password());
    props.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
    truststore.ifPresent(value -> value.addProperties(props));
  }
}
