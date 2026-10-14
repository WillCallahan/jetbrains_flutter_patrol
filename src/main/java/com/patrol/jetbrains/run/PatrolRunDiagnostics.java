package com.patrol.jetbrains.run;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.patrol.jetbrains.PatrolNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

/** Adds actionable context to Patrol CLI failures without interpreting app-specific test output. */
final class PatrolRunDiagnostics implements ProcessListener {
  private static final int MAX_OUTPUT_CHARS = 64 * 1024;
  private static final Pattern RESULT_BUNDLE = Pattern.compile("Report:\\s*(.+?\\.xcresult)");
  private static final Pattern IOS_SIMULATOR_ID = Pattern.compile("[0-9A-Fa-f]{8}(?:-[0-9A-Fa-f]{4}){3}-[0-9A-Fa-f]{12}");

  private final Project project;
  private final StringBuilder output = new StringBuilder();

  PatrolRunDiagnostics(@NotNull Project project) {
    this.project = project;
  }

  static void warnAboutMissingRunnerUiTests(@NotNull Project project,
                                             @Nullable String deviceId,
                                             @Nullable File workingDirectory,
                                             @NotNull List<String> parameters) {
    if (!isIosSimulator(deviceId) || workingDirectory == null) {
      return;
    }
    String schemeName = optionValue(parameters, "--flavor", "Runner");
    if (schemeName.isBlank() || schemeName.contains("/") || schemeName.contains("\\")) {
      return;
    }
    Path ios = workingDirectory.toPath().resolve("ios");
    Path scheme = ios.resolve("Runner.xcodeproj/xcshareddata/xcschemes")
        .resolve(schemeName + ".xcscheme");
    if (!Files.isRegularFile(scheme)) {
      scheme = ios.resolve("Runner.xcworkspace/xcshareddata/xcschemes")
          .resolve(schemeName + ".xcscheme");
    }
    if (Files.isRegularFile(scheme) && Boolean.FALSE.equals(hasRunnerUiTestsTestable(scheme))) {
      PatrolNotifications.warn(project,
          "The iOS Xcode scheme '" + schemeName + "' omits RunnerUITests from its Test action. "
              + "Add RunnerUITests to Testables before running Patrol.");
    }
  }

  static void warnAboutOccupiedSimulatorPort(@NotNull Project project,
                                             @Nullable String deviceId,
                                             @NotNull List<String> parameters) {
    if (!isIosSimulator(deviceId)) {
      return;
    }
    int testPort = portOption(parameters, "--test-server-port", 8081);
    int appPort = portOption(parameters, "--app-server-port", 8082);
    if (testPort < 1 || testPort > 65535 || appPort < 1 || appPort > 65535) {
      return; // Let Patrol report invalid options.
    }
    boolean testPortAvailable = isLoopbackPortAvailable(testPort);
    boolean appPortAvailable = isLoopbackPortAvailable(appPort);
    if (testPortAvailable && appPortAvailable) {
      return;
    }
    int[] suggested = freePortPair();
    String suffix = suggested == null ? "Choose two available ports in Modify options." :
        "Try Test server port " + suggested[0] + " and App server port " + suggested[1] + " in Modify options.";
    PatrolNotifications.warn(project,
        "A local service is using Patrol port " + (!testPortAvailable ? testPort : appPort)
            + ". iOS simulator traffic may reach that service instead of Patrol. " + suffix);
  }

  @Override
  public synchronized void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
    if (!ProcessOutputTypes.STDOUT.equals(outputType) && !ProcessOutputTypes.STDERR.equals(outputType)) {
      return;
    }
    output.append(event.getText());
    if (output.length() > MAX_OUTPUT_CHARS) {
      output.delete(0, output.length() - MAX_OUTPUT_CHARS);
    }
  }

  @Override
  public void processTerminated(@NotNull ProcessEvent event) {
    if (event.getExitCode() == 0) {
      return;
    }
    String captured;
    synchronized (this) {
      captured = output.toString();
    }
    if (!SystemInfo.isMac || !captured.contains("xcodebuild exited with code")) {
      return;
    }
    String report = reportPathFrom(captured);
    if (report == null) {
      return;
    }
    String resultBundle = report;
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      String details = readResultBundle(resultBundle);
      String guidance = guidanceFor(details);
      if (guidance != null && !project.isDisposed()) {
        PatrolNotifications.warn(project, guidance);
      }
    });
  }

  static @Nullable String guidanceFor(@Nullable String result) {
    if (result == null) {
      return null;
    }
    if (result.contains("Missing test product") || result.contains("TestBundlePath should be provided")) {
      return "Xcode did not build Patrol's RunnerUITests bundle. Add RunnerUITests to the Test action "
          + "of the iOS Xcode scheme, then rerun Patrol.";
    }
    if (result.contains("com.apple.CoreSimulator.host_support")) {
      return "CoreSimulator could not launch the iOS test runner. Restart the selected simulator "
          + "or try another simulator, then rerun Patrol.";
    }
    return null;
  }

  static @Nullable String reportPathFrom(@NotNull String output) {
    Matcher matcher = RESULT_BUNDLE.matcher(output);
    String report = null;
    while (matcher.find()) {
      report = matcher.group(1).trim();
    }
    return report;
  }

  static @Nullable Boolean hasRunnerUiTestsTestable(@NotNull Path scheme) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      Element root = factory.newDocumentBuilder().parse(scheme.toFile()).getDocumentElement();
      NodeList testActions = root.getElementsByTagName("TestAction");
      if (testActions.getLength() == 0) {
        return null;
      }
      Element testAction = (Element) testActions.item(0);
      if (testAction.getElementsByTagName("TestPlans").getLength() > 0) {
        return null; // Test plans name their testables outside the scheme.
      }
      NodeList testables = testAction.getElementsByTagName("Testables");
      if (testables.getLength() == 0) {
        return null;
      }
      NodeList references = ((Element) testables.item(0)).getElementsByTagName("BuildableReference");
      for (int i = 0; i < references.getLength(); i++) {
        Element reference = (Element) references.item(i);
        Element testable = (Element) reference.getParentNode();
        if ("RunnerUITests".equals(reference.getAttribute("BlueprintName"))
            && !"YES".equalsIgnoreCase(testable.getAttribute("skipped"))) {
          return true;
        }
      }
      return false;
    } catch (Exception ignored) {
      return null; // Let Xcode describe invalid or unsupported scheme files.
    }
  }

  private static boolean isIosSimulator(@Nullable String deviceId) {
    return SystemInfo.isMac && deviceId != null && IOS_SIMULATOR_ID.matcher(deviceId).matches();
  }

  private static @NotNull String optionValue(@NotNull List<String> parameters,
                                             @NotNull String option,
                                             @NotNull String fallback) {
    String value = fallback;
    for (int i = 0; i < parameters.size(); i++) {
      String parameter = parameters.get(i);
      if (parameter.equals(option) && i + 1 < parameters.size()) {
        value = parameters.get(++i);
      } else if (parameter.startsWith(option + "=")) {
        value = parameter.substring(option.length() + 1);
      }
    }
    return value;
  }

  private static int portOption(@NotNull List<String> parameters, @NotNull String option, int fallback) {
    try {
      return Integer.parseInt(optionValue(parameters, option, Integer.toString(fallback)));
    } catch (NumberFormatException ignored) {
      return -1;
    }
  }

  private static boolean isLoopbackPortAvailable(int port) {
    try (ServerSocket socket = new ServerSocket()) {
      socket.bind(new InetSocketAddress("127.0.0.1", port));
      return true;
    } catch (IOException ignored) {
      return false;
    }
  }

  private static @Nullable int[] freePortPair() {
    for (int port = 8083; port < 10000; port += 2) {
      if (isLoopbackPortAvailable(port) && isLoopbackPortAvailable(port + 1)) {
        return new int[]{port, port + 1};
      }
    }
    return null;
  }

  private static @Nullable String readResultBundle(@NotNull String path) {
    Path outputFile = null;
    Process process = null;
    try {
      Path report = Path.of(path);
      if (!Files.isDirectory(report)) {
        return null;
      }
      outputFile = Files.createTempFile("patrol-xcresult-", ".json");
      process = new ProcessBuilder("/usr/bin/xcrun", "xcresulttool", "get", "object", "--legacy",
          "--path", report.toString(), "--format", "json")
          .redirectErrorStream(true)
          .redirectOutput(outputFile.toFile())
          .start();
      if (!process.waitFor(10, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        return null;
      }
      if (process.exitValue() != 0) {
        return null;
      }
      return Files.readString(outputFile);
    } catch (IOException | RuntimeException e) {
      return null;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } finally {
      if (process != null && process.isAlive()) {
        process.destroyForcibly();
      }
      if (outputFile != null) {
        try {
          Files.deleteIfExists(outputFile);
        } catch (IOException ignored) {
          // Diagnostic temp files are nonessential.
        }
      }
    }
  }
}
