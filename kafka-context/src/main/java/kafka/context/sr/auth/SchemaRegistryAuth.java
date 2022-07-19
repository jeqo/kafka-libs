package kafka.context.sr.auth;

import com.fasterxml.jackson.databind.JsonNode;
import kafka.context.sr.SchemaRegistryContexts;

public interface SchemaRegistryAuth {

  AuthType type();

  static SchemaRegistryAuth parse(JsonNode auth) {
    final var type = AuthType.valueOf(auth.get("type").textValue());
    return switch (type) {
      case BASIC_AUTH -> new HttpUsernamePasswordAuth(
          type, auth.get("username").textValue(), auth.get("password").textValue());
      default -> new HttpNoAuth();
    };
  }

  default JsonNode toJson() {
    return SchemaRegistryContexts.json.createObjectNode().put("type", type().name());
  }

  enum AuthType {
    NO_AUTH,
    BASIC_AUTH
  }
}
