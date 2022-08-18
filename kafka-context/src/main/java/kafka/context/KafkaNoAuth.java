package kafka.context;

import java.util.Properties;

public record KafkaNoAuth() implements KafkaAuth {
  public static KafkaNoAuth build() {
    return new KafkaNoAuth();
  }

  @Override
  public AuthType type() {
    return AuthType.PLAINTEXT;
  }

  @Override
  public void addProperties(Properties props) {}
}
