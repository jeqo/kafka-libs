package kafka.context;

import static kafka.context.ContextHelper.checkFileIsAccessible;
import static kafka.context.ContextHelper.passwordHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;

public record KafkaCertificateAuth(
  Path privateKeyFilePath,
  Optional<String> privateKeyPassword,
  Path publicCertificateChainFilePath,
  Optional<Truststore> truststore
)
  implements KafkaAuth {
  public static KafkaCertificateAuth build(
    Path privateKeyFilePath,
    String privateKeyPassword,
    Path publicCertificateChainFilePath
  ) {
    checkFileIsAccessible(privateKeyFilePath);
    checkFileIsAccessible(publicCertificateChainFilePath);
    return new KafkaCertificateAuth(
      privateKeyFilePath.toAbsolutePath(),
      Optional.of(privateKeyPassword),
      publicCertificateChainFilePath.toAbsolutePath(),
      Optional.empty()
    );
  }

  public static KafkaCertificateAuth build(Path privateKeyFilePath, Path publicCertificateChainFilePath) {
    checkFileIsAccessible(privateKeyFilePath);
    checkFileIsAccessible(publicCertificateChainFilePath);
    return new KafkaCertificateAuth(
      privateKeyFilePath.toAbsolutePath(),
      Optional.empty(),
      publicCertificateChainFilePath.toAbsolutePath(),
      Optional.empty()
    );
  }

  public static KafkaCertificateAuth build(
    Path privateKeyFilePath,
    Path publicCertificateChainFilePath,
    Truststore truststore
  ) {
    checkFileIsAccessible(privateKeyFilePath);
    checkFileIsAccessible(publicCertificateChainFilePath);
    return new KafkaCertificateAuth(
      privateKeyFilePath.toAbsolutePath(),
      Optional.empty(),
      publicCertificateChainFilePath.toAbsolutePath(),
      Optional.of(truststore)
    );
  }

  public static KafkaCertificateAuth build(
    Path privateKeyFilePath,
    String privateKeyPassword,
    Path publicCertificateChainFilePath,
    Truststore truststore
  ) {
    checkFileIsAccessible(privateKeyFilePath);
    checkFileIsAccessible(publicCertificateChainFilePath);
    return new KafkaCertificateAuth(
      privateKeyFilePath.toAbsolutePath(),
      Optional.of(privateKeyPassword),
      publicCertificateChainFilePath.toAbsolutePath(),
      Optional.of(truststore)
    );
  }

  @Override
  public JsonNode toJson() {
    var node = (ObjectNode) KafkaAuth.super.toJson();
    node.put("privateKey", privateKeyFilePath.toString());
    privateKeyPassword.ifPresent(password -> node.put("privateKeyPassword", passwordHelper().encrypt(password)));
    node.put("publicCertificateChain", publicCertificateChainFilePath.toString());
    truststore.ifPresent(ts -> node.set("truststore", ts.toJson()));
    return node;
  }

  static KafkaAuth fromJson(JsonNode auth) {
    return new KafkaCertificateAuth(
      Path.of(auth.get("privateKey").textValue()),
      auth.has("privateKeyPassword")
        ? Optional.of(passwordHelper().decrypt(auth.get("privateKeyPassword").textValue()))
        : Optional.empty(),
      Path.of(auth.get("publicCertificateChain").textValue()),
      Optional.ofNullable(auth.get("truststore")).map(Truststore::fromJson)
    );
  }

  @Override
  public AuthType type() {
    return AuthType.MTLS_CERTIFICATE;
  }

  @Override
  public void addProperties(Properties props) {
    try {
      props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
      props.setProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
      props.setProperty(
        SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG,
        Files.readString(publicCertificateChainFilePath).trim()
      );
      props.setProperty(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, Files.readString(privateKeyFilePath).trim());
      privateKeyPassword.ifPresent(password -> props.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, password));
      truststore.ifPresent(value -> value.addProperties(props));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
