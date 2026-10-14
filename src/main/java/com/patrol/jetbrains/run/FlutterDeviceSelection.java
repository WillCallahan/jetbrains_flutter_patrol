package com.patrol.jetbrains.run;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Reads the device chosen in the IDE's Flutter device selector, the dropdown to the left of the
 * run configuration.
 *
 * <p>The Flutter plugin does not contribute platform {@code ExecutionTarget}s for devices, so the
 * selection is only available from its own {@code DeviceService}. That service is reached
 * reflectively on purpose: the Flutter plugin is a required dependency so the classes are always
 * present at runtime, but its version is tied to the IDE version and this plugin supports a range
 * of them. Reflection keeps the build independent of any single flutter-intellij release.
 * {@code DeviceService.getSelectedDevice()} and {@code FlutterDevice.deviceId()} are both present
 * in at least flutter-intellij 94 and 97.
 */
public final class FlutterDeviceSelection {
  private static final Logger LOG = Logger.getInstance(FlutterDeviceSelection.class);
  private static final String DEVICE_SERVICE_CLASS = "io.flutter.run.daemon.DeviceService";

  private FlutterDeviceSelection() {
  }

  /** Returns the id to pass to {@code patrol --device}, or an empty string if none is selected. */
  public static @NotNull String selectedDeviceId(@Nullable Project project) {
    return readSelectedDevice(project, "deviceId");
  }

  /** Returns the human-readable device name, for diagnostics and error messages. */
  public static @NotNull String selectedDeviceName(@Nullable Project project) {
    return readSelectedDevice(project, "deviceName");
  }

  private static @NotNull String readSelectedDevice(@Nullable Project project, @NotNull String accessor) {
    if (project == null) {
      return "";
    }
    try {
      Class<?> serviceClass =
          Class.forName(DEVICE_SERVICE_CLASS, true, FlutterDeviceSelection.class.getClassLoader());
      Object service = serviceClass.getMethod("getInstance", Project.class).invoke(null, project);
      if (service == null) {
        return "";
      }
      Object device = serviceClass.getMethod("getSelectedDevice").invoke(service);
      if (device == null) {
        return "";
      }
      Method method = device.getClass().getMethod(accessor);
      Object value = method.invoke(device);
      return value == null ? "" : value.toString().trim();
    } catch (ReflectiveOperationException | RuntimeException e) {
      // The Flutter plugin is missing, disabled, or has changed shape. Fall back rather than fail.
      LOG.info("Could not read the selected device from the Flutter plugin via " + accessor, e);
      return "";
    }
  }
}
