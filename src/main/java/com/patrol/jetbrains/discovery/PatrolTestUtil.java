package com.patrol.jetbrains.discovery;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.patrol.jetbrains.settings.PatrolAppSettingsState;
import com.patrol.jetbrains.settings.PubspecUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class PatrolTestUtil {
  private PatrolTestUtil() {
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
    String marker = normalizeRoot(getPatrolTestRoot(project));
    return marker != null && normalized.contains(marker);
  }

  public static @Nullable String defaultWorkingDir(@NotNull Project project) {
    return project.getBasePath();
  }

  public static @NotNull String getPatrolTestRoot(@NotNull Project project) {
    Optional<String> pubspec = PubspecUtil.readPatrolTestDirectory(project);
    if (pubspec.isPresent() && !StringUtil.isEmptyOrSpaces(pubspec.get())) {
      return pubspec.get().trim();
    }
    String fallback = PatrolAppSettingsState.getInstance().defaultTestRoot;
    return StringUtil.isEmptyOrSpaces(fallback) ? "integration_test" : fallback.trim();
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
