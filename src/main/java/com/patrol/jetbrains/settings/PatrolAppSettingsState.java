package com.patrol.jetbrains.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Service
@State(name = "PatrolAppSettings", storages = @Storage("patrol.xml"))
public final class PatrolAppSettingsState implements PersistentStateComponent<PatrolAppSettingsState> {
  public String defaultCliPath = "";
  public List<String> testRoots = new ArrayList<>();

  public PatrolAppSettingsState() {
    if (testRoots.isEmpty()) {
      testRoots.add("integration_test");
      testRoots.add("test");
    }
  }

  public static PatrolAppSettingsState getInstance() {
    return ApplicationManager.getApplication().getService(PatrolAppSettingsState.class);
  }

  @Override
  public @Nullable PatrolAppSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull PatrolAppSettingsState state) {
    this.defaultCliPath = state.defaultCliPath;
    this.testRoots = new ArrayList<>(state.testRoots);
  }
}
