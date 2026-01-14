package com.patrol.jetbrains.cli;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

public final class PatrolCliVersionChecker {
  private static final Logger LOG = Logger.getInstance(PatrolCliVersionChecker.class);
  private static final Duration VERSION_TIMEOUT = Duration.ofSeconds(5);

  private PatrolCliVersionChecker() {
  }

  public static Optional<SemanticVersion> getVersion(@NotNull String cliPath) {
    GeneralCommandLine commandLine = new GeneralCommandLine(cliPath, "--version")
        .withCharset(StandardCharsets.UTF_8);
    try {
      CapturingProcessHandler handler = new CapturingProcessHandler(commandLine);
      ProcessOutput output = handler.runProcess((int) VERSION_TIMEOUT.toMillis());
      if (output.getExitCode() != 0) {
        LOG.warn("Failed to read Patrol CLI version: " + output.getStderr());
        return Optional.empty();
      }
      return SemanticVersion.parse(output.getStdout());
    } catch (ExecutionException e) {
      LOG.warn("Failed to invoke Patrol CLI for version", e);
      return Optional.empty();
    }
  }

  public static boolean isSupported(@NotNull SemanticVersion version) {
    return version.isAtLeast(2, 0, 0);
  }
}
