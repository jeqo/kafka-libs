package kafka.context.sr;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

class SchemaRegistryContextsTest {

  @Test
  void shouldLoadDefault() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = SchemaRegistryContexts.load(tmpDir);
    final var ctx = ctxs.getDefault();
    assertThat(ctx.cluster()).isEqualTo(SchemaRegistryContexts.CONTEXT_DEFAULT);
  }

  @Test
  void shouldLoadDefaultByName() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = SchemaRegistryContexts.load(tmpDir);
    final var ctx = ctxs.get("default");
    assertThat(ctx.cluster()).isEqualTo(SchemaRegistryContexts.CONTEXT_DEFAULT);
  }

  @Test
  void shouldGetNullWhenNotExists() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = SchemaRegistryContexts.load(tmpDir);
    final var ctx = ctxs.get("NON_EXISTING");
    assertThat(ctx).isNull();
  }

  @Test
  void shouldSaveAndLoadExisting() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = SchemaRegistryContexts.load(tmpDir);
    ctxs.add(
      new SchemaRegistryContext(
        "local",
        new SchemaRegistryCluster("http://local:8081", new HttpNoAuth())
      )
    );
    ctxs.save(tmpDir);

    final var ctxs2 = SchemaRegistryContexts.load(tmpDir);
    final var ctx = ctxs2.get("local");
    assertThat(ctx).isNotNull();
  }

  @Test
  void shouldRenameSaveAndLoadExisting() throws IOException {
    final var tmpDir = Files.createTempDirectory("kfk-ctx");
    final var ctxs = SchemaRegistryContexts.load(tmpDir);
    ctxs.add(
      new SchemaRegistryContext(
        "local",
        new SchemaRegistryCluster("http://local:8081", new HttpNoAuth())
      )
    );
    ctxs.rename("local", "other");
    ctxs.save(tmpDir);

    final var ctxs2 = SchemaRegistryContexts.load(tmpDir);
    final var ctx = ctxs2.get("other");
    assertThat(ctx).isNotNull();
  }
}
