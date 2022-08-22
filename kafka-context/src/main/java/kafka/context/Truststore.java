package kafka.context;

import static kafka.context.ContextHelper.checkFileIsAccessible;
import static kafka.context.ContextHelper.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.common.config.SslConfigs;

public record Truststore(Optional<Keystore> truststore, Optional<Path> certificates) {
  public static Truststore build(Keystore truststore) {
    return new Truststore(Optional.of(truststore), Optional.empty());
  }

  public static Truststore build(Path certificatesPath) {
    checkFileIsAccessible(certificatesPath);
    return new Truststore(Optional.empty(), Optional.of(certificatesPath.toAbsolutePath()));
  }

  public static Truststore fromJson(JsonNode node) {
    if (node.has("certificates")) return build(Path.of(node.get("certificates").textValue())); else return build(
      Keystore.parseJson(node)
    );
  }

  public JsonNode toJson() {
    if (certificates.isPresent()) return json.createObjectNode().put("certificates", certificates.get().toString());
    if (truststore.isPresent()) return truststore.get().toJson();
    return json.createObjectNode();
  }

  public void addProperties(Properties props) {
    truststore.ifPresent(keystore -> {
      props.setProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, keystore.type());
      props.setProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, keystore.location().toString());
      props.setProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, keystore.password());
    });
    certificates.ifPresent(cert -> {
      props.setProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
      try {
        props.setProperty(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, Files.readString(cert).trim());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
