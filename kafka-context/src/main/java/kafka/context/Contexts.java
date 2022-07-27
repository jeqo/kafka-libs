package kafka.context;

import static kafka.context.ContextHelper.baseDir;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Path;

public interface Contexts<T extends Context> {
  String CONTEXT_DEFAULT_NAME = "default";

  /**
   * Write Kafka Contexts to configuration files
   *
   * @throws IOException when file IO exception happens
   */
  default void save() throws IOException {
    save(baseDir());
  }

  /**
   * Write Kafka Contexts to configuration files
   *
   * @param dir Path to directory where configuration files are located
   * @throws IOException when file IO exception happens
   */
  void save(Path dir) throws IOException;

  /**
   * Kafka Context names
   *
   * @return set of names
   * @throws JsonProcessingException when context file is not readable
   */
  String names() throws JsonProcessingException;

  /**
   * Append context
   *
   * @param ctx context to add
   */
  void add(T ctx);

  /**
   * Get context by name
   *
   * @param name Context name
   * @return Context or null if not found
   */
  Context get(String name);

  /**
   * Get context named "default" or the first one defined in the config file
   *
   * @return Context or null if not found
   */
  Context getDefault();

  /**
   * Checks if context exists by name
   *
   * @param name Context name
   * @return true if context exists
   */
  boolean has(String name);

  /**
   * Delete context from memory
   *
   * @param name Context name to remove
   */
  void remove(String name);

  /**
   * Prints a JSON representation of names and addresses (bootstrap servers)
   *
   * @return JSON of names and bootstrap servers
   * @throws JsonProcessingException if there's an issue with JSON representation
   */
  String printNamesAndAddresses() throws JsonProcessingException;

  byte[] serialize() throws IOException;
}
