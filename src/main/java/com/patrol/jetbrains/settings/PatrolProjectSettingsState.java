package com.patrol.jetbrains.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.PROJECT)
@State(name = "PatrolProjectSettings", storages = @Storage("patrol.project.xml"))
public final class PatrolProjectSettingsState implements PersistentStateComponent<PatrolProjectSettingsState> {
  public boolean useProjectTestRoots = false;
  public boolean includePubspecTestDirectory = true;
  public List<String> projectTestRoots = new ArrayList<>();

  public static PatrolProjectSettingsState getInstance(@NotNull Project project) {
    return project.getService(PatrolProjectSettingsState.class);
  }

  @Override
  public @Nullable PatrolProjectSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull PatrolProjectSettingsState state) {
    this.useProjectTestRoots = state.useProjectTestRoots;
    this.includePubspecTestDirectory = state.includePubspecTestDirectory;
    this.projectTestRoots = new ArrayList<>(state.projectTestRoots);
  }
}
