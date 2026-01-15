package com.patrol.jetbrains.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
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

  public static boolean hasPatrolDependency(@NotNull Project project) {
    Path pubspec = resolvePubspecPath(project);
    if (pubspec == null || !Files.isRegularFile(pubspec)) {
      return false;
    }

    try {
      String content = Files.readString(pubspec);
      return hasPatrolDependency(content);
    } catch (IOException e) {
      return false;
    }
  }

  public static @Nullable String validatePatrolTestDirectory(@NotNull Project project) {
    String pathValue = resolvePatrolTestRoot(project);
    if (StringUtil.isEmptyOrSpaces(pathValue)) {
      return null;
    }
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

  public static @NotNull String resolvePatrolTestRoot(@NotNull Project project) {
    Optional<String> value = readPatrolTestDirectory(project);
    if (value.isPresent() && !StringUtil.isEmptyOrSpaces(value.get())) {
      return value.get().trim();
    }
    String fallback = PatrolAppSettingsState.getInstance().defaultTestRoot;
    return StringUtil.isEmptyOrSpaces(fallback) ? "patrol_test" : fallback.trim();
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

  private static boolean hasPatrolDependency(@NotNull String content) {
    String[] lines = content.split("\n");
    boolean inDeps = false;
    int depsIndent = -1;

    for (String line : lines) {
      String trimmedLine = line.replaceFirst("#.*$", "").trim();
      if (trimmedLine.isEmpty()) {
        continue;
      }

      int indent = countIndent(line);
      if (!inDeps) {
        if (trimmedLine.equals("dependencies:") || trimmedLine.equals("dev_dependencies:")) {
          inDeps = true;
          depsIndent = indent;
        }
        continue;
      }

      if (indent <= depsIndent) {
        inDeps = false;
        depsIndent = -1;
        continue;
      }

      if (trimmedLine.startsWith("patrol:")) {
        return true;
      }
    }

    return false;
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
