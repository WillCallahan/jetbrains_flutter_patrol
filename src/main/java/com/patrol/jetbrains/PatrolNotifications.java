package com.patrol.jetbrains;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PatrolNotifications {
  public static final String GROUP_ID = "Patrol";

  private PatrolNotifications() {
  }

  public static void warn(@Nullable Project project, @NotNull String message) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup(GROUP_ID)
        .createNotification(message, NotificationType.WARNING)
        .notify(project);
  }
}
