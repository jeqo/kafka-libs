package kafka.context;

import java.util.Properties;

/**
 * Cluster context registered and producing different outputs for clients
 * to simplify the connection including security details.
 */
public interface Context {
  String name();

  /**
   * Properties to be used by Kafka client constructors as baseline
   *
   * @return Properties to extend and include on Kafka client constructor
   */
  Properties properties();
}
