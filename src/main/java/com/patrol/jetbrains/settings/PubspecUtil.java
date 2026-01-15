package com.patrol.jetbrains.settings;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class PubspecUtil {
  private PubspecUtil() {
  }

  public static Optional<String> readPatrolTestDirectory(@NotNull Project project) {
    Path pubspec = resolvePubspecPath(project);
    if (pubspec == null || !Files.isRegularFile(pubspec)) {
      return Optional.empty();
    }

    try {
      String content = Files.readString(pubspec);
      return parsePatrolTestDirectory(content);
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public static @Nullable String validatePatrolTestDirectory(@NotNull Project project) {
    Optional<String> value = readPatrolTestDirectory(project);
    if (value.isEmpty()) {
      return "patrol.test_directory is missing in pubspec.yaml";
    }
    String pathValue = value.get();
    Path base = resolveProjectBase(project);
    if (base == null) {
      return null;
    }
    Path resolved = base.resolve(pathValue).normalize();
    if (!Files.exists(resolved)) {
      return "patrol.test_directory points to a missing path: " + pathValue;
    }
    if (!Files.isDirectory(resolved)) {
      return "patrol.test_directory is not a directory: " + pathValue;
    }
    return null;
  }

  private static Optional<String> parsePatrolTestDirectory(@NotNull String content) {
    String[] lines = content.split("\n");
    boolean inPatrol = false;
    int patrolIndent = -1;

    for (String line : lines) {
      String trimmedLine = line.replaceFirst("#.*$", "").trim();
      if (trimmedLine.isEmpty()) {
        continue;
      }

      int indent = countIndent(line);
      if (!inPatrol) {
        if (trimmedLine.equals("patrol:")) {
          inPatrol = true;
          patrolIndent = indent;
        }
        continue;
      }

      if (indent <= patrolIndent) {
        inPatrol = false;
        patrolIndent = -1;
        continue;
      }

      if (trimmedLine.startsWith("test_directory:")) {
        String value = trimmedLine.substring("test_directory:".length()).trim();
        return Optional.of(unquote(value));
      }
    }

    return Optional.empty();
  }

  private static String unquote(@NotNull String value) {
    if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
      return value.substring(1, value.length() - 1).trim();
    }
    return value;
  }

  private static int countIndent(@NotNull String line) {
    int count = 0;
    while (count < line.length() && Character.isWhitespace(line.charAt(count))) {
      count++;
    }
    return count;
  }

  private static @Nullable Path resolvePubspecPath(@NotNull Project project) {
    Path base = resolveProjectBase(project);
    if (base == null) {
      return null;
    }
    return base.resolve("pubspec.yaml");
  }

  private static @Nullable Path resolveProjectBase(@NotNull Project project) {
    String basePath = project.getBasePath();
    if (basePath == null || basePath.isEmpty()) {
      return null;
    }
    return Path.of(basePath);
  }
}
