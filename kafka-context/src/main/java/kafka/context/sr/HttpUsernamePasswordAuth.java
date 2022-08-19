package kafka.context.sr;

import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.PasswordAuthentication;

public record HttpUsernamePasswordAuth(AuthType authType, String username, String password)
  implements SchemaRegistryAuth {
  public static SchemaRegistryAuth build(AuthType authType, String username, String password) {
    return new HttpUsernamePasswordAuth(authType, username, passwordHelper().encrypt(password));
  }

  public PasswordAuthentication passwordAuth() {
    return new PasswordAuthentication(username, passwordHelper().decrypt(password).toCharArray());
  }

  @Override
  public AuthType type() {
    return authType;
  }

  @Override
  public JsonNode printJson() {
    var node = (ObjectNode) SchemaRegistryAuth.super.printJson();
    return node.put("username", username).put("password", password);
  }
}
