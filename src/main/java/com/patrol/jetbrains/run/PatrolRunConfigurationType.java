package com.patrol.jetbrains.run;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

public final class PatrolRunConfigurationType extends ConfigurationTypeBase {
  public static final String ID = "PATROL_RUN_CONFIGURATION";

  public PatrolRunConfigurationType() {
    super(ID, "Patrol Test", "Run Patrol Flutter tests", AllIcons.RunConfigurations.Junit);
    addFactory(new PatrolRunConfigurationFactory(this));
  }

  @NotNull
  public static PatrolRunConfigurationType getInstance() {
    return ConfigurationTypeBase.CONFIGURATION_TYPE_EP.findExtension(PatrolRunConfigurationType.class);
  }
}
