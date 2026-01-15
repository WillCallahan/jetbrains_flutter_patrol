package com.patrol.jetbrains.discovery;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.patrol.jetbrains.settings.PatrolAppSettingsState;
import com.patrol.jetbrains.settings.PatrolProjectSettingsState;
import com.patrol.jetbrains.settings.PubspecUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    for (String root : getPatrolTestRoots(project)) {
      String marker = normalizeRoot(root);
      if (marker != null && normalized.contains(marker)) {
        return true;
      }
    }
    return false;
  }

  public static @Nullable String defaultWorkingDir(@NotNull Project project) {
    return project.getBasePath();
  }

  public static @NotNull List<String> getPatrolTestRoots(@NotNull Project project) {
    PatrolProjectSettingsState projectSettings = PatrolProjectSettingsState.getInstance(project);
    List<String> baseRoots = new ArrayList<>();
    if (projectSettings.useProjectTestRoots && !projectSettings.projectTestRoots.isEmpty()) {
      baseRoots.addAll(projectSettings.projectTestRoots);
    } else {
      baseRoots.addAll(PatrolAppSettingsState.getInstance().testRoots);
    }

    if (projectSettings.includePubspecTestDirectory) {
      Optional<String> pubspec = PubspecUtil.readPatrolTestDirectory(project);
      pubspec.ifPresent(baseRoots::add);
    }

    Set<String> unique = new LinkedHashSet<>();
    for (String root : baseRoots) {
      if (StringUtil.isEmptyOrSpaces(root)) {
        continue;
      }
      unique.add(root.trim());
    }
    return new ArrayList<>(unique);
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
