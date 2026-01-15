package com.patrol.jetbrains.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Service(Service.Level.PROJECT)
@State(name = "PatrolProjectSettings", storages = @Storage("patrol.project.xml"))
public final class PatrolProjectSettingsState implements PersistentStateComponent<PatrolProjectSettingsState> {
  public boolean useProjectCliPath = false;
  public String projectCliPath = "";

  public static PatrolProjectSettingsState getInstance(@NotNull Project project) {
    return project.getService(PatrolProjectSettingsState.class);
  }

  @Override
  public @Nullable PatrolProjectSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull PatrolProjectSettingsState state) {
    this.useProjectCliPath = state.useProjectCliPath;
    this.projectCliPath = state.projectCliPath;
  }
}
