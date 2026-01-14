package com.patrol.jetbrains.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class PatrolRunConfigurationFactory extends ConfigurationFactory {
  protected PatrolRunConfigurationFactory(@NotNull PatrolRunConfigurationType type) {
    super(type);
  }

  @Override
  public @NotNull String getId() {
    return PatrolRunConfigurationType.ID;
  }

  @Override
  public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
    return new PatrolRunConfiguration(project, this, "Patrol Test");
  }
}
