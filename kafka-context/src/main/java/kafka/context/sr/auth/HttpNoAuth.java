package kafka.context.sr.auth;

public record HttpNoAuth() implements SchemaRegistryAuth {

  @Override
  public AuthType type() {
    return AuthType.NO_AUTH;
  }
}
