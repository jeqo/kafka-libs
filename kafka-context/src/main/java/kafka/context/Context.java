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

  /**
   * Command help to inject properties to kcat output
   *
   * @return kcat command output
   */
  String kcat();

  /**
   * Environment variables to export and use in command line or application runtime
   *
   * @param includeAuth whether to include security details to output
   * @return environment variables output
   */
  String env(boolean includeAuth);
}
