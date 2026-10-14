# Patrol Flutter JetBrains Plugin

Run and debug Patrol Flutter tests directly from JetBrains IDEs, with a fast feedback loop built for Patrol.

## Highlights
- Run Patrol tests from the gutter or Run tool window
- Two modes: `test` for single runs and `develop` for watch/reload workflows
- Device-aware run configuration with CLI arguments, env vars, and working directory
- Rich output in the Run tool window with structured diagnostics

## Requirements
- IntelliJ IDEA 2024.2+ (build 242) through 2026.2 (build 262), or a matching Android Studio
- JDK 25 to build (the IDE supplies its own runtime at install time)
- Maven 3.8+
- Flutter SDK 3.0+
- Patrol CLI 2.0+

The plugin is built against IntelliJ Platform 2026.2.3 and verified with the
IntelliJ Plugin Verifier at both ends of that range. See `CHANGELOG.md` for details.

Note the two different Java versions: the 2026.2 platform jars are Java 25 bytecode, so
**javac must be JDK 25 or newer to read them**. The plugin itself is still compiled to
Java 21 (`maven.compiler.release`), which is what allows a single build to run on every
IDE from 2024.2 through 2026.2. Building with JDK 21 fails with
`class file has wrong version 69.0, should be 65.0`.

## Project Layout
- `src/main/java` - plugin sources
- `src/main/resources` - plugin resources
- `src/main/resources/META-INF/plugin.xml` - plugin descriptor
- `src/test/java` - unit tests

## Build
From the project root:

```bash
mvn package
```

This produces the plugin JAR in `target/`.

## Run in IntelliJ (Plugin Sandbox)

1. Open the project in IntelliJ.
2. Add an IntelliJ Platform SDK:
   - **File → Project Structure → SDKs → + → IntelliJ Platform Plugin SDK**
3. Create a **Plugin** Run Configuration:
   - **Run → Edit Configurations → + → Plugin**
4. Run the configuration to launch the IDE sandbox.

## Install Locally in IntelliJ

1. Build the plugin:
   ```bash
   mvn package
   ```
2. In IntelliJ, go to **Settings → Plugins**.
3. Click the gear icon, then **Install Plugin from Disk...**.
4. Select `target/jetbrains-flutter-patrol-<version>.jar` and restart the IDE.

Alternatively, download the `patrol-flutter-<version>.zip` attached to a
[GitHub release](https://github.com/WillCallahan/jetbrains_flutter_patrol/releases)
and install that from disk.

## Usage

### Run Patrol Test (single run)
- Open a `patrol_test.dart` file.
- Click the gutter icon or create a **Patrol Test** run configuration.
- Set **Command** to `Test`.

### Develop (watch for changes)
- Open a `patrol_test.dart` file.
- Create a **Patrol Test** run configuration.
- Set **Command** to `Develop` to run and watch for file changes.

## Configuration Fields
Every field has a tooltip explaining what it does; hover the field in the run configuration editor.

- **Test target**: dropdown of discovered `*patrol_test.dart` files, searched under the
  `patrol.test_directory` from `pubspec.yaml`, then `integration_test/` and `test/`.
  Paths are shown relative to the project root. The field is editable, so you can type or paste
  any other path; a relative path is resolved against the project root.
- **Command**: `Test` or `Develop`
- **Patrol CLI args**: additional CLI arguments
- **Working directory**: optional override
- **Patrol CLI path**: optional override for CLI resolution
- **Environment**: custom environment variables
- **Diagnostic logging**: verbose logs without env var output

## Notes
- The plugin depends on the Flutter plugin (`io.flutter`). Make sure it is available in the target IDE.
- The Patrol CLI must be on `PATH` or set explicitly in the run configuration.

## Releasing

Releases are produced by `.github/workflows/release.yml`.

Pushing a `v*` tag builds the plugin, stamps the version into `pom.xml` and `plugin.xml`,
and publishes a GitHub release with two attachments:

- `patrol-flutter-<version>.zip` - plugin directory layout (`Patrol Flutter/lib/<jar>`)
- `patrol-flutter-<version>.jar` - the plain plugin JAR

```bash
git tag -a v0.2.0 -m "Patrol Flutter 0.2.0"
git push origin v0.2.0
```

The workflow can also be run manually from the Actions tab (`workflow_dispatch`), which
builds and uploads the same files as workflow artifacts without creating a release.

## Development
- Changelog: `CHANGELOG.md`
- Main requirements and progress tracking:
  - `REQUIREMENTS.md`
  - `CHECKLIST.md`
