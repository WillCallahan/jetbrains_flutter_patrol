package com.patrol.jetbrains.run;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.icons.AllIcons;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.CheckBoxList;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ListTableModel;
import com.patrol.jetbrains.DefaultPatrolCliLocator;
import com.patrol.jetbrains.discovery.PatrolTestUtil;
import com.patrol.jetbrains.settings.PatrolAppSettingsState;
import com.patrol.jetbrains.settings.PatrolProjectSettingsState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PatrolRunConfigurationEditor extends SettingsEditor<PatrolRunConfiguration> {
  private static final String TARGET_TOOLTIP =
      "<html>The Dart test entrypoint passed to <code>patrol --target</code>.<br><br>"
          + "The dropdown lists every <code>*patrol_test.dart</code> file found under the Patrol test "
          + "directory (from <code>pubspec.yaml</code>), <code>integration_test/</code> and <code>test/</code>.<br><br>"
          + "You can also type a path yourself: relative paths are resolved against the project root.</html>";
  private static final String COMMAND_TOOLTIP =
      "<html><b>Test</b> &mdash; <code>patrol test</code>. Runs the target once and exits. Use this for CI "
          + "and for a normal single run.<br><br>"
          + "<b>Develop</b> &mdash; <code>patrol develop</code>. Keeps the app running and watches for "
          + "changes so you can iterate on a test, with a Hot Reload button in the Run toolbar.</html>";
  private static final String DART_DEFINES_TOOLTIP =
      "<html>Values passed as <code>--dart-define KEY=VALUE</code>, one per entry.<br><br>"
          + "Paste a whole list in any of these shapes and it is normalised for you:<br>"
          + "<code>--dart-define A=1 --dart-define B=2</code><br>"
          + "<code>A=1;B=2</code><br>"
          + "one <code>KEY=VALUE</code> per line<br><br>"
          + "Use the expand control to edit one per line, or the browse button for a table.</html>";
  private static final String ARGS_TOOLTIP =
      "<html>Extra arguments appended to the Patrol command, parsed like a shell command line.<br><br>"
          + "Use this for flags that have no field above.</html>";
  private static final String WORKING_DIR_TOOLTIP =
      "<html>Directory the Patrol CLI runs in. Defaults to the project root.<br><br>"
          + "Set this when the Flutter package is in a subdirectory of the project.<br><br>"
          + "Version managers such as fvm resolve the SDK from this directory, so it must be the "
          + "directory containing <code>pubspec.yaml</code> (and <code>.fvmrc</code>, if you use fvm).</html>";
  private static final String CLI_PATH_TOOLTIP =
      "<html>Overrides which Patrol CLI executable is used for this run configuration only.<br><br>"
          + "Resolution order: this field &rarr; project override &rarr; IDE default &rarr; "
          + "<code>~/.pub-cache/bin</code> &rarr; <code>PATH</code>.</html>";

  private final ComboBox<String> targetBox = new ComboBox<>();
  private final ExpandableTextField argsField = new ExpandableTextField();
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
  private DartDefinesField dartDefinesField;
  private final JPanel optionsPanel = new JPanel();
  private final JSeparator optionsTopSeparator = new JSeparator();
  private final JSeparator optionsBottomSeparator = new JSeparator();
  private final ActionLink modifyOptionsLink =
      new ActionLink("Modify options...", (java.awt.event.ActionListener) event -> showOptionsPopup());
  private com.intellij.openapi.project.Project project;
  private boolean targetsLoaded;

  @Override
  protected void resetEditorFrom(@NotNull PatrolRunConfiguration configuration) {
    project = configuration.getProject();
    updateWorkingDirHint();
    populateTargets();
    setTargetText(toDisplayPath(configuration.getTarget()));
    argsField.setText(configuration.getCliArgs());
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
    configuration.setTarget(toStoredPath(getTargetText()));
    configuration.setCliArgs(argsField.getText());
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
    cliPathField.addActionListener(event -> browseForCli(
        cliPathField, "Select Patrol CLI", "Choose the Patrol CLI executable.", true));
    commandModeBox.addActionListener(event -> updateOptionRowsVisibility());

    // Editable so a target that discovery missed can still be typed or pasted in.
    targetBox.setEditable(true);
    targetBox.setToolTipText(TARGET_TOOLTIP);
    // The combo's editor is a separate component, so it needs the tooltip too.
    if (targetBox.getEditor().getEditorComponent() instanceof JComponent editorComponent) {
      editorComponent.setToolTipText(TARGET_TOOLTIP);
    }
    commandModeBox.setToolTipText(COMMAND_TOOLTIP);
    argsField.setToolTipText(ARGS_TOOLTIP);
    workingDirField.setToolTipText(WORKING_DIR_TOOLTIP);
    cliPathField.setToolTipText(CLI_PATH_TOOLTIP);
    cliPathTextField.setToolTipText(CLI_PATH_TOOLTIP);

    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
    optionsPanel.setBorder(JBUI.Borders.emptyTop(4));
    buildOptionsPanel();

    JPanel panel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Test target", targetBox)
        .addLabeledComponent("Command", commandModeBox)
        .addComponent(createWideFieldPanel("Patrol CLI args", argsField))
        .addLabeledComponent("Working directory", workingDirField)
        .addLabeledComponent("Patrol CLI path", cliPathField)
        .addComponent(envComponent)
        .addComponent(modifyOptionsLink)
        .addComponent(optionsPanel)
        .addComponent(diagnosticCheckBox)
        .getPanel();
    return panel;
  }

  /**
   * Fills the dropdown with discovered Patrol test entrypoints. The scan touches the filesystem,
   * so it runs once per editor instance rather than on every {@code resetEditorFrom}.
   */
  private void populateTargets() {
    if (targetsLoaded || project == null) {
      return;
    }
    targetsLoaded = true;
    String current = getTargetText();
    for (String target : PatrolTestUtil.findTestTargets(project)) {
      targetBox.addItem(target);
    }
    setTargetText(current);
  }

  private @NotNull String getTargetText() {
    Object item = targetBox.getEditor().getItem();
    return item == null ? "" : item.toString().trim();
  }

  private void setTargetText(@NotNull String value) {
    targetBox.setSelectedItem(value);
    targetBox.getEditor().setItem(value);
  }

  /**
   * Renders a stored target for display. Targets inside the project are shown relative to the
   * project root so they line up with the dropdown entries; anything else is shown verbatim.
   */
  private @NotNull String toDisplayPath(@Nullable String stored) {
    if (StringUtil.isEmptyOrSpaces(stored)) {
      return "";
    }
    String trimmed = stored.trim();
    Path base = projectBase();
    if (base == null) {
      return trimmed;
    }
    try {
      Path target = Path.of(trimmed);
      if (target.isAbsolute() && target.startsWith(base)) {
        return base.relativize(target).toString().replace('\\', '/');
      }
    } catch (java.nio.file.InvalidPathException ignored) {
      // Not a usable path; leave the text exactly as the user typed it.
    }
    return trimmed;
  }

  /**
   * Converts what is shown in the editor back to the absolute path the run configuration stores,
   * which is the form the producers and the CLI invocation already use.
   */
  private @NotNull String toStoredPath(@NotNull String display) {
    if (StringUtil.isEmptyOrSpaces(display)) {
      return "";
    }
    String trimmed = display.trim();
    Path base = projectBase();
    if (base == null) {
      return trimmed;
    }
    try {
      Path target = Path.of(trimmed);
      if (!target.isAbsolute()) {
        return base.resolve(target).normalize().toString();
      }
    } catch (java.nio.file.InvalidPathException ignored) {
      // Not a usable path; leave the text exactly as the user typed it.
    }
    return trimmed;
  }

  /** Blank means "project root", so say which directory that actually is. */
  private void updateWorkingDirHint() {
    Path base = projectBase();
    workingDirField.getEmptyText().setText(
        base == null ? "Defaults to the project root" : "Defaults to the project root: " + base);
  }

  private @Nullable Path projectBase() {
    String basePath = project == null ? null : project.getBasePath();
    if (StringUtil.isEmptyOrSpaces(basePath)) {
      return null;
    }
    try {
      return Path.of(basePath);
    } catch (java.nio.file.InvalidPathException e) {
      return null;
    }
  }

  /**
   * Opens a file chooser for a Patrol CLI path field.
   *
   * <p>Done by hand rather than via {@code addBrowseFolderListener}: the overload taking a title
   * and description is scheduled for removal, and the replacement that moves them onto the
   * descriptor does not exist before 2024.3, which is below this plugin's since-build.
   */
  private void browseForCli(@NotNull TextFieldWithBrowseButton field,
                            @NotNull String title,
                            @NotNull String description,
                            boolean onlyPatrolExecutables) {
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        .withTitle(title)
        .withDescription(description);
    if (onlyPatrolExecutables) {
      descriptor = descriptor.withFileFilter(this::isPatrolCli);
    }
    VirtualFile chosen = FileChooser.chooseFile(descriptor, project, currentFile(field.getText()));
    if (chosen != null) {
      field.setText(FileUtil.toSystemDependentName(chosen.getPath()));
      updateCliPathHint();
    }
  }

  private @Nullable VirtualFile currentFile(@Nullable String path) {
    if (StringUtil.isEmptyOrSpaces(path)) {
      return null;
    }
    return LocalFileSystem.getInstance().findFileByPath(path.trim().replace('\\', '/'));
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

  private JComponent createWideFieldPanel(@NotNull String labelText, @NotNull JComponent field) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JBLabel label = new JBLabel(labelText);
    label.setAlignmentX(0f);
    field.setAlignmentX(0f);
    Dimension preferred = field.getPreferredSize();
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
    panel.add(label);
    panel.add(field);
    return panel;
  }

  private void buildOptionsPanel() {
    optionsPanel.removeAll();
    optionRows.clear();
    optionToggleFields.clear();
    optionValueFields.clear();

    optionsTopSeparator.setAlignmentX(0f);
    optionsBottomSeparator.setAlignmentX(0f);
    optionsPanel.add(optionsTopSeparator);
    for (PatrolRunOption option : PatrolRunOption.values()) {
      JComponent row = createOptionRow(option);
      optionRows.put(option, row);
      optionsPanel.add(row);
    }
    optionsPanel.add(optionsBottomSeparator);
  }


  private JComponent createOptionRow(@NotNull PatrolRunOption option) {
    if (option.getType() == PatrolRunOptionType.TOGGLE) {
      JBCheckBox checkBox = new JBCheckBox(option.getLabel());
      optionToggleFields.put(option, checkBox);
      return checkBox;
    }
    if (option == PatrolRunOption.DART_DEFINE) {
      dartDefinesField = new DartDefinesField();
      return createFieldPanel(option.getLabel(), dartDefinesField);
    }
    JBTextField field = new JBTextField();
    optionValueFields.put(option, field);
    return createFieldPanel(option.getLabel(), field);
  }

  private JPanel createFieldPanel(@NotNull String label, @NotNull JComponent field) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(JBUI.Borders.emptyBottom(6));
    JBLabel labelComponent = new JBLabel(label);
    labelComponent.setAlignmentX(0f);
    field.setAlignmentX(0f);
    panel.add(labelComponent);
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
      } else if (option == PatrolRunOption.DART_DEFINE) {
        if (dartDefinesField != null) {
          dartDefinesField.setText(DartDefines.normalize(configuration.getOptionValue(option)));
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
      } else if (option == PatrolRunOption.DART_DEFINE) {
        configuration.setOptionValue(option,
            dartDefinesField == null ? "" : DartDefines.normalize(dartDefinesField.getText()));
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
    boolean anyVisible = false;
    for (PatrolRunOption option : PatrolRunOption.values()) {
      JComponent row = optionRows.get(option);
      if (row == null) {
        continue;
      }
      boolean visible = option.appliesTo(mode) && optionEnabled.getOrDefault(option, Boolean.FALSE);
      row.setVisible(visible);
      anyVisible |= visible;
    }
    optionsTopSeparator.setVisible(anyVisible);
    optionsBottomSeparator.setVisible(anyVisible);
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
    panel.setBorder(JBUI.Borders.empty(8, 8, 8, 8));
    addOptionsGroup(panel, mode, "Build",
        List.of(PatrolRunOption.DEBUG, PatrolRunOption.PROFILE, PatrolRunOption.RELEASE));
    addOptionsGroup(panel, mode, "Targets",
        List.of(PatrolRunOption.EXCLUDE, PatrolRunOption.TAGS, PatrolRunOption.EXCLUDE_TAGS));
    addOptionsGroup(panel, mode, "Dart Defines",
        List.of(PatrolRunOption.DART_DEFINE, PatrolRunOption.DART_DEFINE_FILE));
    addOptionsGroup(panel, mode, "Execution", List.of(
        PatrolRunOption.GENERATE_BUNDLE,
        PatrolRunOption.LABEL,
        PatrolRunOption.TEST_SERVER_PORT,
        PatrolRunOption.APP_SERVER_PORT,
        PatrolRunOption.HIDE_TEST_STEPS,
        PatrolRunOption.CLEAR_TEST_STEPS,
        PatrolRunOption.CHECK_COMPATIBILITY,
        PatrolRunOption.UNINSTALL,
        PatrolRunOption.OPEN_DEVTOOLS
    ));
    addOptionsGroup(panel, mode, "Build Metadata", List.of(
        PatrolRunOption.FLAVOR,
        PatrolRunOption.BUILD_NAME,
        PatrolRunOption.BUILD_NUMBER,
        PatrolRunOption.PACKAGE_NAME,
        PatrolRunOption.BUNDLE_ID
    ));
    addOptionsGroup(panel, mode, "Coverage & Logs", List.of(
        PatrolRunOption.COVERAGE,
        PatrolRunOption.COVERAGE_IGNORE,
        PatrolRunOption.COVERAGE_PACKAGE,
        PatrolRunOption.SHOW_FLUTTER_LOGS
    ));
    addOptionsGroup(panel, mode, "iOS Simulator", List.of(
        PatrolRunOption.CLEAR_PERMISSIONS,
        PatrolRunOption.FULL_ISOLATION,
        PatrolRunOption.IOS_VERSION
    ));
    addOptionsGroup(panel, mode, "Web", List.of(
        PatrolRunOption.WEB_RESULTS_DIR,
        PatrolRunOption.WEB_REPORT_DIR,
        PatrolRunOption.WEB_RETRIES,
        PatrolRunOption.WEB_VIDEO,
        PatrolRunOption.WEB_TIMEOUT,
        PatrolRunOption.WEB_WORKERS,
        PatrolRunOption.WEB_REPORTER,
        PatrolRunOption.WEB_LOCALE,
        PatrolRunOption.WEB_TIMEZONE,
        PatrolRunOption.WEB_COLOR_SCHEME,
        PatrolRunOption.WEB_GEOLOCATION,
        PatrolRunOption.WEB_PERMISSIONS,
        PatrolRunOption.WEB_USER_AGENT,
        PatrolRunOption.WEB_VIEWPORT,
        PatrolRunOption.WEB_GLOBAL_TIMEOUT,
        PatrolRunOption.WEB_SHARD,
        PatrolRunOption.WEB_HEADLESS
    ));
    com.intellij.openapi.ui.popup.JBPopup popup = com.intellij.openapi.ui.popup.JBPopupFactory.getInstance()
        .createComponentPopupBuilder(panel, panel)
        .setTitle("Add Run Options")
        .setRequestFocus(true)
        .createPopup();
    popup.showUnderneathOf(modifyOptionsLink);
  }

  private void addOptionsGroup(@NotNull JPanel panel,
                               @NotNull PatrolCommandMode mode,
                               @NotNull String title,
                               @NotNull List<PatrolRunOption> options) {
    CheckBoxList<PatrolRunOption> list = createOptionsList(mode, options);
    if (list == null) {
      return;
    }
    panel.add(createGroupHeader(title));
    panel.add(list);
  }

  private JComponent createGroupHeader(@NotNull String text) {
    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
    header.setBorder(JBUI.Borders.empty(6, 0, 6, 0));
    JBLabel label = new JBLabel(text);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    label.setAlignmentX(0f);
    JSeparator separator = new JSeparator();
    separator.setAlignmentX(0f);
    header.add(label);
    header.add(separator);
    return header;
  }

  private @Nullable CheckBoxList<PatrolRunOption> createOptionsList(@NotNull PatrolCommandMode mode,
                                                                    @NotNull List<PatrolRunOption> options) {
    CheckBoxList<PatrolRunOption> list = new CheckBoxList<>();
    list.setAlignmentX(0f);
    List<PatrolRunOption> visibleOptions = new ArrayList<>();
    for (PatrolRunOption option : options) {
      if (option.appliesTo(mode)) {
        visibleOptions.add(option);
      }
    }
    if (visibleOptions.isEmpty()) {
      return null;
    }
    for (PatrolRunOption option : visibleOptions) {
      list.addItem(option, option.getLabel(), optionEnabled.getOrDefault(option, Boolean.FALSE));
    }
    list.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent event) {
        int index = list.locationToIndex(event.getPoint());
        PatrolRunOption option = index >= 0 ? list.getItemAt(index) : null;
        list.setToolTipText(option == null ? null : option.getDescription());
      }
    });
    list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    list.setCheckBoxListListener((index, value) -> {
      PatrolRunOption option = visibleOptions.get(index);
      optionEnabled.put(option, value);
      if (value) {
        applyDefaultOptionValue(option);
        enforceMutualExclusion(option, list, visibleOptions);
      }
      updateOptionRowsVisibility();
    });
    return list;
  }

  private void enforceMutualExclusion(@NotNull PatrolRunOption selected,
                                      @NotNull CheckBoxList<PatrolRunOption> list,
                                      @NotNull List<PatrolRunOption> options) {
    if (selected != PatrolRunOption.DEBUG
        && selected != PatrolRunOption.PROFILE
        && selected != PatrolRunOption.RELEASE) {
      return;
    }
    for (int i = 0; i < options.size(); i++) {
      PatrolRunOption option = options.get(i);
      if (option == selected) {
        continue;
      }
      if (option == PatrolRunOption.DEBUG || option == PatrolRunOption.PROFILE || option == PatrolRunOption.RELEASE) {
        optionEnabled.put(option, Boolean.FALSE);
        list.setItemSelected(option, false);
      }
    }
  }

  private void applyDefaultOptionValue(@NotNull PatrolRunOption option) {
    if (option.getType() == PatrolRunOptionType.TOGGLE) {
      JBCheckBox checkBox = optionToggleFields.get(option);
      if (checkBox != null && (option == PatrolRunOption.DEBUG
          || option == PatrolRunOption.PROFILE
          || option == PatrolRunOption.RELEASE)) {
        checkBox.setSelected(true);
        return;
      }
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



  /**
   * Editor for {@code --dart-define} values, shaped like the platform's environment variables
   * field: an inline {@code KEY=VALUE;KEY=VALUE} text field that accepts a pasted define list in
   * any common shape, with the same in-field button the environment variables field uses to open a
   * table for structured editing, plus an expand control for one-per-line editing.
   */
  private final class DartDefinesField extends ExpandableTextField {
    private DartDefinesField() {
      super(DartDefines::split, values -> String.join(";", values));
      setToolTipText(DART_DEFINES_TOOLTIP);
      // Same icon pair the platform's EnvironmentVariablesTextFieldWithBrowseButton uses.
      // NOTE: the create() overload taking a trailing boolean only exists from 2025 onwards;
      // this four-argument one is present across the whole supported range.
      addExtension(ExtendableTextComponent.Extension.create(
          AllIcons.General.OpenDisk,
          AllIcons.General.OpenDiskHover,
          "Edit Dart defines in a table",
          this::editInTable));
      // Tidy a pasted list as soon as focus leaves, so it is obvious the paste was understood.
      addFocusListener(new java.awt.event.FocusAdapter() {
        @Override
        public void focusLost(java.awt.event.FocusEvent event) {
          String normalized = DartDefines.normalize(getText());
          if (!normalized.equals(getText())) {
            setText(normalized);
          }
        }
      });
    }

    private void editInTable() {
      KeyValueTablePanel table = new KeyValueTablePanel();
      table.setPairs(DartDefines.parse(getText()));
      table.setPreferredSize(new Dimension(JBUI.scale(620), JBUI.scale(320)));

      DialogWrapper dialog = new DialogWrapper(project, true) {
        {
          setTitle("Dart Defines");
          init();
        }

        @Override
        protected JComponent createCenterPanel() {
          return table;
        }

        @Override
        protected String getDimensionServiceKey() {
          return "patrol.dartDefines.dialog";
        }
      };

      if (dialog.showAndGet()) {
        setText(DartDefines.format(table.getPairs()));
      }
    }
  }

  private static final class KeyValueTablePanel extends JPanel {
    private final ListTableModel<KeyValueRow> model;
    private final JBTable table;

    private KeyValueTablePanel() {
      ColumnInfo<KeyValueRow, String> keyColumn = new ColumnInfo<>("Key") {
        @Override
        public String valueOf(KeyValueRow row) {
          return row.key;
        }

        @Override
        public void setValue(KeyValueRow row, String value) {
          row.key = value == null ? "" : value;
        }

        @Override
        public boolean isCellEditable(KeyValueRow row) {
          return true;
        }
      };
      ColumnInfo<KeyValueRow, String> valueColumn = new ColumnInfo<>("Value") {
        @Override
        public String valueOf(KeyValueRow row) {
          return row.value;
        }

        @Override
        public void setValue(KeyValueRow row, String value) {
          row.value = value == null ? "" : value;
        }

        @Override
        public boolean isCellEditable(KeyValueRow row) {
          return true;
        }
      };
      model = new ListTableModel<>(keyColumn, valueColumn);
      table = new JBTable(model);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

      ToolbarDecorator decorator = ToolbarDecorator.createDecorator(table)
          .disableUpDownActions()
          .setAddAction(event -> addRow())
          .setRemoveAction(event -> removeSelectedRow());

      setLayout(new BorderLayout());
      add(decorator.createPanel(), BorderLayout.CENTER);
    }

    private void addRow() {
      model.addRow(new KeyValueRow("", ""));
      int index = model.getRowCount() - 1;
      if (index >= 0) {
        table.setRowSelectionInterval(index, index);
        table.editCellAt(index, 0);
      }
    }

    private void removeSelectedRow() {
      int index = table.getSelectedRow();
      if (index >= 0) {
        model.removeRow(index);
      }
    }

    private void setPairs(@NotNull Map<String, String> pairs) {
      List<KeyValueRow> rows = new ArrayList<>();
      for (Map.Entry<String, String> entry : pairs.entrySet()) {
        rows.add(new KeyValueRow(entry.getKey(), entry.getValue()));
      }
      model.setItems(rows);
    }

    private @NotNull Map<String, String> getPairs() {
      Map<String, String> result = new LinkedHashMap<>();
      for (KeyValueRow row : model.getItems()) {
        if (row.key.isEmpty() && row.value.isEmpty()) {
          continue;
        }
        result.put(row.key, row.value);
      }
      return result;
    }
  }

  private static final class KeyValueRow {
    private String key;
    private String value;

    private KeyValueRow(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
