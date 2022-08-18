package kafka.context.sr;

import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Properties;
import kafka.context.Context;

public record SchemaRegistryContext(String name, SchemaRegistryCluster cluster)
  implements Context {
  static SchemaRegistryContext from(JsonNode node) {
    final var name = node.get("name").textValue();
    return new SchemaRegistryContext(
      name,
      SchemaRegistryCluster.fromJson(node.get("cluster"))
    );
  }

  public JsonNode printJson() {
    final var node = SchemaRegistryContexts.json
      .createObjectNode()
      .put("name", this.name);
    node.set("cluster", cluster.toJson());
    return node;
  }

  @Override
  public Properties properties() {
    final var props = new Properties();
    props.put("schema.registry.url", cluster.urls());
    switch (cluster.auth().type()) {
      case BASIC_AUTH -> {
        props.put("basic.auth.credentials.source", "USER_INFO");
        var auth = (HttpUsernamePasswordAuth) cluster.auth();
        props.put(
          "basic.auth.user.info",
          "%s:%s".formatted(auth.username(), passwordHelper().decrypt(auth.password()))
        );
      }
      default -> {}
    }
    return props;
  }

  @Override
  public String kcat() {
    var urls = cluster().urls();
    final var https = "https://";
    return switch (cluster.auth().type()) {
      case BASIC_AUTH -> "\\\n -r " +
      https +
      "$SCHEMA_REGISTRY_USERNAME:$SCHEMA_REGISTRY_PASSWORD" +
      "@" +
      urls.substring(https.length()) +
      " -s value=avro";
      case NO_AUTH -> "\\\n -r " + urls + " -s value=avro";
    };
  }

  @Override
  public String env(boolean includeAuth) {
    var urls = cluster().urls();
    return switch (cluster.auth().type()) {
      case BASIC_AUTH -> includeAuth
        ? """
                    export SCHEMA_REGISTRY_URL=%s
                    export SCHEMA_REGISTRY_USERNAME=%s
                    export SCHEMA_REGISTRY_PASSWORD=%s""".formatted(
            urls,
            ((HttpUsernamePasswordAuth) cluster.auth()).username(),
            passwordHelper()
              .decrypt(((HttpUsernamePasswordAuth) cluster.auth()).password())
          )
        : "export SCHEMA_REGISTRY_URL=%s".formatted(urls);
      case NO_AUTH -> "export SCHEMA_REGISTRY_URL=%s".formatted(urls);
    };
  }
  SchemaRegistryContext withName(String newName) {
    return new SchemaRegistryContext(newName, this.cluster);
  }
}
