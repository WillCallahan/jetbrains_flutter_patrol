package com.patrol.jetbrains.discovery;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PatrolTestUtil {
  private PatrolTestUtil() {
  }

  public static boolean isPatrolTestFile(@NotNull PsiFile file) {
    VirtualFile vFile = file.getVirtualFile();
    if (vFile == null) {
      return false;
    }
    return isPatrolTestPath(vFile.getPath());
  }

  public static boolean isPatrolTestPath(@NotNull String path) {
    if (!path.endsWith("patrol_test.dart")) {
      return false;
    }
    String normalized = path.replace('\\', '/');
    return normalized.contains("/integration_test/") || normalized.contains("/test/");
  }

  public static @Nullable String defaultWorkingDir(@NotNull Project project) {
    return project.getBasePath();
  }
}
