package com.patrol.jetbrains.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PatrolRunConfiguration extends RunConfigurationBase<PatrolRunConfiguration> {
  private static final String FIELD_TARGET = "patrol.target";
  private static final String FIELD_ARGS = "patrol.args";
  private static final String FIELD_WORKING_DIR = "patrol.workingDir";
  private static final String FIELD_CLI_PATH = "patrol.cliPath";
  private static final String FIELD_DIAGNOSTIC = "patrol.diagnostic";
  private static final String FIELD_COMMAND_MODE = "patrol.commandMode";
  private static final String FIELD_DEVICE = "patrol.device";
  private static final String FIELD_OPTION_ENABLED_PREFIX = "patrol.option.enabled.";
  private static final String FIELD_OPTION_VALUE_PREFIX = "patrol.option.value.";

  private String target = "";
  private String cliArgs = "";
  private String workingDir = "";
  private String cliPath = "";
  private String device = "";
  private boolean diagnosticMode = false;
  private PatrolCommandMode commandMode = PatrolCommandMode.TEST;
  private EnvironmentVariablesData envData = EnvironmentVariablesData.DEFAULT;
  private final java.util.EnumMap<PatrolRunOption, Boolean> optionEnabled =
      new java.util.EnumMap<>(PatrolRunOption.class);
  private final java.util.EnumMap<PatrolRunOption, String> optionValues =
      new java.util.EnumMap<>(PatrolRunOption.class);

  protected PatrolRunConfiguration(@NotNull Project project,
                                   @NotNull PatrolRunConfigurationFactory factory,
                                   @NotNull String name) {
    super(project, factory, name);
    setAllowRunningInParallel(false);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    if (target == null || target.trim().isEmpty()) {
      throw new RuntimeConfigurationException("Test target is required.");
    }
  }

  @Override
  public @Nullable RunProfileState getState(@NotNull Executor executor,
                                            @NotNull ExecutionEnvironment environment) throws ExecutionException {
    return new PatrolCommandLineState(environment, this);
  }

  @Override
  public @NotNull PatrolRunConfigurationEditor getConfigurationEditor() {
    return new PatrolRunConfigurationEditor();
  }

  @Override
  public void readExternal(@NotNull Element element) {
    super.readExternal(element);
    target = JDOMExternalizerUtil.readField(element, FIELD_TARGET, "");
    cliArgs = JDOMExternalizerUtil.readField(element, FIELD_ARGS, "");
    workingDir = JDOMExternalizerUtil.readField(element, FIELD_WORKING_DIR, "");
    cliPath = JDOMExternalizerUtil.readField(element, FIELD_CLI_PATH, "");
    device = JDOMExternalizerUtil.readField(element, FIELD_DEVICE, "");
    diagnosticMode = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, FIELD_DIAGNOSTIC, "false"));
    String commandValue = JDOMExternalizerUtil.readField(element, FIELD_COMMAND_MODE, PatrolCommandMode.TEST.name());
    commandMode = PatrolCommandMode.valueOf(commandValue);
    envData = EnvironmentVariablesData.readExternal(element);
    readOptions(element);
  }

  @Override
  public void writeExternal(@NotNull Element element) {
    super.writeExternal(element);
    JDOMExternalizerUtil.writeField(element, FIELD_TARGET, target);
    JDOMExternalizerUtil.writeField(element, FIELD_ARGS, cliArgs);
    JDOMExternalizerUtil.writeField(element, FIELD_WORKING_DIR, workingDir);
    JDOMExternalizerUtil.writeField(element, FIELD_CLI_PATH, cliPath);
    JDOMExternalizerUtil.writeField(element, FIELD_DEVICE, device);
    JDOMExternalizerUtil.writeField(element, FIELD_DIAGNOSTIC, Boolean.toString(diagnosticMode));
    JDOMExternalizerUtil.writeField(element, FIELD_COMMAND_MODE, commandMode.name());
    envData.writeExternal(element);
    writeOptions(element);
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target == null ? "" : target;
  }

  public String getCliArgs() {
    return cliArgs;
  }

  public void setCliArgs(String cliArgs) {
    this.cliArgs = cliArgs == null ? "" : cliArgs;
  }

  public String getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(String workingDir) {
    this.workingDir = workingDir == null ? "" : workingDir;
  }

  public String getCliPath() {
    return cliPath;
  }

  public void setCliPath(String cliPath) {
    this.cliPath = cliPath == null ? "" : cliPath;
  }

  public String getDevice() {
    return device;
  }

  public void setDevice(String device) {
    this.device = device == null ? "" : device;
  }

  public EnvironmentVariablesData getEnvData() {
    return envData;
  }

  public void setEnvData(EnvironmentVariablesData envData) {
    this.envData = envData == null ? EnvironmentVariablesData.DEFAULT : envData;
  }

  public boolean isDiagnosticMode() {
    return diagnosticMode;
  }

  public void setDiagnosticMode(boolean diagnosticMode) {
    this.diagnosticMode = diagnosticMode;
  }

  public PatrolCommandMode getCommandMode() {
    return commandMode;
  }

  public void setCommandMode(PatrolCommandMode commandMode) {
    this.commandMode = commandMode == null ? PatrolCommandMode.TEST : commandMode;
  }

  public boolean isOptionEnabled(@NotNull PatrolRunOption option) {
    return optionEnabled.getOrDefault(option, Boolean.FALSE);
  }

  public void setOptionEnabled(@NotNull PatrolRunOption option, boolean enabled) {
    optionEnabled.put(option, enabled);
  }

  public @NotNull String getOptionValue(@NotNull PatrolRunOption option) {
    String value = optionValues.get(option);
    if (value != null) {
      return value;
    }
    if (option.getType() == PatrolRunOptionType.TOGGLE) {
      Boolean defaultValue = option.getDefaultToggleValue();
      return defaultValue == null ? "" : defaultValue.toString();
    }
    return "";
  }

  public void setOptionValue(@NotNull PatrolRunOption option, @NotNull String value) {
    optionValues.put(option, value);
  }

  private void readOptions(@NotNull Element element) {
    optionEnabled.clear();
    optionValues.clear();
    for (PatrolRunOption option : PatrolRunOption.values()) {
      String enabledValue = JDOMExternalizerUtil.readField(element, FIELD_OPTION_ENABLED_PREFIX + option.getId());
      if (enabledValue != null) {
        optionEnabled.put(option, Boolean.parseBoolean(enabledValue));
      }
      String value = JDOMExternalizerUtil.readField(element, FIELD_OPTION_VALUE_PREFIX + option.getId());
      if (value != null) {
        optionValues.put(option, value);
      }
    }
  }

  private void writeOptions(@NotNull Element element) {
    for (PatrolRunOption option : PatrolRunOption.values()) {
      boolean enabled = optionEnabled.getOrDefault(option, Boolean.FALSE);
      JDOMExternalizerUtil.writeField(element, FIELD_OPTION_ENABLED_PREFIX + option.getId(), Boolean.toString(enabled));
      String value = optionValues.get(option);
      if (value != null && !value.isEmpty()) {
        JDOMExternalizerUtil.writeField(element, FIELD_OPTION_VALUE_PREFIX + option.getId(), value);
      }
    }
  }
}
