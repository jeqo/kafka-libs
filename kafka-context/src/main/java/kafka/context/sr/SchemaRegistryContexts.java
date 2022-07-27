package kafka.context.sr;

import static kafka.context.ContextHelper.baseDir;
import static kafka.context.ContextHelper.from;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import kafka.context.Contexts;
import kafka.context.sr.auth.HttpNoAuth;

/**
 * Confluent Schema Registry contexts file-based store
 */
public final class SchemaRegistryContexts implements Contexts<SchemaRegistryContext> {

  static final ObjectMapper json = new ObjectMapper();
  public static final String CONTEXT_FILENAME = "schema-registry.json";
  public static final SchemaRegistryCluster CONTEXT_DEFAULT = new SchemaRegistryCluster("http://localhost:8081",
    new HttpNoAuth());
  private final Map<String, SchemaRegistryContext> contextMap;

  SchemaRegistryContexts(Map<String, SchemaRegistryContext> contextMap) {
    this.contextMap = contextMap;
  }

  public static SchemaRegistryContexts load() throws IOException {
    return load(baseDir());
  }

  public static SchemaRegistryContexts load(Path baseDir) throws IOException {
    final var contextPath = baseDir.resolve(CONTEXT_FILENAME);
    if (!Files.isRegularFile(contextPath)) {
      System.err.println(
        "Schema registry contexts configuration file doesn't exist, creating one..."
      );
      Files.write(contextPath, createDefault().serialize());
    }
    return new SchemaRegistryContexts(from(contextPath, SchemaRegistryContext::from));
  }

  private static SchemaRegistryContexts createDefault() {
    final var ctx = new SchemaRegistryContext(
      CONTEXT_DEFAULT_NAME,
      CONTEXT_DEFAULT
    );
    return new SchemaRegistryContexts(Map.of(ctx.name(), ctx));
  }

  @Override
  public void save(Path dir) throws IOException {
    Files.write(dir.resolve(CONTEXT_FILENAME), serialize());
  }

  @Override
  public String names() throws JsonProcessingException {
    return json.writeValueAsString(contextMap.keySet());
  }

  @Override
  public void add(SchemaRegistryContext ctx) {
    contextMap.put(ctx.name(), ctx);
  }


  @Override
  public void rename(String oldName, String newName) {
    var ctx = contextMap.remove(oldName);
    contextMap.put(newName, ctx);
  }

  @Override
  public SchemaRegistryContext get(String name) {
    return contextMap.get(name);
  }

  @Override
  public SchemaRegistryContext getDefault() {
    return get(CONTEXT_DEFAULT_NAME);
  }

  @Override
  public boolean has(String contextName) {
    return contextMap.containsKey(contextName);
  }

  @Override
  public void remove(String name) {
    contextMap.remove(name);
  }

  @Override
  public String printNamesAndAddresses() throws JsonProcessingException {
    final var node = json.createObjectNode();
    contextMap.forEach((k, v) -> node.put(k, v.cluster().urls()));
    return json.writeValueAsString(node);
  }

  public byte[] serialize() throws IOException {
    try {
      final var array = json.createArrayNode();
      for (final var ctx : contextMap.values()) {
        array.add(ctx.printJson());
      }
      return json.writeValueAsBytes(array);
    } catch (JsonProcessingException e) {
      throw new IOException(e.getMessage());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null || obj.getClass() != this.getClass()) {
      return false;
    }
    var that = (SchemaRegistryContexts) obj;
    return Objects.equals(this.contextMap, that.contextMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contextMap);
  }

  @Override
  public String toString() {
    return "SchemaRegistryContexts[" + "contextMap=" + contextMap + ']';
  }
}
