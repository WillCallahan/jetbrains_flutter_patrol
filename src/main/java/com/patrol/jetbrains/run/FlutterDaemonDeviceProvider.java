package com.patrol.jetbrains.run;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlutterDaemonDeviceProvider {
  private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
  private static final Pattern PLATFORM_PATTERN = Pattern.compile("\"platform\"\\s*:\\s*\"([^\"]+)\"");

  private FlutterDaemonDeviceProvider() {
  }

  public static @NotNull List<DeviceItem> loadDevices() {
    Map<String, DeviceItem> devices = new LinkedHashMap<>();
    devices.put("all", new DeviceItem("all", "All devices", ""));

    Process process = null;
    try {
      process = new ProcessBuilder("flutter", "daemon")
          .redirectErrorStream(true)
          .start();

      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
           BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        writer.write("{\"id\":1,\"method\":\"device.enable\"}\n");
        writer.flush();

        long deadline = System.currentTimeMillis() + 1500;
        while (System.currentTimeMillis() < deadline) {
          if (reader.ready()) {
            String line = reader.readLine();
            if (line == null) {
              break;
            }
            parseDeviceAdded(line, devices);
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
      return new ArrayList<>(devices.values());
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return new ArrayList<>(devices.values());
  }

  private static void parseDeviceAdded(@NotNull String line, @NotNull Map<String, DeviceItem> devices) {
    if (!line.contains("\"event\":\"device.added\"")) {
      return;
    }
    String id = match(line, ID_PATTERN);
    if (id == null || id.isEmpty()) {
      return;
    }
    String name = match(line, NAME_PATTERN);
    String platform = match(line, PLATFORM_PATTERN);
    devices.put(id, new DeviceItem(id, name == null ? id : name, platform == null ? "" : platform));
  }

  private static String match(@NotNull String line, @NotNull Pattern pattern) {
    Matcher matcher = pattern.matcher(line);
    if (!matcher.find()) {
      return null;
    }
    return matcher.group(1);
  }

  public static final class DeviceItem {
    public final String id;
    public final String name;
    public final String platform;

    public DeviceItem(@NotNull String id, @NotNull String name, @NotNull String platform) {
      this.id = id;
      this.name = name;
      this.platform = platform;
    }

    @Override
    public String toString() {
      if (platform == null || platform.isEmpty()) {
        return name;
      }
      return name + " (" + platform + ")";
    }
  }
}
