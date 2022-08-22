package kafka.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;

public interface KafkaAuth {
  AuthType type();

  static KafkaAuth parse(JsonNode auth) {
    final var type = AuthType.valueOf(auth.get("type").textValue());
    return switch (type) {
      case SASL_PLAIN -> KafkaUsernamePasswordAuth.fromJson(auth);
      case TLS -> KafkaTlsNoAuth.fromJson(auth);
      case MTLS_CERTIFICATE -> KafkaCertificateAuth.fromJson(auth);
      case MTLS_KEYSTORE -> KafkaKeystoreAuth.fromJson(auth);
      default -> new KafkaNoAuth();
    };
  }

  default JsonNode toJson() {
    return new ObjectMapper().createObjectNode().put("type", type().name());
  }

  void addProperties(Properties props);

  enum AuthType {
    PLAINTEXT,
    SASL_PLAIN,
    // SASL_SCRAM,
    // SASL_KERBEROS,
    // SASL_OAUTH,
    TLS,
    MTLS_KEYSTORE,
    MTLS_CERTIFICATE,
  }
}
