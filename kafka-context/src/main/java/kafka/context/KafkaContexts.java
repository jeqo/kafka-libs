package kafka.context;

import static kafka.context.ContextHelper.baseDir;
import static kafka.context.ContextHelper.contextPath;
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
  private final Map<String, KafkaContext> contextMap;

  /**
   * @param contextMap kafka contexts by name
   */
  KafkaContexts(Map<String, KafkaContext> contextMap) {
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
    return new KafkaContexts(
      from(contextPath(baseDir, CONTEXT_FILENAME), KafkaContext::from)
    );
  }

  @Override
  public void save(Path dir) throws IOException {
    Files.write(contextPath(dir, CONTEXT_FILENAME), serialize());
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
  public KafkaContext get(String name) {
    return contextMap.get(name);
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

  byte[] serialize() throws JsonProcessingException {
    final var array = json.createArrayNode();
    for (final var ctx : contextMap.values()) array.add(ctx.printJson());
    return json.writeValueAsBytes(array);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
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
