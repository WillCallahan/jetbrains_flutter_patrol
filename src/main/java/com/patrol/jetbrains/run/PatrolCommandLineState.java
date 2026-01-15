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
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
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
    commandLine.addParameter(configuration.getTarget());

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
