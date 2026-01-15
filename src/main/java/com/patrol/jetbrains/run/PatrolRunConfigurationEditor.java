package com.patrol.jetbrains.run;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.FormBuilder;
import com.patrol.jetbrains.DefaultPatrolCliLocator;
import com.patrol.jetbrains.settings.PatrolAppSettingsState;
import com.patrol.jetbrains.settings.PatrolProjectSettingsState;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Icon;
import java.util.EnumMap;
import java.util.Map;

public final class PatrolRunConfigurationEditor extends SettingsEditor<PatrolRunConfiguration> {
  private static final Icon PATROL_ICON = IconLoader.getIcon("/icons/patrol.svg", PatrolRunConfigurationEditor.class);

  private final JBTextField targetField = new JBTextField();
  private final JBTextField argsField = new JBTextField();
  private final JBTextField deviceField = new JBTextField();
  private final JBTextField workingDirField = new JBTextField();
  private final JBTextField cliPathTextField = new JBTextField();
  private final TextFieldWithBrowseButton cliPathField = new TextFieldWithBrowseButton(cliPathTextField);
  private final JComboBox<PatrolCommandMode> commandModeBox =
      new JComboBox<>(PatrolCommandMode.values());
  private final JBCheckBox diagnosticCheckBox = new JBCheckBox("Enable diagnostic logging");
  private final EnvironmentVariablesComponent envComponent = new EnvironmentVariablesComponent();
  private final Map<PatrolRunOption, Boolean> optionEnabled = new EnumMap<>(PatrolRunOption.class);
  private final Map<PatrolRunOption, JComponent> optionRows = new EnumMap<>(PatrolRunOption.class);
  private final Map<PatrolRunOption, JBCheckBox> optionToggleFields = new EnumMap<>(PatrolRunOption.class);
  private final Map<PatrolRunOption, JBTextField> optionValueFields = new EnumMap<>(PatrolRunOption.class);
  private final JPanel optionsPanel = new JPanel();
  private final ActionLink modifyOptionsLink =
      new ActionLink("Modify options...", (java.awt.event.ActionListener) event -> showOptionsPopup());
  private com.intellij.openapi.project.Project project;

  @Override
  protected void resetEditorFrom(@NotNull PatrolRunConfiguration configuration) {
    project = configuration.getProject();
    targetField.setText(configuration.getTarget());
    argsField.setText(configuration.getCliArgs());
    deviceField.setText(configuration.getDevice());
    workingDirField.setText(configuration.getWorkingDir());
    cliPathField.setText(configuration.getCliPath());
    commandModeBox.setSelectedItem(configuration.getCommandMode());
    diagnosticCheckBox.setSelected(configuration.isDiagnosticMode());
    envComponent.setEnvs(configuration.getEnvData().getEnvs());
    envComponent.setPassParentEnvs(configuration.getEnvData().isPassParentEnvs());
    resetOptions(configuration);
    updateCliPathHint();
  }

  @Override
  protected void applyEditorTo(@NotNull PatrolRunConfiguration configuration) {
    configuration.setTarget(targetField.getText());
    configuration.setCliArgs(argsField.getText());
    configuration.setDevice(deviceField.getText());
    configuration.setWorkingDir(workingDirField.getText());
    configuration.setCliPath(cliPathField.getText());
    configuration.setCommandMode((PatrolCommandMode) commandModeBox.getSelectedItem());
    configuration.setDiagnosticMode(diagnosticCheckBox.isSelected());
    configuration.setEnvData(envComponent.getEnvData());
    applyOptions(configuration);
  }

  @Override
  protected @NotNull JComponent createEditor() {
    commandModeBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
      @Override
      public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list,
                                                             Object value,
                                                             int index,
                                                             boolean isSelected,
                                                             boolean cellHasFocus) {
        PatrolCommandMode mode = (PatrolCommandMode) value;
        return super.getListCellRendererComponent(list,
            mode == null ? "" : mode.getDisplayName(),
            index,
            isSelected,
            cellHasFocus);
      }
    });
    cliPathTextField.getEmptyText().setText("Auto-detecting Patrol CLI...");
    cliPathField.setButtonIcon(PATROL_ICON);
    cliPathField.addBrowseFolderListener(
        "Select Patrol CLI",
        "Choose the Patrol CLI executable.",
        null,
        FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor().withFileFilter(this::isPatrolCli)
    );
    deviceField.getEmptyText().setText("Device ID or name (e.g., emulator-5554, iPhone 14)");
    commandModeBox.addActionListener(event -> updateOptionRowsVisibility());

    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
    optionsPanel.setBorder(JBUI.Borders.emptyTop(4));
    buildOptionsPanel();

    JPanel panel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Test target", targetField)
        .addLabeledComponent("Command", commandModeBox)
        .addLabeledComponent("Device", deviceField)
        .addLabeledComponent("Patrol CLI args", argsField)
        .addLabeledComponent("Working directory", workingDirField)
        .addLabeledComponent("Patrol CLI path", cliPathField)
        .addComponent(modifyOptionsLink)
        .addComponent(optionsPanel)
        .addComponent(diagnosticCheckBox)
        .addComponent(envComponent)
        .getPanel();
    return panel;
  }

  private boolean isPatrolCli(@NotNull VirtualFile file) {
    String name = file.getName().toLowerCase();
    return name.equals("patrol") || name.equals("patrol.bat") || name.equals("patrol.exe");
  }

  private void updateCliPathHint() {
    String override = cliPathField.getText();
    if (!StringUtil.isEmptyOrSpaces(override)) {
      cliPathTextField.getEmptyText().setText("");
      return;
    }

    String text;
    String defaultPath = PatrolAppSettingsState.getInstance().defaultCliPath;
    String projectPath = "";
    if (project != null) {
      PatrolProjectSettingsState projectSettings = PatrolProjectSettingsState.getInstance(project);
      if (projectSettings.useProjectCliPath) {
        projectPath = projectSettings.projectCliPath;
      }
    }
    boolean hasProjectPath = !StringUtil.isEmptyOrSpaces(projectPath);
    java.nio.file.Path preferred = null;
    if (hasProjectPath) {
      preferred = java.nio.file.Path.of(projectPath.trim());
    } else if (!StringUtil.isEmptyOrSpaces(defaultPath)) {
      preferred = java.nio.file.Path.of(defaultPath.trim());
    }

    java.util.Optional<java.nio.file.Path> resolved = new DefaultPatrolCliLocator(preferred).findPatrolCli();
    if (resolved.isPresent()) {
      if (preferred != null && resolved.get().equals(preferred)) {
        text = hasProjectPath ? "Project default: " + resolved.get() : "Default: " + resolved.get();
      } else if (preferred != null) {
        text = "Detected: " + resolved.get() + " (default not found)";
      } else {
        text = "Detected: " + resolved.get();
      }
    } else if (preferred != null) {
      text = "Default not found: " + preferred;
    } else {
      text = "Not found (checks PATH and ~/.pub-cache/bin)";
    }
    cliPathTextField.getEmptyText().setText(text);
  }

  private void buildOptionsPanel() {
    optionsPanel.removeAll();
    optionRows.clear();
    optionToggleFields.clear();
    optionValueFields.clear();

    for (PatrolRunOption option : PatrolRunOption.values()) {
      JComponent row = createOptionRow(option);
      optionRows.put(option, row);
      optionsPanel.add(row);
    }
  }

  private JComponent createOptionRow(@NotNull PatrolRunOption option) {
    if (option.getType() == PatrolRunOptionType.TOGGLE) {
      JBCheckBox checkBox = new JBCheckBox(option.getLabel());
      optionToggleFields.put(option, checkBox);
      return checkBox;
    }
    JBTextField field = new JBTextField();
    optionValueFields.put(option, field);
    return createFieldPanel(option.getLabel(), field);
  }

  private JPanel createFieldPanel(@NotNull String label, @NotNull JComponent field) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(JBUI.Borders.emptyBottom(6));
    panel.add(new com.intellij.ui.components.JBLabel(label));
    panel.add(field);
    return panel;
  }

  private void resetOptions(@NotNull PatrolRunConfiguration configuration) {
    for (PatrolRunOption option : PatrolRunOption.values()) {
      optionEnabled.put(option, configuration.isOptionEnabled(option));
      if (option.getType() == PatrolRunOptionType.TOGGLE) {
        boolean value = Boolean.parseBoolean(configuration.getOptionValue(option));
        JBCheckBox checkBox = optionToggleFields.get(option);
        if (checkBox != null) {
          checkBox.setSelected(value);
        }
      } else {
        JBTextField field = optionValueFields.get(option);
        if (field != null) {
          field.setText(configuration.getOptionValue(option));
        }
      }
    }
    updateOptionRowsVisibility();
  }

  private void applyOptions(@NotNull PatrolRunConfiguration configuration) {
    for (PatrolRunOption option : PatrolRunOption.values()) {
      boolean enabled = optionEnabled.getOrDefault(option, Boolean.FALSE);
      configuration.setOptionEnabled(option, enabled);
      if (option.getType() == PatrolRunOptionType.TOGGLE) {
        JBCheckBox checkBox = optionToggleFields.get(option);
        configuration.setOptionValue(option, checkBox != null && checkBox.isSelected() ? "true" : "false");
      } else {
        JBTextField field = optionValueFields.get(option);
        configuration.setOptionValue(option, field == null ? "" : field.getText().trim());
      }
    }
  }

  private void updateOptionRowsVisibility() {
    PatrolCommandMode mode = (PatrolCommandMode) commandModeBox.getSelectedItem();
    if (mode == null) {
      mode = PatrolCommandMode.TEST;
    }
    for (PatrolRunOption option : PatrolRunOption.values()) {
      JComponent row = optionRows.get(option);
      if (row == null) {
        continue;
      }
      boolean visible = option.appliesTo(mode) && optionEnabled.getOrDefault(option, Boolean.FALSE);
      row.setVisible(visible);
    }
    optionsPanel.revalidate();
    optionsPanel.repaint();
  }

  private void showOptionsPopup() {
    PatrolCommandMode mode = (PatrolCommandMode) commandModeBox.getSelectedItem();
    if (mode == null) {
      mode = PatrolCommandMode.TEST;
    }
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(JBUI.Borders.empty(6));
    for (PatrolRunOption option : PatrolRunOption.values()) {
      if (!option.appliesTo(mode)) {
        continue;
      }
      JBCheckBox checkBox = new JBCheckBox(option.getLabel());
      checkBox.setSelected(optionEnabled.getOrDefault(option, Boolean.FALSE));
      checkBox.addActionListener(event -> {
        optionEnabled.put(option, checkBox.isSelected());
        if (checkBox.isSelected()) {
          applyDefaultOptionValue(option);
        }
        updateOptionRowsVisibility();
      });
      panel.add(checkBox);
    }
    com.intellij.openapi.ui.popup.JBPopup popup = com.intellij.openapi.ui.popup.JBPopupFactory.getInstance()
        .createComponentPopupBuilder(panel, panel)
        .setRequestFocus(true)
        .createPopup();
    popup.showUnderneathOf(modifyOptionsLink);
  }

  private void applyDefaultOptionValue(@NotNull PatrolRunOption option) {
    if (option.getType() == PatrolRunOptionType.TOGGLE) {
      JBCheckBox checkBox = optionToggleFields.get(option);
      if (checkBox != null && option.getDefaultToggleValue() != null) {
        checkBox.setSelected(option.getDefaultToggleValue());
      }
      return;
    }
    JBTextField field = optionValueFields.get(option);
    if (field != null && field.getText().isEmpty()) {
      field.setText("");
    }
  }
}
