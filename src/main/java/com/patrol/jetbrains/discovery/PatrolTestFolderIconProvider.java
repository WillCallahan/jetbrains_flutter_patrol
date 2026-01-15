package com.patrol.jetbrains.discovery;

import com.intellij.icons.AllIcons;
import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.patrol.jetbrains.settings.PubspecUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public final class PatrolTestFolderIconProvider implements FileIconProvider {
  @Override
  public @Nullable Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
    if (project == null || !file.isDirectory()) {
      return null;
    }
    if (!PubspecUtil.hasPatrolDependency(project)) {
      return null;
    }
    if (!"patrol_test".equals(file.getName())) {
      return null;
    }
    return AllIcons.Modules.TestRoot;
  }
}
