package com.patrol.jetbrains.run;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class PatrolRunConfigurationType extends ConfigurationTypeBase {
  public static final String ID = "PATROL_RUN_CONFIGURATION";
  private static final Icon PATROL_ICON = IconLoader.getIcon("/icons/patrol.svg", PatrolRunConfigurationType.class);
  private static final Icon SMALL_PATROL_ICON = IconUtil.scale(PATROL_ICON, null, 0.3f);

  public PatrolRunConfigurationType() {
    super(ID, "Patrol Test", "Run Patrol Flutter tests", SMALL_PATROL_ICON);
    addFactory(new PatrolRunConfigurationFactory(this, "Patrol Test", PatrolCommandMode.TEST));
    addFactory(new PatrolRunConfigurationFactory(this, "Patrol Develop", PatrolCommandMode.DEVELOP));
  }

  @NotNull
  public static PatrolRunConfigurationType getInstance() {
    return ConfigurationTypeBase.CONFIGURATION_TYPE_EP.findExtension(PatrolRunConfigurationType.class);
  }
}
