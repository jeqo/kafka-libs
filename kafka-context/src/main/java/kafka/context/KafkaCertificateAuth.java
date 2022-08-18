package kafka.context;

import static kafka.context.ContextHelper.checkFileIsAccessible;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.common.config.SslConfigs;

public record KafkaCertificateAuth(
  Path privateKeyFilePath,
  Path publicCertificateChainFilePath,
  Optional<Truststore> truststore
)
  implements KafkaAuth {
  public static KafkaCertificateAuth build(
    Path privateKeyFilePath,
    Path publicCertificateChainFilePath
  ) {
    checkFileIsAccessible(privateKeyFilePath);
    checkFileIsAccessible(publicCertificateChainFilePath);
    return new KafkaCertificateAuth(
      privateKeyFilePath.toAbsolutePath(),
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
      privateKeyFilePath,
      publicCertificateChainFilePath,
      Optional.of(truststore)
    );
  }

  static KafkaAuth fromJson(JsonNode auth) {
    return new KafkaCertificateAuth(
      Path.of(auth.get("privateKey").textValue()),
      Path.of(auth.get("publicCertificateChain").textValue()),
      Optional.ofNullable(auth.get("truststore")).map(Truststore::parse)
    );
  }

  @Override
  public AuthType type() {
    return AuthType.SSL_CERTIFICATE;
  }

  @Override
  public void addProperties(Properties props) {
    props.setProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PEM");
    props.setProperty(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, privateKeyFilePath.toString());
    props.setProperty(
      SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG,
      publicCertificateChainFilePath.toString()
    );
    // TODO add additional SSL configs (TLS version, supported alg, etc.)
  }
}
