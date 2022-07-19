package kafka.context.sr;

import static kafka.context.ContextHelper.baseDir;
import static kafka.context.ContextHelper.emptyContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record SchemaRegistryContexts(Map<String, SchemaRegistryContext> contextMap) {
  static final ObjectMapper json = new ObjectMapper();

  public static SchemaRegistryContexts load(Path baseDir) throws IOException {
    return from(Files.readAllBytes(schemaRegistryContextConfig(baseDir)));
  }

  public static SchemaRegistryContexts load() throws IOException {
    return load(baseDir());
  }

  public static void save(SchemaRegistryContexts contexts) throws IOException {
    Files.write(schemaRegistryContextConfig(baseDir()), contexts.serialize());
  }

  static Path schemaRegistryContextConfig(Path home) throws IOException {
    final var context = home.resolve("schema-registry.json");
    if (!Files.isRegularFile(context)) {
      System.err.println(
        "Schema Registry Content configuration file doesn't exist, creating one..."
      );
      Files.write(context, emptyContext());
    }

    return context;
  }

  static SchemaRegistryContexts from(byte[] bytes) throws IOException {
    final var tree = json.readTree(bytes);
    if (!tree.isArray()) {
      throw new IllegalArgumentException("JSON is not an array");
    }

    final var array = (ArrayNode) tree;
    final var contexts = new HashMap<String, SchemaRegistryContext>(array.size());
    for (final var node : array) {
      final var context = SchemaRegistryContext.parse(node);
      contexts.put(context.name(), context);
    }

    return new SchemaRegistryContexts(contexts);
  }

  public String names() throws JsonProcessingException {
    return json.writeValueAsString(contextMap.keySet());
  }

  public byte[] serialize() throws JsonProcessingException {
    final var array = json.createArrayNode();
    for (final var ctx : contextMap.values()) array.add(ctx.toJson());
    return json.writeValueAsBytes(array);
  }

  public void add(SchemaRegistryContext ctx) {
    contextMap.put(ctx.name(), ctx);
  }

  public SchemaRegistryContext get(String name) {
    return contextMap.get(name);
  }

  public boolean has(String contextName) {
    return contextMap.containsKey(contextName);
  }

  public void remove(String name) {
    contextMap.remove(name);
  }

  public String namesAndUrls() throws JsonProcessingException {
    final var node = json.createObjectNode();
    contextMap.forEach((k, v) -> node.put(k, v.cluster().urls()));
    return json.writeValueAsString(node);
  }
}
