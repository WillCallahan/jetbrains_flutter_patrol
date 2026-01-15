package com.patrol.jetbrains.run;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class PatrolRunConfigurationType extends ConfigurationTypeBase {
  public static final String ID = "PATROL_RUN_CONFIGURATION";
  private static final Icon PATROL_ICON = IconLoader.getIcon("/icons/patrol.svg", PatrolRunConfigurationType.class);

  public PatrolRunConfigurationType() {
    super(ID, "Patrol Test", "Run Patrol Flutter tests", PATROL_ICON);
    addFactory(new PatrolRunConfigurationFactory(this, "Patrol Test", PatrolCommandMode.TEST));
    addFactory(new PatrolRunConfigurationFactory(this, "Patrol Develop", PatrolCommandMode.DEVELOP));
  }

  @NotNull
  public static PatrolRunConfigurationType getInstance() {
    return ConfigurationTypeBase.CONFIGURATION_TYPE_EP.findExtension(PatrolRunConfigurationType.class);
  }
}
