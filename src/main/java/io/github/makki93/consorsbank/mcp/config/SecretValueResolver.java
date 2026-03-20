package io.github.makki93.consorsbank.mcp.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

final class SecretValueResolver {
  private SecretValueResolver() {
  }

  static String fromFile(String path) {
    try {
      return Files.readString(Path.of(path)).trim();
    } catch (IOException exception) {
      throw new IllegalArgumentException("Failed to read access token file: " + path, exception);
    }
  }

  static String fromCommand(String command) {
    Process process = null;
    try {
      process = new ProcessBuilder("sh", "-c", command)
          .redirectErrorStream(true)
          .start();
      String output = new String(process.getInputStream().readAllBytes()).trim();
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new IllegalArgumentException(
            "Access token command failed with exit code " + exitCode);
      }
      return output;
    } catch (IOException exception) {
      throw new IllegalArgumentException("Failed to start access token command", exception);
    } catch (InterruptedException exception) {
      if (process != null) {
        process.destroyForcibly();
      }
      Thread.currentThread().interrupt();
      throw new IllegalArgumentException("Access token command was interrupted", exception);
    }
  }
}
