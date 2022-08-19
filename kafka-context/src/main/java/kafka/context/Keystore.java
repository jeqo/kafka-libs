package kafka.context;

import static kafka.context.ContextHelper.checkFileIsAccessible;
import static kafka.context.ContextHelper.json;
import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;

public record Keystore(String type, Path location, String password) {
  public static final String DEFAULT_KEYSTORE_TYPE = "PKCS12";

  public static Keystore build(String type, Path location, String password) {
    checkFileIsAccessible(location);
    return new Keystore(type, location.toAbsolutePath(), password);
  }

  public static Keystore build(Path location, String password) {
    checkFileIsAccessible(location);
    return new Keystore(DEFAULT_KEYSTORE_TYPE, location.toAbsolutePath(), password);
  }
  public static Keystore parseJson(JsonNode node) {
    var type = node.get("type").textValue();
    var location = Path.of(node.get("location").textValue());
    var password = passwordHelper().decrypt(node.get("password").textValue());
    return new Keystore(type, location, password);
  }

  public JsonNode toJson() {
    return json
      .createObjectNode()
      .put("type", type)
      .put("location", location.toString())
      .put("password", passwordHelper().encrypt(password));
  }
}
