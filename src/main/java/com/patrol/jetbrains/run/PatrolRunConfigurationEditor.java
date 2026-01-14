package com.patrol.jetbrains.run;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBComboBox;
import com.intellij.util.ui.FormBuilder;
import com.patrol.jetbrains.DefaultPatrolCliLocator;
import com.patrol.jetbrains.cli.PatrolCliVersionChecker;
import com.patrol.jetbrains.cli.SemanticVersion;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class PatrolRunConfigurationEditor extends SettingsEditor<PatrolRunConfiguration> {
  private final JBTextField targetField = new JBTextField();
  private final JBTextField argsField = new JBTextField();
  private final JBTextField workingDirField = new JBTextField();
  private final JBTextField cliPathField = new JBTextField();
  private final JBTextField resolvedCliField = new JBTextField();
  private final JBComboBox<PatrolCommandMode> commandModeBox =
      new JBComboBox<>(PatrolCommandMode.values());
  private final JBCheckBox diagnosticCheckBox = new JBCheckBox("Enable diagnostic logging");
  private final EnvironmentVariablesComponent envComponent = new EnvironmentVariablesComponent();
  private final AtomicLong resolveRequestId = new AtomicLong();

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
    updateResolvedCliAsync();
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
    resolvedCliField.setEditable(false);
    cliPathField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        updateResolvedCliAsync();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateResolvedCliAsync();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateResolvedCliAsync();
      }
    });
    JPanel panel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Test target", targetField)
        .addLabeledComponent("Command", commandModeBox)
        .addLabeledComponent("Patrol CLI args", argsField)
        .addLabeledComponent("Working directory", workingDirField)
        .addLabeledComponent("Patrol CLI path", cliPathField)
        .addLabeledComponent("Resolved Patrol CLI", resolvedCliField)
        .addComponent(diagnosticCheckBox)
        .addComponent(envComponent)
        .getPanel();
    return panel;
  }

  private void updateResolvedCliAsync() {
    String override = cliPathField.getText();
    if (!StringUtil.isEmptyOrSpaces(override)) {
      resolvedCliField.setText("Override: " + override);
      return;
    }

    long requestId = resolveRequestId.incrementAndGet();
    resolvedCliField.setText("Detecting...");
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      Optional<Path> resolved = new DefaultPatrolCliLocator(null).findPatrolCli();
      String text;
      if (resolved.isEmpty()) {
        text = "Not found on PATH";
      } else {
        Path path = resolved.get();
        Optional<SemanticVersion> version = PatrolCliVersionChecker.getVersion(path.toString());
        String versionText = version.map(SemanticVersion::toString).orElse("unknown");
        text = "Detected: " + versionText + " (" + path + ")";
      }
      SwingUtilities.invokeLater(() -> {
        if (resolveRequestId.get() != requestId) {
          return;
        }
        resolvedCliField.setText(text);
      });
    });
  }
}
