package kafka.context.sr;

import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Properties;
import kafka.context.sr.auth.HttpUsernamePasswordAuth;

public record SchemaRegistryContext(String name, SchemaRegistryCluster cluster) {
  static SchemaRegistryContext parse(JsonNode node) {
    final var name = node.get("name").textValue();
    return new SchemaRegistryContext(
      name,
      SchemaRegistryCluster.parse(node.get("cluster"))
    );
  }

  public JsonNode toJson() {
    final var node = SchemaRegistryContexts.json
      .createObjectNode()
      .put("name", this.name);
    node.set("cluster", cluster.toJson());
    return node;
  }

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
}