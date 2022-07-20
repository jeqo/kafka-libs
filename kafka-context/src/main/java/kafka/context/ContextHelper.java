package kafka.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ContextHelper {

  static final ObjectMapper json = new ObjectMapper();

  public static Path baseDir() throws IOException {
    final var homePath = System.getProperty("user.home");
    if (homePath.isBlank()) {
      throw new IllegalStateException("Can't find user's home. ${HOME} is empty");
    }

    final var home = Path.of(homePath, ".kafka");
    if (!Files.isDirectory(home)) {
      System.err.println("Kafka Context directory doesn't exist, creating one...");
      Files.createDirectories(home);
    }
    return home;
  }

  public static Path contextPath(Path home, String filename) throws IOException {
    final var context = home.resolve(filename);
    if (!Files.isRegularFile(context)) {
      System.err.println(
        "Kafka Content configuration file doesn't exist, creating one..."
      );
      Files.write(context, emptyContext());
    }

    return context;
  }

  public static <C extends Context> Map<String, C> from(
    Path contextPath,
    Function<JsonNode, C> from
  ) throws IOException {
    final var tree = json.readTree(Files.readAllBytes(contextPath));
    if (!tree.isArray()) {
      throw new IllegalArgumentException("JSON is not an array");
    }

    final var array = (ArrayNode) tree;
    final var contexts = new HashMap<String, C>(array.size());
    for (final var node : array) {
      final var context = from.apply(node);
      contexts.put(context.name(), context);
    }

    return contexts;
  }

  public static byte[] emptyContext() {
    return "[]".getBytes(StandardCharsets.UTF_8);
  }

  public static PasswordHelper passwordHelper() {
    try {
      final var saltPath = baseDir().resolve(".salt");
      if (!Files.exists(saltPath)) {
        final var salt = PasswordHelper.generateKey();
        Files.writeString(saltPath, salt);
        return new PasswordHelper(salt);
      } else {
        final var salt = Files.readString(saltPath);
        return new PasswordHelper(salt);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Password helper not loading", e);
    }
  }
}
