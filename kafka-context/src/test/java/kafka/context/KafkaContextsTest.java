package kafka.context;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class KafkaContextsTest {

  @Test
  void shouldLoadDefault() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = KafkaContexts.load(tmpDir);
    final var ctx = ctxs.getDefault();
    assertThat(ctx.cluster()).isEqualTo(KafkaContexts.CONTEXT_DEFAULT);
  }

  @Test
  void shouldLoadDefaultByName() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = KafkaContexts.load(tmpDir);
    final var ctx = ctxs.get("default");
    assertThat(ctx.cluster()).isEqualTo(KafkaContexts.CONTEXT_DEFAULT);
  }

  @Test
  void shouldGetNullWhenNotExists() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = KafkaContexts.load(tmpDir);
    final var ctx = ctxs.get("NON_EXISTING");
    assertThat(ctx).isNull();
  }

  @Test
  void shouldSaveAndLoadExisting() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = KafkaContexts.load(tmpDir);
    ctxs.add(
      new KafkaContext("local", new KafkaCluster("http://local:8081", new KafkaNoAuth()))
    );
    ctxs.save(tmpDir);

    final var ctxs2 = KafkaContexts.load(tmpDir);
    final var ctx = ctxs2.get("local");
    assertThat(ctx).isNotNull();
  }

  @Test
  void shouldRenameSaveAndLoadExisting() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = KafkaContexts.load(tmpDir);
    ctxs.add(
      new KafkaContext("local", new KafkaCluster("http://local:8081", new KafkaNoAuth()))
    );
    ctxs.rename("local", "other");
    ctxs.save(tmpDir);

    final var ctxs2 = KafkaContexts.load(tmpDir);
    final var ctx = ctxs2.get("other");
    assertThat(ctx).isNotNull();
  }

  @Test
  void shouldGenerateSslCertificateAuth() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = KafkaContexts.load(tmpDir);
    ctxs.add(
      new KafkaContext(
        "local",
        new KafkaCluster(
          "http://local:8081",
          new KafkaCertificateAuth(
            Path.of("key.pem"),
            Path.of("cert.pem"),
            Optional.empty()
          )
        )
      )
    );
    System.out.println(ctxs.get("local").properties());
  }
}
