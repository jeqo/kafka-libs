package kafka.context;

import static kafka.context.ContextHelper.checkFileIsAccessible;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.common.config.SslConfigs;

public record Truststore(
  boolean keystoreBased,
  Optional<Keystore> truststore,
  Optional<Path> certificates
) {
  static Truststore build(Keystore truststore) {
    return new Truststore(true, Optional.of(truststore), Optional.empty());
  }

  static Truststore build(Path certificatesPath) {
    checkFileIsAccessible(certificatesPath);
    return new Truststore(
      true,
      Optional.empty(),
      Optional.of(certificatesPath.toAbsolutePath())
    );
  }

  public static Truststore parse(JsonNode node) {
    if (node.has("certificates")) return build(
      Path.of(node.get("certificates").textValue())
    ); else return build(Keystore.parseJson(node));
  }

  public void addProperties(Properties props) {
    truststore.ifPresent(keystore -> {
      props.setProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, keystore.type());
      props.setProperty(
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
        keystore.location().toString()
      );
      props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, keystore.password());
    });
    certificates.ifPresent(cert ->
      props.setProperty(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, cert.toString())
    );
  }
}
