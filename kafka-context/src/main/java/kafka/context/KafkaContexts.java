package kafka.context;

import static kafka.context.ContextHelper.baseDir;
import static kafka.context.ContextHelper.from;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Kafka contexts file-based store
 */
public final class KafkaContexts implements Contexts<KafkaContext> {

  static final ObjectMapper json = new ObjectMapper();
  public static final String CONTEXT_FILENAME = "kafka.json";
  public static final KafkaCluster CONTEXT_DEFAULT = new KafkaCluster("localhost:9092", new KafkaNoAuth());
  private final Path baseDir;
  private final Map<String, KafkaContext> contextMap;

  /**
   * @param baseDir base dir for Kafka context files
   * @param contextMap kafka contexts by name
   */
  KafkaContexts(Path baseDir, Map<String, KafkaContext> contextMap) {
    this.baseDir = baseDir;
    this.contextMap = contextMap;
  }

  /**
   * Load Kafka Contexts from files in default location.
   *
   * @return set of Kafka Contexts
   * @throws IOException when file IO exceptions happen
   */
  public static KafkaContexts load() throws IOException {
    return load(baseDir());
  }

  /**
   * Load Kafka Contexts from files in {@code baseDir}
   *
   * @param baseDir Location to search for contexts
   * @return set of Kafka Contexts
   * @throws IOException when file IO exceptions happen
   */
  public static KafkaContexts load(Path baseDir) throws IOException {
    final var contextPath = baseDir.resolve(CONTEXT_FILENAME);
    if (!Files.isRegularFile(contextPath)) {
      System.err.println("Kafka Context configuration file doesn't exist, creating one...");
      Files.write(contextPath, init(baseDir).serialize());
    }
    return new KafkaContexts(baseDir, from(contextPath, KafkaContext::fromJson));
  }

  private static KafkaContexts init(Path baseDir) {
    final var ctx = new KafkaContext(CONTEXT_DEFAULT_NAME, CONTEXT_DEFAULT);
    return new KafkaContexts(baseDir, Map.of(ctx.name(), ctx));
  }

  @Override
  public void save() throws IOException {
    Files.write(baseDir.resolve(CONTEXT_FILENAME), serialize());
  }

  @Override
  public String names() throws JsonProcessingException {
    return json.writeValueAsString(contextMap.keySet());
  }

  @Override
  public void add(KafkaContext ctx) {
    contextMap.put(ctx.name(), ctx);
  }

  @Override
  public void rename(String oldName, String newName) {
    var ctx = contextMap.remove(oldName);
    contextMap.put(newName, ctx.withName(newName));
  }

  @Override
  public KafkaContext get(String name) {
    return contextMap.get(name);
  }

  @Override
  public KafkaContext getDefault() {
    return contextMap.get(CONTEXT_DEFAULT_NAME);
  }

  @Override
  public boolean has(String name) {
    return contextMap.containsKey(name);
  }

  @Override
  public void remove(String name) {
    contextMap.remove(name);
  }

  @Override
  public String printNamesAndAddresses() throws JsonProcessingException {
    final var node = json.createObjectNode();
    contextMap.forEach((k, v) -> node.put(k, v.cluster().bootstrapServers()));
    return json.writeValueAsString(node);
  }

  @Override
  public byte[] serialize() throws IOException {
    try {
      final var array = json.createArrayNode();
      for (final var ctx : contextMap.values()) {
        array.add(ctx.toJson());
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
    var that = (KafkaContexts) obj;
    return Objects.equals(this.contextMap, that.contextMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contextMap);
  }

  @Override
  public String toString() {
    return "KafkaContexts[" + "contextMap=" + contextMap + ']';
  }
}
