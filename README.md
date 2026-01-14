# Patrol Flutter JetBrains Plugin

Run and debug Patrol Flutter tests directly from JetBrains IDEs, with a fast feedback loop built for Patrol.

## Highlights
- Run Patrol tests from the gutter or Run tool window
- Two modes: `test` for single runs and `develop` for watch/reload workflows
- Device-aware run configuration with CLI arguments, env vars, and working directory
- Rich output in the Run tool window with structured diagnostics

## Requirements
- IntelliJ IDEA 2023.1+ or Android Studio Flamingo+
- JDK 11
- Maven 3.8+
- Flutter SDK 3.0+
- Patrol CLI 2.0+

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
4. Select `target/jetbrains-flutter-patrol-0.1.0-SNAPSHOT.jar` and restart the IDE.

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
- **Test target**: Dart file or test target path
- **Command**: `Test` or `Develop`
- **Patrol CLI args**: additional CLI arguments
- **Working directory**: optional override
- **Patrol CLI path**: optional override for CLI resolution
- **Environment**: custom environment variables
- **Diagnostic logging**: verbose logs without env var output

## Notes
- The plugin depends on the Flutter plugin (`io.flutter`). Make sure it is available in the target IDE.
- The Patrol CLI must be on `PATH` or set explicitly in the run configuration.

## Development
- Main requirements and progress tracking:
  - `REQUIREMENTS.md`
  - `CHECKLIST.md`
