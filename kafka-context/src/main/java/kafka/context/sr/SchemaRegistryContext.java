package kafka.context.sr;

import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Properties;
import kafka.context.Context;

public record SchemaRegistryContext(String name, SchemaRegistryCluster cluster) implements Context {
  static SchemaRegistryContext from(JsonNode node) {
    final var name = node.get("name").textValue();
    return new SchemaRegistryContext(name, SchemaRegistryCluster.fromJson(node.get("cluster")));
  }

  public JsonNode printJson() {
    final var node = SchemaRegistryContexts.json.createObjectNode().put("name", this.name);
    node.set("cluster", cluster.toJson());
    return node;
  }

  @Override
  public Properties properties() {
    final var props = new Properties();
    props.put("schema.registry.url", cluster.urls());
    switch (cluster.auth().type()) {
      case BASIC_AUTH -> {
        var auth = (HttpUsernamePasswordAuth) cluster.auth();
        props.put("basic.auth.credentials.source", "USER_INFO");
        props.put(
          "basic.auth.user.info",
          "%s:%s".formatted(auth.username(), passwordHelper().decrypt(auth.password()))
        );
      }
      default -> {}
    }
    return props;
  }

  SchemaRegistryContext withName(String newName) {
    return new SchemaRegistryContext(newName, this.cluster);
  }
}
