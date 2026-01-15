package com.patrol.jetbrains.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configuration.EnvironmentVariablesData;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.execution.ParametersListUtil;
import com.patrol.jetbrains.DefaultPatrolCliLocator;
import com.patrol.jetbrains.PatrolNotifications;
import com.patrol.jetbrains.cli.PatrolCliVersionChecker;
import com.patrol.jetbrains.cli.SemanticVersion;
import com.patrol.jetbrains.settings.PatrolAppSettingsState;
import com.patrol.jetbrains.settings.PatrolProjectSettingsState;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PatrolCommandLineState extends CommandLineState {
  private static final Logger LOG = Logger.getInstance(PatrolCommandLineState.class);
  private final PatrolRunConfiguration configuration;

  protected PatrolCommandLineState(@NotNull ExecutionEnvironment environment,
                                   @NotNull PatrolRunConfiguration configuration) {
    super(environment);
    this.configuration = configuration;
  }

  @Override
  protected @NotNull ProcessHandler startProcess() throws ExecutionException {
    Path cliPath = resolveCliPath();
    checkCliVersion(cliPath.toString());

    com.intellij.execution.configurations.GeneralCommandLine commandLine =
        new com.intellij.execution.configurations.GeneralCommandLine(cliPath.toString());
    if (configuration.getCommandMode() == PatrolCommandMode.DEVELOP) {
      commandLine.addParameter("develop");
    } else {
      commandLine.addParameter("test");
    }
    String device = resolveDeviceTarget();
    if (!StringUtil.isEmptyOrSpaces(device)) {
      commandLine.addParameter("--device");
      commandLine.addParameter(device);
    }
    commandLine.addParameter("--target");
    commandLine.addParameter(configuration.getTarget());
    addOptionParameters(commandLine);

    List<String> extraArgs = ParametersListUtil.parse(configuration.getCliArgs());
    commandLine.addParameters(extraArgs);

    if (!StringUtil.isEmptyOrSpaces(configuration.getWorkingDir())) {
      commandLine.setWorkDirectory(configuration.getWorkingDir());
    }

    EnvironmentVariablesData envData = configuration.getEnvData();
    commandLine.getEnvironment().putAll(envData.getEnvs());
    if (!envData.isPassParentEnvs()) {
      commandLine.withParentEnvironmentType(com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType.NONE);
    }

    KillableProcessHandler handler = new KillableProcessHandler(commandLine);
    if (configuration.isDiagnosticMode()) {
      attachDiagnosticLogging(handler, commandLine);
    }
    ProcessTerminatedListener.attach(handler);
    return handler;
  }

  private Path resolveCliPath() throws ExecutionException {
    Path overridePath = null;
    if (!StringUtil.isEmptyOrSpaces(configuration.getCliPath())) {
      overridePath = Path.of(configuration.getCliPath());
    } else {
      PatrolProjectSettingsState projectSettings = PatrolProjectSettingsState.getInstance(configuration.getProject());
      String projectPath = projectSettings.useProjectCliPath ? projectSettings.projectCliPath : "";
      if (!StringUtil.isEmptyOrSpaces(projectPath)) {
        overridePath = Path.of(projectPath);
      } else {
        String defaultPath = PatrolAppSettingsState.getInstance().defaultCliPath;
        if (!StringUtil.isEmptyOrSpaces(defaultPath)) {
          overridePath = Path.of(defaultPath);
        }
      }
    }

    Optional<Path> resolved = new DefaultPatrolCliLocator(overridePath).findPatrolCli();
    if (resolved.isEmpty()) {
      throw new ExecutionException("Patrol CLI not found. Set the CLI path or ensure it is on PATH.");
    }

    return resolved.get();
  }

  private void checkCliVersion(@NotNull String cliPath) {
    PatrolCliVersionChecker.getVersion(cliPath).ifPresent(version -> {
      if (!PatrolCliVersionChecker.isSupported(version)) {
        PatrolNotifications.warn(getEnvironment().getProject(),
            "Patrol CLI " + version + " is not supported. Please upgrade to 2.0+.");
      } else if (configuration.isDiagnosticMode()) {
        LOG.info("Detected Patrol CLI version " + version);
      }
    });
  }

  private String resolveDeviceTarget() {
    String configured = configuration.getDevice();
    com.intellij.execution.ExecutionTarget target = getEnvironment().getExecutionTarget();
    if (target != null) {
      String id = extractDeviceId(target.getId());
      if (!StringUtil.isEmptyOrSpaces(id) && !"default".equalsIgnoreCase(id)) {
        return id;
      }
      String name = extractDeviceId(target.getDisplayName());
      if (!StringUtil.isEmptyOrSpaces(name)) {
        return name;
      }
    }
    return configured == null ? "" : configured.trim();
  }

  private String extractDeviceId(String raw) {
    if (StringUtil.isEmptyOrSpaces(raw)) {
      return "";
    }
    String value = raw.trim();
    String extracted = extractIdentifier(value);
    if (!StringUtil.isEmptyOrSpaces(extracted)) {
      value = extracted;
    }
    if (value.startsWith("path=")) {
      String pathValue = value.substring("path=".length());
      int slash = Math.max(pathValue.lastIndexOf('/'), pathValue.lastIndexOf('\\'));
      String name = slash >= 0 ? pathValue.substring(slash + 1) : pathValue;
      if (name.endsWith(".avd")) {
        name = name.substring(0, name.length() - ".avd".length());
      }
      return name.trim();
    }
    if (value.contains("DeviceId(")) {
      String inner = extractIdentifier(value);
      if (!StringUtil.isEmptyOrSpaces(inner)) {
        return inner;
      }
    }
    return value;
  }

  private String extractIdentifier(String value) {
    int idx = value.indexOf("identifier=");
    if (idx < 0) {
      return "";
    }
    String tail = value.substring(idx + "identifier=".length());
    int end = tail.indexOf(')');
    if (end < 0) {
      end = tail.indexOf(']');
    }
    if (end < 0) {
      end = tail.indexOf(',');
    }
    String extracted = end >= 0 ? tail.substring(0, end) : tail;
    return extracted.trim();
  }

  private void addOptionParameters(@NotNull com.intellij.execution.configurations.GeneralCommandLine commandLine) {
    PatrolCommandMode mode = configuration.getCommandMode();
    PatrolRunOption buildMode = selectBuildMode(mode);

    if (buildMode != null) {
      applyToggleOption(commandLine, buildMode);
    }

    for (PatrolRunOption option : PatrolRunOption.values()) {
      if (option == PatrolRunOption.DEBUG || option == PatrolRunOption.PROFILE || option == PatrolRunOption.RELEASE) {
        continue;
      }
      if (!option.appliesTo(mode)) {
        continue;
      }
      if (!configuration.isOptionEnabled(option)) {
        continue;
      }
      if (option.getType() == PatrolRunOptionType.TOGGLE) {
        applyToggleOption(commandLine, option);
      } else {
        applyValueOption(commandLine, option);
      }
    }
  }

  private PatrolRunOption selectBuildMode(@NotNull PatrolCommandMode mode) {
    PatrolRunOption[] priority = new PatrolRunOption[]{
        PatrolRunOption.RELEASE,
        PatrolRunOption.PROFILE,
        PatrolRunOption.DEBUG
    };
    for (PatrolRunOption option : priority) {
      if (option.appliesTo(mode) && configuration.isOptionEnabled(option)) {
        return option;
      }
    }
    return null;
  }

  private void applyToggleOption(@NotNull com.intellij.execution.configurations.GeneralCommandLine commandLine,
                                 @NotNull PatrolRunOption option) {
    String value = configuration.getOptionValue(option);
    boolean enabledValue = Boolean.parseBoolean(value);
    if (option.isNegatable()) {
      commandLine.addParameter(enabledValue ? option.getFlag() : option.getNegatedFlag());
    } else if (enabledValue) {
      commandLine.addParameter(option.getFlag());
    }
  }

  private void applyValueOption(@NotNull com.intellij.execution.configurations.GeneralCommandLine commandLine,
                                @NotNull PatrolRunOption option) {
    String rawValue = configuration.getOptionValue(option);
    if (StringUtil.isEmptyOrSpaces(rawValue)) {
      return;
    }
    if (option == PatrolRunOption.DART_DEFINE
        || option == PatrolRunOption.EXCLUDE
        || option == PatrolRunOption.COVERAGE_IGNORE) {
      List<String> values = splitMultiValue(rawValue);
      for (String value : values) {
        commandLine.addParameter(option.getFlag());
        commandLine.addParameter(value);
      }
      return;
    }
    commandLine.addParameter(option.getFlag());
    commandLine.addParameter(rawValue);
  }

  private List<String> splitMultiValue(@NotNull String rawValue) {
    List<String> values = new ArrayList<>();
    for (String part : rawValue.split("[,\n]")) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        values.add(trimmed);
      }
    }
    return values;
  }

  private void attachDiagnosticLogging(@NotNull ProcessHandler handler,
                                       @NotNull com.intellij.execution.configurations.GeneralCommandLine commandLine) {
    LOG.info("Patrol CLI: " + commandLine.getExePath());
    LOG.info("Patrol working dir: " + commandLine.getWorkDirectory());
    LOG.info("Patrol args: " + String.join(" ", commandLine.getParametersList().getParameters()));
    handler.addProcessListener(new com.intellij.execution.process.ProcessAdapter() {
      @Override
      public void onTextAvailable(com.intellij.execution.process.ProcessEvent event,
                                  com.intellij.openapi.util.Key outputType) {
        if (ProcessOutputTypes.STDERR.equals(outputType)) {
          LOG.warn(event.getText());
        } else {
          LOG.info(event.getText());
        }
      }

      @Override
      public void processTerminated(com.intellij.execution.process.ProcessEvent event) {
        LOG.info("Patrol process exited with code " + event.getExitCode());
      }
    });
  }
}
