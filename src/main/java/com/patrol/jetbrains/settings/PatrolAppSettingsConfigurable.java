package com.patrol.jetbrains.settings;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.FormBuilder;
import com.intellij.ui.CollectionListModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

public final class PatrolAppSettingsConfigurable implements SearchableConfigurable {
  private static final Icon PATROL_ICON = IconLoader.getIcon("/icons/patrol.svg", PatrolAppSettingsConfigurable.class);

  private JPanel panel;
  private TextFieldWithBrowseButton defaultCliPathField;
  private CollectionListModel<String> testRootsModel;
  private JBList<String> testRootsList;

  @Override
  public @NotNull String getId() {
    return "patrol.settings";
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

    testRootsModel = new CollectionListModel<>();
    testRootsList = new JBList<>(testRootsModel);
    JPanel testRootsPanel = ToolbarDecorator.createDecorator(testRootsList)
        .setAddAction(button -> addTestRoot())
        .setRemoveAction(button -> removeTestRoot())
        .createPanel();

    panel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Default Patrol CLI path", defaultCliPathField)
        .addLabeledComponent("Patrol test root paths", testRootsPanel)
        .getPanel();
    return panel;
  }

  @Override
  public boolean isModified() {
    PatrolAppSettingsState settings = PatrolAppSettingsState.getInstance();
    if (defaultCliPathField == null) {
      return false;
    }
    String currentCli = StringUtil.notNullize(defaultCliPathField.getText()).trim();
    String storedCli = StringUtil.notNullize(settings.defaultCliPath).trim();
    if (!currentCli.equals(storedCli)) {
      return true;
    }
    return !getModelRoots().equals(settings.testRoots);
  }

  @Override
  public void apply() {
    if (defaultCliPathField == null) {
      return;
    }
    PatrolAppSettingsState settings = PatrolAppSettingsState.getInstance();
    settings.defaultCliPath = StringUtil.notNullize(defaultCliPathField.getText()).trim();
    settings.testRoots = new ArrayList<>(getModelRoots());
  }

  @Override
  public void reset() {
    PatrolAppSettingsState settings = PatrolAppSettingsState.getInstance();
    if (defaultCliPathField != null) {
      defaultCliPathField.setText(settings.defaultCliPath);
    }
    if (testRootsModel != null) {
      testRootsModel.removeAll();
      testRootsModel.addAll(0, settings.testRoots);
    }
  }

  @Override
  public void disposeUIResources() {
    panel = null;
    defaultCliPathField = null;
    testRootsModel = null;
    testRootsList = null;
  }

  private void addTestRoot() {
    String input = Messages.showInputDialog(panel,
        "Enter a Patrol test root path (relative to project root).",
        "Add Test Root",
        null);
    if (StringUtil.isEmptyOrSpaces(input)) {
      return;
    }
    testRootsModel.add(StringUtil.trim(input));
  }

  private void removeTestRoot() {
    int index = testRootsList.getSelectedIndex();
    if (index >= 0) {
      testRootsModel.remove(index);
    }
  }

  private @NotNull List<String> getModelRoots() {
    if (testRootsModel == null) {
      return List.of();
    }
    return new ArrayList<>(testRootsModel.getItems());
  }
}
