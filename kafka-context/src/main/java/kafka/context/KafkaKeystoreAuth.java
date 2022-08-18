package kafka.context;

import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.common.config.SslConfigs;

public record KafkaKeystoreAuth(
  Keystore keystore,
  String keyPassword,
  Optional<Truststore> truststore
)
  implements KafkaAuth {
  public static KafkaKeystoreAuth build(Keystore keystore, String keyPassword) {
    return new KafkaKeystoreAuth(
      keystore,
      passwordHelper().encrypt(keyPassword),
      Optional.empty()
    );
  }

  public static KafkaKeystoreAuth build(
    Keystore keystore,
    String keyPassword,
    Truststore truststore
  ) {
    return new KafkaKeystoreAuth(
      keystore,
      passwordHelper().encrypt(keyPassword),
      Optional.of(truststore)
    );
  }
  static KafkaAuth fromJson(JsonNode auth) {
    return new KafkaKeystoreAuth(
      Keystore.parseJson(auth),
      auth.get("keyPassword").textValue(),
      Optional.ofNullable(auth.get("truststore")).map(Truststore::parse)
    );
  }

  @Override
  public AuthType type() {
    return AuthType.SSL_KEYSTORE;
  }

  @Override
  public void addProperties(Properties props) {
    props.setProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, keystore.type());
    props.setProperty(
      SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
      keystore.location().toAbsolutePath().toString()
    );
    props.setProperty(
      SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
      passwordHelper().decrypt(keystore.password())
    );
    props.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);
    // TODO add additional SSL configs (TLS version, supported alg, etc.)
  }
}
