package kafka.context.sr;

public record HttpNoAuth() implements SchemaRegistryAuth {
  @Override
  public AuthType type() {
    return AuthType.NO_AUTH;
  }
}
