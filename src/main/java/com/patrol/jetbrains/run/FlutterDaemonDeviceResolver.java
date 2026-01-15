package com.patrol.jetbrains.run;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlutterDaemonDeviceResolver {
  private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

  private FlutterDaemonDeviceResolver() {
  }

  public static @NotNull String resolveDeviceIdByName(@NotNull String deviceName) {
    Process process = null;
    try {
      process = new ProcessBuilder("flutter", "daemon")
          .redirectErrorStream(true)
          .start();

      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
           BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        writer.write("{\"id\":1,\"method\":\"device.enable\"}\n");
        writer.flush();

        long deadline = System.currentTimeMillis() + 1200;
        while (System.currentTimeMillis() < deadline) {
          if (reader.ready()) {
            String line = reader.readLine();
            if (line == null) {
              break;
            }
            String id = parseDeviceAdded(line, deviceName);
            if (!id.isEmpty()) {
              return id;
            }
          } else {
            try {
              Thread.sleep(50);
            } catch (InterruptedException ignored) {
              Thread.currentThread().interrupt();
              break;
            }
          }
        }
      }
    } catch (IOException ignored) {
      return "";
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
    return "";
  }

  private static String parseDeviceAdded(@NotNull String line, @NotNull String expectedName) {
    if (!line.contains("\"event\":\"device.added\"")) {
      return "";
    }
    String name = match(line, NAME_PATTERN);
    if (name == null || !name.equalsIgnoreCase(expectedName.trim())) {
      return "";
    }
    String id = match(line, ID_PATTERN);
    return id == null ? "" : id;
  }

  private static String match(@NotNull String line, @NotNull Pattern pattern) {
    Matcher matcher = pattern.matcher(line);
    if (!matcher.find()) {
      return null;
    }
    return matcher.group(1);
  }
}
