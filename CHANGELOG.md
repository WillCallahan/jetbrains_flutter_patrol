# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.1] - 2026-10-14

### Changed
- **Removed the Device override field. The device now always comes from the IDE's Flutter device
  selector**, the dropdown to the left of the run configuration. The Flutter plugin does not
  contribute platform execution targets for devices, so the previous code fell back to the
  execution target's display name, which is a label such as `macOS (desktop)` rather than the id
  Patrol expects (`macos`). The selected device is now read from the Flutter plugin's
  `DeviceService`, which returns the real device id. If no device is selected, the run reports that
  instead of failing obscurely later.
- Build against IntelliJ Platform **2026.2.3** (build `262.10968.63`), up from 2023.1.7.
- Raised the minimum supported IDE to **2024.2** (`since-build=242`), up from 2023.1 (`231`).
  This is the first build that ships a Java 21 runtime, which the plugin now requires.
- Compile with **Java 21** (`maven.compiler.release`), up from Java 11. The emitted bytecode
  stays at Java 21 so that one build runs across the whole 242-262 range.
- Building now requires **JDK 25 or newer**, up from JDK 11. The 2026.2 platform jars are
  Java 25 bytecode (class file version 69), which javac must be able to read; JDK 21 fails
  with `class file has wrong version 69.0, should be 65.0`. This is a build-time requirement
  only and does not affect which IDEs the plugin runs on.

### Fixed
- **A run configuration created by hand ran the Patrol CLI from the wrong directory.** Only the
  gutter and context-menu producers set a working directory, so a configuration made via
  *Run > Edit Configurations* left it blank and the CLI inherited the IDE's working directory,
  which is not the project root when the IDE is launched from Finder or the Dock. The working
  directory now falls back to the project root. This surfaced as a confusing
  `FormatException: Unexpected character` from deep inside `patrol_cli`, because version managers
  such as fvm resolve the SDK from `.fvmrc` in the project root and, when they cannot, exit 0
  having printed nothing, which the CLI then tried to parse as JSON.
- **Dart define values containing a comma were silently torn into separate defines.** The launcher
  split every multi-value option on `,`, `;` and newline, so `--dart-define LIST=a,b,c` became three
  broken arguments. Dart defines now split only on `;` and newline; `--exclude` and
  `--coverage-ignore`, which are genuinely comma-separated, are unchanged.
- Replaced the deprecated `ProcessAdapter` with the `ProcessListener` interface in diagnostic logging.
- The "Hot Reload" toolbar action now declares `getActionUpdateThread()`, which the platform requires
  from any action that overrides `update()`.

- Cleared the plugin verifier warnings:
  - Dropped `TextFieldWithBrowseButton.addBrowseFolderListener(String, String, Project, FileChooserDescriptor)`,
    which is scheduled for removal. Its direct replacement does not exist before 2024.3, so rather
    than raise `since-build` the browse buttons now open the chooser explicitly via
    `FileChooser.chooseFile`, with the title and description on the `FileChooserDescriptor`.
    Every API involved exists in both 242 and 262.
  - Migrated both run configuration producers from the deprecated
    `RunConfigurationProducer(ConfigurationType)` to `LazyRunConfigurationProducer`. Each producer
    now returns the factory matching its own command mode, so a context-created Develop
    configuration is backed by the Develop factory instead of the Test one.

### Known deprecations (intentionally retained)
- `EnvironmentVariablesComponent()` is deprecated on 2026.2 in favour of the constructor taking a
  `Project`, which does not exist on 2024.2. Keeping the no-arg constructor is what allows one
  build to span 242-262; migrate it when `since-build` is raised past 242.

### Added
- **Dart defines are now a paste-friendly field** shaped like the platform's environment variables
  component: an inline `KEY=VALUE;KEY=VALUE` text field, an expand control for one-per-line
  editing, and a browse button opening a table. A whole define list can be pasted in any of the
  shapes people actually have on hand and is normalised automatically:
  `--dart-define A=1 --dart-define B=2`, `--dart-define=A=1`, `A=1;B=2`, or one `KEY=VALUE`
  per line. Quoted values keep their spaces. The stored format is unchanged, so existing run
  configurations keep working.
- **Test target is now a dropdown.** The run configuration editor lists every
  `*patrol_test.dart` file it finds under the Patrol test directory (from `pubspec.yaml`),
  `integration_test/` and `test/`, so the target no longer has to be typed by hand.
  The field stays editable for targets discovery misses, and paths are shown relative to the
  project root while still being stored as absolute paths, so existing run configurations and
  the gutter-icon matching are unaffected. The scan skips `build/`, `.dart_tool/`, platform
  folders and dot-directories, and is bounded in depth and result count.
- **Tooltips on every run configuration field**, including an explanation of when to use
  `Test` (single run, CI) versus `Develop` (watch mode with hot reload).
- `.github/workflows/release.yml` — tag-triggered GitHub Actions build that publishes an
  installable plugin ZIP and JAR to a GitHub release, and can also be run manually
  via `workflow_dispatch`.
- This changelog.

### Verified
- IntelliJ Plugin Verifier 1.410 reports **Compatible** at both ends of the declared range:
  `IC-242.26775.15` (2024.2.6) with no warnings at all, and `IU-262.10968.63` (2026.2.3) with a
  single deprecation that cannot be resolved without dropping 2024.2 support.

## [0.2.0] - not released

Tagged, but the release build failed before publishing any artifact and no release was created.
Everything intended for 0.2.0 ships in 0.2.1.

## [0.1.1] - 2026-01-16

Versions up to and including 0.1.1 predate this changelog; see the git history for details.

[Unreleased]: https://github.com/WillCallahan/jetbrains_flutter_patrol/compare/v0.2.1...HEAD
[0.2.1]: https://github.com/WillCallahan/jetbrains_flutter_patrol/releases/tag/v0.2.1
[0.2.0]: https://github.com/WillCallahan/jetbrains_flutter_patrol/releases/tag/v0.2.0
[0.1.1]: https://github.com/WillCallahan/jetbrains_flutter_patrol/releases/tag/v0.1.1
