package com.patrol.jetbrains.run;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.patrol.jetbrains.discovery.PatrolTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PatrolRunLineMarkerContributor extends RunLineMarkerContributor {
  private static final javax.swing.Icon RUN_ICON = AllIcons.RunConfigurations.TestMark;

  @Override
  public @Nullable Info getInfo(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();
    if (file == null || !PatrolTestUtil.isPatrolTestFile(file)) {
      return null;
    }

    if (isTargetElement(element)) {
      return new Info(RUN_ICON, ExecutorAction.getActions(), e -> "Run Patrol Test");
    }

    return null;
  }

  private boolean isTargetElement(@NotNull PsiElement element) {
    if (element instanceof PsiFile) {
      return true;
    }
    String text = element.getText();
    return "patrolTest".equals(text) || "group".equals(text);
  }
}
