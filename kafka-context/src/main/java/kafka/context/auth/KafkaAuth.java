package kafka.context.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.context.KafkaContexts;

public interface KafkaAuth {
  AuthType type();

  static KafkaAuth parse(JsonNode auth) {
    final var type = AuthType.valueOf(auth.get("type").textValue());
    return switch (type) {
      case SASL_PLAIN -> new KafkaUsernamePasswordAuth(
        type,
        auth.get("username").textValue(),
        auth.get("password").textValue()
      );
      default -> new KafkaNoAuth();
    };
  }

  default JsonNode toJson() {
    return new ObjectMapper().createObjectNode().put("type", type().name());
  }

  enum AuthType {
    PLAINTEXT,
    SASL_PLAIN,
  }
}
