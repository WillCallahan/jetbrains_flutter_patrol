package com.patrol.jetbrains.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

public final class PatrolProjectSettingsConfigurable implements SearchableConfigurable {
  private final Project project;

  private JPanel panel;
  private JBCheckBox overrideRootsCheckBox;
  private JBCheckBox includePubspecCheckBox;
  private CollectionListModel<String> testRootsModel;
  private JBList<String> testRootsList;
  private JPanel testRootsPanel;
  private JBLabel pubspecWarningLabel;

  public PatrolProjectSettingsConfigurable(@NotNull Project project) {
    this.project = project;
  }

  @Override
  public @NotNull String getId() {
    return "patrol.project.settings";
  }

  @Override
  public String getDisplayName() {
    return "Patrol";
  }

  @Override
  public @Nullable JComponent createComponent() {
    overrideRootsCheckBox = new JBCheckBox("Override test roots for this project");
    includePubspecCheckBox = new JBCheckBox("Include patrol.test_directory from pubspec.yaml");
    includePubspecCheckBox.addActionListener(event -> updatePubspecWarning());
    overrideRootsCheckBox.addActionListener(event -> updateRootsEnabled());

    testRootsModel = new CollectionListModel<>();
    testRootsList = new JBList<>(testRootsModel);
    testRootsPanel = ToolbarDecorator.createDecorator(testRootsList)
        .setAddAction(button -> addTestRoot())
        .setRemoveAction(button -> removeTestRoot())
        .createPanel();

    pubspecWarningLabel = new JBLabel();
    pubspecWarningLabel.setForeground(JBColor.RED);
    pubspecWarningLabel.setVisible(false);

    panel = FormBuilder.createFormBuilder()
        .addComponent(overrideRootsCheckBox)
        .addLabeledComponent("Project Patrol test root paths", testRootsPanel)
        .addComponent(includePubspecCheckBox)
        .addComponent(pubspecWarningLabel)
        .getPanel();
    return panel;
  }

  @Override
  public boolean isModified() {
    PatrolProjectSettingsState settings = PatrolProjectSettingsState.getInstance(project);
    if (overrideRootsCheckBox == null) {
      return false;
    }
    if (overrideRootsCheckBox.isSelected() != settings.useProjectTestRoots) {
      return true;
    }
    if (includePubspecCheckBox.isSelected() != settings.includePubspecTestDirectory) {
      return true;
    }
    return !getModelRoots().equals(settings.projectTestRoots);
  }

  @Override
  public void apply() {
    if (overrideRootsCheckBox == null) {
      return;
    }
    PatrolProjectSettingsState settings = PatrolProjectSettingsState.getInstance(project);
    settings.useProjectTestRoots = overrideRootsCheckBox.isSelected();
    settings.includePubspecTestDirectory = includePubspecCheckBox.isSelected();
    settings.projectTestRoots = new ArrayList<>(getModelRoots());
    updatePubspecWarning();
  }

  @Override
  public void reset() {
    PatrolProjectSettingsState settings = PatrolProjectSettingsState.getInstance(project);
    if (overrideRootsCheckBox != null) {
      overrideRootsCheckBox.setSelected(settings.useProjectTestRoots);
    }
    if (includePubspecCheckBox != null) {
      includePubspecCheckBox.setSelected(settings.includePubspecTestDirectory);
    }
    if (testRootsModel != null) {
      testRootsModel.removeAll();
      testRootsModel.addAll(0, settings.projectTestRoots);
    }
    updateRootsEnabled();
    updatePubspecWarning();
  }

  @Override
  public void disposeUIResources() {
    panel = null;
    overrideRootsCheckBox = null;
    includePubspecCheckBox = null;
    testRootsModel = null;
    testRootsList = null;
    testRootsPanel = null;
    pubspecWarningLabel = null;
  }

  private void addTestRoot() {
    if (overrideRootsCheckBox != null && !overrideRootsCheckBox.isSelected()) {
      return;
    }
    String input = Messages.showInputDialog(panel,
        "Enter a Patrol test root path (relative to project root).",
        "Add Project Test Root",
        null);
    if (StringUtil.isEmptyOrSpaces(input)) {
      return;
    }
    testRootsModel.add(StringUtil.trim(input));
  }

  private void removeTestRoot() {
    if (overrideRootsCheckBox != null && !overrideRootsCheckBox.isSelected()) {
      return;
    }
    int index = testRootsList.getSelectedIndex();
    if (index >= 0) {
      testRootsModel.remove(index);
    }
  }

  private void updateRootsEnabled() {
    if (overrideRootsCheckBox == null || testRootsList == null || testRootsPanel == null) {
      return;
    }
    boolean enabled = overrideRootsCheckBox.isSelected();
    testRootsList.setEnabled(enabled);
    testRootsPanel.setEnabled(enabled);
  }

  private void updatePubspecWarning() {
    if (pubspecWarningLabel == null || includePubspecCheckBox == null) {
      return;
    }
    if (!includePubspecCheckBox.isSelected()) {
      pubspecWarningLabel.setVisible(false);
      return;
    }
    String warning = PubspecUtil.validatePatrolTestDirectory(project);
    if (StringUtil.isEmptyOrSpaces(warning)) {
      pubspecWarningLabel.setVisible(false);
    } else {
      pubspecWarningLabel.setText(warning);
      pubspecWarningLabel.setVisible(true);
    }
  }

  private @NotNull List<String> getModelRoots() {
    if (testRootsModel == null) {
      return List.of();
    }
    return new ArrayList<>(testRootsModel.getItems());
  }
}
