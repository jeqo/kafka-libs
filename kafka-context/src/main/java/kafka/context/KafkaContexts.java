package kafka.context;

import static kafka.context.ContextHelper.baseDir;
import static kafka.context.ContextHelper.emptyContext;
import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record KafkaContexts(Map<String, KafkaContext> contextMap) {
  static final ObjectMapper json = new ObjectMapper();

  public static void save(KafkaContexts contexts) throws IOException {
    Files.write(kafkaContextConfig(baseDir()), contexts.serialize());
  }

  static Path kafkaContextConfig(Path home) throws IOException {
    final var context = home.resolve("kafka.json");
    if (!Files.isRegularFile(context)) {
      System.err.println(
        "Kafka Content configuration file doesn't exist, creating one..."
      );
      Files.write(context, emptyContext());
    }

    return context;
  }

  static KafkaContexts load(Path baseDir) throws IOException {
    return from(Files.readAllBytes(kafkaContextConfig(baseDir)));
  }

  public static KafkaContexts load() throws IOException {
    return load(baseDir());
  }

  static KafkaContexts from(byte[] bytes) throws IOException {
    final var tree = json.readTree(bytes);
    if (!tree.isArray()) throw new IllegalArgumentException("JSON is not an array");

    final var array = (ArrayNode) tree;
    final var contexts = new HashMap<String, KafkaContext>(array.size());
    for (final var node : array) {
      final var context = KafkaContext.parse(node);
      contexts.put(context.name(), context);
    }

    return new KafkaContexts(contexts);
  }

  public String names() throws JsonProcessingException {
    return json.writeValueAsString(contextMap.keySet());
  }

  public byte[] serialize() throws JsonProcessingException {
    final var array = json.createArrayNode();
    for (final var ctx : contextMap.values()) array.add(ctx.toJson());
    return json.writeValueAsBytes(array);
  }

  public void add(KafkaContext ctx) {
    contextMap.put(ctx.name(), ctx);
  }

  public KafkaContext get(String name) {
    return contextMap.get(name);
  }

  public boolean has(String name) {
    return contextMap.containsKey(name);
  }

  public void remove(String name) {
    contextMap.remove(name);
  }

  public String namesAndBootstrapServers() throws JsonProcessingException {
    final var node = json.createObjectNode();
    contextMap.forEach((k, v) -> node.put(k, v.cluster().bootstrapServers()));
    return json.writeValueAsString(node);
  }
}
