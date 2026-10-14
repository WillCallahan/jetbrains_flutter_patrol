package com.patrol.jetbrains.discovery;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.patrol.jetbrains.settings.PubspecUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PatrolTestUtil {
  /** File-name suffix that marks a Patrol test entrypoint. */
  public static final String TEST_FILE_SUFFIX = "patrol_test.dart";

  /** Directories that never contain hand-written tests, so there is no point descending into them. */
  private static final Set<String> SKIPPED_DIRS =
      Set.of("build", ".dart_tool", ".git", ".idea", "node_modules", ".gradle", "ios", "android", "windows", "macos", "linux");

  /** Guard rails for the filesystem scan, which runs on the EDT when a run configuration is opened. */
  private static final int MAX_SCAN_DEPTH = 12;
  private static final int MAX_RESULTS = 500;

  private PatrolTestUtil() {
  }

  /**
   * Finds Patrol test entrypoints under the project, as paths relative to the project root.
   *
   * <p>Scans the configured {@code patrol.test_directory} first, then the conventional
   * {@code integration_test/} and {@code test/} directories. Returns an empty list rather than
   * throwing if the project has no base path or the directories are unreadable.
   */
  public static @NotNull List<String> findTestTargets(@NotNull Project project) {
    String basePath = project.getBasePath();
    if (StringUtil.isEmptyOrSpaces(basePath)) {
      return List.of();
    }
    Path base = Path.of(basePath);

    Set<String> candidates = new LinkedHashSet<>();
    candidates.add(PubspecUtil.resolvePatrolTestRoot(project));
    candidates.add("integration_test");
    candidates.add("test");

    Set<String> found = new LinkedHashSet<>();
    for (String candidate : candidates) {
      if (StringUtil.isEmptyOrSpaces(candidate)) {
        continue;
      }
      Path root = base.resolve(candidate.trim()).normalize();
      if (!Files.isDirectory(root)) {
        continue;
      }
      collectTestFiles(base, root, found);
      if (found.size() >= MAX_RESULTS) {
        break;
      }
    }

    List<String> results = new ArrayList<>(found);
    results.sort(String::compareToIgnoreCase);
    return results;
  }

  private static void collectTestFiles(@NotNull Path base, @NotNull Path root, @NotNull Set<String> sink) {
    try {
      Files.walkFileTree(root, Set.of(), MAX_SCAN_DEPTH, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
          String name = dir.getFileName() == null ? "" : dir.getFileName().toString();
          if (!dir.equals(root) && (name.startsWith(".") || SKIPPED_DIRS.contains(name))) {
            return FileVisitResult.SKIP_SUBTREE;
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          if (sink.size() >= MAX_RESULTS) {
            return FileVisitResult.TERMINATE;
          }
          Path name = file.getFileName();
          if (name != null && name.toString().endsWith(TEST_FILE_SUFFIX)) {
            sink.add(base.relativize(file).toString().replace('\\', '/'));
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
          // An unreadable entry should not abort discovery of the rest.
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException ignored) {
      // Discovery is best effort; the target field stays editable either way.
    }
  }

  public static boolean isPatrolTestFile(@NotNull PsiFile file) {
    VirtualFile vFile = file.getVirtualFile();
    if (vFile == null) {
      return false;
    }
    if (!PubspecUtil.hasPatrolDependency(file.getProject())) {
      return false;
    }
    return isPatrolTestPath(file.getProject(), vFile.getPath());
  }

  public static boolean isPatrolTestPath(@NotNull String path) {
    if (!path.endsWith("patrol_test.dart")) {
      return false;
    }
    String normalized = path.replace('\\', '/');
    return normalized.contains("/integration_test/") || normalized.contains("/test/");
  }

  public static boolean isPatrolTestPath(@NotNull Project project, @NotNull String path) {
    if (!path.endsWith("patrol_test.dart")) {
      return false;
    }
    String normalized = path.replace('\\', '/');
    String marker = normalizeRoot(PubspecUtil.resolvePatrolTestRoot(project));
    return marker != null && normalized.contains(marker);
  }

  public static @Nullable String defaultWorkingDir(@NotNull Project project) {
    return project.getBasePath();
  }

  private static @Nullable String normalizeRoot(@NotNull String root) {
    String normalized = root.replace('\\', '/').trim();
    if (normalized.isEmpty()) {
      return null;
    }
    if (normalized.startsWith("./")) {
      normalized = normalized.substring(2);
    }
    if (normalized.startsWith("/")) {
      normalized = normalized.substring(1);
    }
    if (!normalized.endsWith("/")) {
      normalized = normalized + "/";
    }
    return "/" + normalized;
  }
}
