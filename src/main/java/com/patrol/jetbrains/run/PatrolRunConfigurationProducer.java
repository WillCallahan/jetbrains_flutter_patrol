package com.patrol.jetbrains.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.patrol.jetbrains.discovery.PatrolTestUtil;
import org.jetbrains.annotations.NotNull;

public final class PatrolRunConfigurationProducer extends RunConfigurationProducer<PatrolRunConfiguration> {
  public PatrolRunConfigurationProducer() {
    super(PatrolRunConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(@NotNull PatrolRunConfiguration configuration,
                                                  @NotNull ConfigurationContext context,
                                                  @NotNull Ref<PsiElement> sourceElement) {
    PsiFile file = context.getPsiLocation() == null ? null : context.getPsiLocation().getContainingFile();
    if (file == null || !PatrolTestUtil.isPatrolTestFile(file)) {
      return false;
    }

    VirtualFile vFile = file.getVirtualFile();
    if (vFile == null) {
      return false;
    }

    configuration.setTarget(vFile.getPath());
    configuration.setWorkingDir(PatrolTestUtil.defaultWorkingDir(context.getProject()));
    configuration.setName("Patrol: " + vFile.getName());
    sourceElement.set(file);
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(@NotNull PatrolRunConfiguration configuration,
                                            @NotNull ConfigurationContext context) {
    PsiFile file = context.getPsiLocation() == null ? null : context.getPsiLocation().getContainingFile();
    if (file == null || !PatrolTestUtil.isPatrolTestFile(file)) {
      return false;
    }
    VirtualFile vFile = file.getVirtualFile();
    if (vFile == null) {
      return false;
    }

    return vFile.getPath().equals(configuration.getTarget());
  }
}
