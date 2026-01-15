package com.patrol.jetbrains.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class PatrolRunConfigurationFactory extends ConfigurationFactory {
  private final String factoryName;
  private final PatrolCommandMode defaultMode;

  protected PatrolRunConfigurationFactory(@NotNull PatrolRunConfigurationType type,
                                          @NotNull String factoryName,
                                          @NotNull PatrolCommandMode defaultMode) {
    super(type);
    this.factoryName = factoryName;
    this.defaultMode = defaultMode;
  }

  @Override
  public @NotNull String getId() {
    return PatrolRunConfigurationType.ID + "." + defaultMode.name().toLowerCase();
  }

  @Override
  public @NotNull String getName() {
    return factoryName;
  }

  @Override
  public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
    PatrolRunConfiguration configuration = new PatrolRunConfiguration(project, this, factoryName);
    configuration.setCommandMode(defaultMode);
    return configuration;
  }
}
