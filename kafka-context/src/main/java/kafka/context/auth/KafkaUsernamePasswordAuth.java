package kafka.context.auth;

import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record KafkaUsernamePasswordAuth(AuthType authType, String username, String password)
    implements KafkaAuth {

  public static KafkaUsernamePasswordAuth build(AuthType authType, String username, String password) {
    return new KafkaUsernamePasswordAuth(authType, username, passwordHelper().encrypt(password));
  }

  @Override
  public AuthType type() {
    return authType;
  }

  @Override
  public JsonNode toJson() {
    var node = (ObjectNode) KafkaAuth.super.toJson();
    return node.put("username", username).put("password", password);
  }
}
