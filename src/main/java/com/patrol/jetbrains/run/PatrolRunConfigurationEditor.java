package com.patrol.jetbrains.run;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBComboBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;

public final class PatrolRunConfigurationEditor extends SettingsEditor<PatrolRunConfiguration> {
  private final JBTextField targetField = new JBTextField();
  private final JBTextField argsField = new JBTextField();
  private final JBTextField workingDirField = new JBTextField();
  private final JBTextField cliPathField = new JBTextField();
  private final JBComboBox<PatrolCommandMode> commandModeBox =
      new JBComboBox<>(PatrolCommandMode.values());
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
}
