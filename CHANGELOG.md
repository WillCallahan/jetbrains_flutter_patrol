# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.0] - 2026-10-13

### Changed
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
- Replaced the deprecated `ProcessAdapter` with the `ProcessListener` interface in diagnostic logging.
- The "Hot Reload" toolbar action now declares `getActionUpdateThread()`, which the platform requires
  from any action that overrides `update()`.

### Known deprecations (intentionally retained)
- `TextFieldWithBrowseButton.addBrowseFolderListener(String, String, Project, FileChooserDescriptor)`
  is deprecated and marked for removal on 2026.2, but its replacement
  `addBrowseFolderListener(Project, FileChooserDescriptor)` does not exist before 2024.3.
  Verified against 2024.2.6: switching to the replacement raises `NoSuchMethodError`.
  The deprecated overload is kept so that one build runs across the whole 242-262 range;
  migrate it when `since-build` is raised to 243 or later.
- `RunConfigurationProducer(ConfigurationType)` is deprecated in favour of
  `LazyRunConfigurationProducer`. Left as-is; migrating changes which factory backs each
  producer and was out of scope for this compatibility update.

### Added
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
  `IC-242.26775.15` (2024.2.6) and `IU-262.10968.63` (2026.2.3).

## [0.1.1] - 2026-01-16

Versions up to and including 0.1.1 predate this changelog; see the git history for details.

[Unreleased]: https://github.com/WillCallahan/jetbrains_flutter_patrol/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/WillCallahan/jetbrains_flutter_patrol/releases/tag/v0.2.0
[0.1.1]: https://github.com/WillCallahan/jetbrains_flutter_patrol/releases/tag/v0.1.1
