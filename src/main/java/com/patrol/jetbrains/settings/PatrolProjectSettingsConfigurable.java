package com.patrol.jetbrains.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

public final class PatrolProjectSettingsConfigurable implements SearchableConfigurable {
  private static final Icon PATROL_ICON = IconLoader.getIcon("/icons/patrol.svg", PatrolProjectSettingsConfigurable.class);

  private final Project project;

  private JPanel panel;
  private TextFieldWithBrowseButton defaultCliPathField;
  private JBCheckBox projectCliOverrideCheckBox;
  private TextFieldWithBrowseButton projectCliPathField;
  private JBLabel pubspecValueLabel;
  private ActionLink openPubspecLink;
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
    defaultCliPathField = new TextFieldWithBrowseButton();
    defaultCliPathField.setButtonIcon(PATROL_ICON);
    defaultCliPathField.addBrowseFolderListener(
        "Select Patrol CLI",
        "Choose the default Patrol CLI executable.",
        null,
        FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
    );

    projectCliOverrideCheckBox = new JBCheckBox("Use project-specific Patrol CLI path");
    projectCliOverrideCheckBox.addActionListener(event -> updateProjectCliEnabled());
    projectCliPathField = new TextFieldWithBrowseButton();
    projectCliPathField.setButtonIcon(PATROL_ICON);
    projectCliPathField.addBrowseFolderListener(
        "Select Project Patrol CLI",
        "Choose the Patrol CLI executable for this project.",
        null,
        FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
    );

    pubspecValueLabel = new JBLabel();
    openPubspecLink = new ActionLink("Open pubspec.yaml", (java.awt.event.ActionListener) event -> openPubspec());

    pubspecWarningLabel = new JBLabel();
    pubspecWarningLabel.setForeground(JBColor.RED);
    pubspecWarningLabel.setVisible(false);

    panel = FormBuilder.createFormBuilder()
        .addComponent(new TitledSeparator("IDE Defaults"))
        .addComponent(createFieldPanel("Default Patrol CLI path", defaultCliPathField, null))
        .addComponent(new TitledSeparator("Project Overrides"))
        .addComponent(projectCliOverrideCheckBox)
        .addComponent(createFieldPanel("Project Patrol CLI path", projectCliPathField, null))
        .addComponent(new TitledSeparator("Patrol Test Directory"))
        .addComponent(createFieldPanel("Patrol test directory", pubspecValueLabel, openPubspecLink))
        .addComponent(pubspecWarningLabel)
        .getPanel();
    return panel;
  }

  @Override
  public boolean isModified() {
    PatrolAppSettingsState appSettings = PatrolAppSettingsState.getInstance();
    PatrolProjectSettingsState settings = PatrolProjectSettingsState.getInstance(project);
    if (projectCliOverrideCheckBox == null) {
      return false;
    }
    String currentCli = StringUtil.notNullize(defaultCliPathField.getText()).trim();
    String storedCli = StringUtil.notNullize(appSettings.defaultCliPath).trim();
    if (!currentCli.equals(storedCli)) {
      return true;
    }
    if (projectCliOverrideCheckBox.isSelected() != settings.useProjectCliPath) {
      return true;
    }
    String currentProjectCli = StringUtil.notNullize(projectCliPathField.getText()).trim();
    String storedProjectCli = StringUtil.notNullize(settings.projectCliPath).trim();
    return !currentProjectCli.equals(storedProjectCli);
  }

  @Override
  public void apply() {
    if (projectCliOverrideCheckBox == null) {
      return;
    }
    PatrolAppSettingsState appSettings = PatrolAppSettingsState.getInstance();
    appSettings.defaultCliPath = StringUtil.notNullize(defaultCliPathField.getText()).trim();
    PatrolProjectSettingsState settings = PatrolProjectSettingsState.getInstance(project);
    settings.useProjectCliPath = projectCliOverrideCheckBox.isSelected();
    settings.projectCliPath = StringUtil.notNullize(projectCliPathField.getText()).trim();
    updatePubspecWarning();
  }

  @Override
  public void reset() {
    PatrolAppSettingsState appSettings = PatrolAppSettingsState.getInstance();
    PatrolProjectSettingsState settings = PatrolProjectSettingsState.getInstance(project);
    if (defaultCliPathField != null) {
      defaultCliPathField.setText(appSettings.defaultCliPath);
    }
    if (projectCliOverrideCheckBox != null) {
      projectCliOverrideCheckBox.setSelected(settings.useProjectCliPath);
    }
    if (projectCliPathField != null) {
      projectCliPathField.setText(settings.projectCliPath);
    }
    updateProjectCliEnabled();
    updatePubspecValue();
    updatePubspecWarning();
  }

  @Override
  public void disposeUIResources() {
    panel = null;
    defaultCliPathField = null;
    projectCliOverrideCheckBox = null;
    projectCliPathField = null;
    pubspecValueLabel = null;
    openPubspecLink = null;
    pubspecWarningLabel = null;
  }

  private void updateProjectCliEnabled() {
    if (projectCliOverrideCheckBox == null || projectCliPathField == null) {
      return;
    }
    boolean enabled = projectCliOverrideCheckBox.isSelected();
    projectCliPathField.setEnabled(enabled);
  }

  private void updatePubspecWarning() {
    if (pubspecWarningLabel == null) {
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

  private void updatePubspecValue() {
    if (pubspecValueLabel == null) {
      return;
    }
    String value = PubspecUtil.readPatrolTestDirectory(project).orElse("");
    if (StringUtil.isEmptyOrSpaces(value)) {
      pubspecValueLabel.setText("Set patrol.test_directory in pubspec.yaml (currently using integration_test).");
    } else {
      pubspecValueLabel.setText("Configured via patrol.test_directory: " + value.trim());
    }
  }

  private void openPubspec() {
    String basePath = project.getBasePath();
    if (basePath == null || basePath.isEmpty()) {
      return;
    }
    VirtualFile file = LocalFileSystem.getInstance().findFileByPath(basePath + "/pubspec.yaml");
    if (file != null) {
      FileEditorManager.getInstance(project).openFile(file, true);
    }
  }

  private @NotNull JPanel createFieldPanel(@NotNull String labelText,
                                           @NotNull JComponent field,
                                           @Nullable JComponent helper) {
    JPanel container = new JPanel();
    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
    container.add(new JBLabel(labelText));
    container.add(field);
    if (helper != null) {
      container.add(helper);
    }
    return container;
  }
}
