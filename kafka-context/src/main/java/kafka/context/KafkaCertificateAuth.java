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
  String privateKeyPassword, //TODO make private key psw optional
  Optional<Path> publicCertificateChainFilePath,
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
      privateKeyPassword,
      Optional.of(publicCertificateChainFilePath).map(Path::toAbsolutePath),
      Optional.empty()
    );
  }

  public static KafkaCertificateAuth build(Path privateKeyFilePath, String privateKeyPassword) {
    checkFileIsAccessible(privateKeyFilePath);
    return new KafkaCertificateAuth(
      privateKeyFilePath.toAbsolutePath(),
      privateKeyPassword,
      Optional.empty(),
      Optional.empty()
    );
  }

  public static KafkaCertificateAuth build(Path privateKeyFilePath, String privateKeyPassword, Truststore truststore) {
    checkFileIsAccessible(privateKeyFilePath);
    return new KafkaCertificateAuth(
      privateKeyFilePath.toAbsolutePath(),
      privateKeyPassword,
      Optional.empty(),
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
      privateKeyPassword,
      Optional.of(publicCertificateChainFilePath).map(Path::toAbsolutePath),
      Optional.of(truststore)
    );
  }

  @Override
  public JsonNode toJson() {
    var node = (ObjectNode) KafkaAuth.super.toJson();
    node.put("privateKey", privateKeyFilePath.toString());
    node.put("privateKeyPassword", passwordHelper().encrypt(privateKeyPassword));
    publicCertificateChainFilePath.ifPresent(path -> node.put("publicCertificateChain", path.toString()));
    truststore.ifPresent(ts -> node.set("truststore", ts.toJson()));
    return node;
  }

  static KafkaAuth fromJson(JsonNode auth) {
    return new KafkaCertificateAuth(
      Path.of(auth.get("privateKey").textValue()),
      passwordHelper().decrypt(auth.get("privateKeyPassword").textValue()),
      auth.has("publicCertificateChain")
        ? Optional.of(Path.of(auth.get("publicCertificateChain").textValue()))
        : Optional.empty(),
      Optional.ofNullable(auth.get("truststore")).map(Truststore::fromJson)
    );
  }

  @Override
  public AuthType type() {
    return AuthType.SSL_CERTIFICATE;
  }

  @Override
  public void addProperties(Properties props) {
    try {
      props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
      props.setProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
      props.setProperty(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, Files.readString(privateKeyFilePath));
      props.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, privateKeyPassword);
      //    props.setProperty(SslConfigs.SSL_KEY_PASSWORD_CONFIG, privateKeyFilePath.toString());
      publicCertificateChainFilePath.ifPresent(path -> {
        try {
          props.setProperty(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, Files.readString(path));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
      //    props.setProperty(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, publicCertificateChainFilePath.toString());
      truststore.ifPresent(value -> value.addProperties(props));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
