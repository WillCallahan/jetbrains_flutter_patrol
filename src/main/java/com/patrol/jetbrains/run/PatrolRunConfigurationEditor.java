package com.patrol.jetbrains.run;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import com.patrol.jetbrains.DefaultPatrolCliLocator;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Icon;

public final class PatrolRunConfigurationEditor extends SettingsEditor<PatrolRunConfiguration> {
  private static final Icon PATROL_ICON = IconLoader.getIcon("/icons/patrol.svg", PatrolRunConfigurationEditor.class);

  private final JBTextField targetField = new JBTextField();
  private final JBTextField argsField = new JBTextField();
  private final JBTextField workingDirField = new JBTextField();
  private final JBTextField cliPathTextField = new JBTextField();
  private final TextFieldWithBrowseButton cliPathField = new TextFieldWithBrowseButton(cliPathTextField);
  private final JComboBox<PatrolCommandMode> commandModeBox =
      new JComboBox<>(PatrolCommandMode.values());
  private final JBCheckBox diagnosticCheckBox = new JBCheckBox("Enable diagnostic logging");
  private final EnvironmentVariablesComponent envComponent = new EnvironmentVariablesComponent();

  @Override
  protected void resetEditorFrom(@NotNull PatrolRunConfiguration configuration) {
    targetField.setText(configuration.getTarget());
    argsField.setText(configuration.getCliArgs());
    workingDirField.setText(configuration.getWorkingDir());
    cliPathField.setText(configuration.getCliPath());
    commandModeBox.setSelectedItem(configuration.getCommandMode());
    diagnosticCheckBox.setSelected(configuration.isDiagnosticMode());
    envComponent.setEnvs(configuration.getEnvData().getEnvs());
    envComponent.setPassParentEnvs(configuration.getEnvData().isPassParentEnvs());
    updateCliPathHint();
  }

  @Override
  protected void applyEditorTo(@NotNull PatrolRunConfiguration configuration) {
    configuration.setTarget(targetField.getText());
    configuration.setCliArgs(argsField.getText());
    configuration.setWorkingDir(workingDirField.getText());
    configuration.setCliPath(cliPathField.getText());
    configuration.setCommandMode((PatrolCommandMode) commandModeBox.getSelectedItem());
    configuration.setDiagnosticMode(diagnosticCheckBox.isSelected());
    configuration.setEnvData(envComponent.getEnvData());
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
    JPanel panel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Test target", targetField)
        .addLabeledComponent("Command", commandModeBox)
        .addLabeledComponent("Patrol CLI args", argsField)
        .addLabeledComponent("Working directory", workingDirField)
        .addLabeledComponent("Patrol CLI path", cliPathField)
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
    java.util.Optional<java.nio.file.Path> resolved = new DefaultPatrolCliLocator(null).findPatrolCli();
    if (resolved.isPresent()) {
      text = "Detected: " + resolved.get().toString();
    } else {
      text = "Not found (checks PATH and ~/.pub-cache/bin)";
    }
    cliPathTextField.getEmptyText().setText(text);
  }
}
