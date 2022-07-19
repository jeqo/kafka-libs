package kafka.context.auth;

public record KafkaNoAuth() implements KafkaAuth {
  @Override
  public AuthType type() {
    return AuthType.PLAINTEXT;
  }
}
