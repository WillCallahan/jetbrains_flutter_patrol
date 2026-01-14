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

  private String target = "";
  private String cliArgs = "";
  private String workingDir = "";
  private String cliPath = "";
  private boolean diagnosticMode = false;
  private PatrolCommandMode commandMode = PatrolCommandMode.TEST;
  private EnvironmentVariablesData envData = EnvironmentVariablesData.DEFAULT;

  protected PatrolRunConfiguration(@NotNull Project project,
                                   @NotNull PatrolRunConfigurationFactory factory,
                                   @NotNull String name) {
    super(project, factory, name);
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
    diagnosticMode = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, FIELD_DIAGNOSTIC, "false"));
    String commandValue = JDOMExternalizerUtil.readField(element, FIELD_COMMAND_MODE, PatrolCommandMode.TEST.name());
    commandMode = PatrolCommandMode.valueOf(commandValue);
    envData = EnvironmentVariablesData.readExternal(element);
  }

  @Override
  public void writeExternal(@NotNull Element element) {
    super.writeExternal(element);
    JDOMExternalizerUtil.writeField(element, FIELD_TARGET, target);
    JDOMExternalizerUtil.writeField(element, FIELD_ARGS, cliArgs);
    JDOMExternalizerUtil.writeField(element, FIELD_WORKING_DIR, workingDir);
    JDOMExternalizerUtil.writeField(element, FIELD_CLI_PATH, cliPath);
    JDOMExternalizerUtil.writeField(element, FIELD_DIAGNOSTIC, Boolean.toString(diagnosticMode));
    JDOMExternalizerUtil.writeField(element, FIELD_COMMAND_MODE, commandMode.name());
    envData.writeExternal(element);
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
}
