# Patrol Flutter JetBrains Plugin

Run and debug Patrol Flutter tests directly from JetBrains IDEs.

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

### Option 1: Run via Maven goal

```bash
mvn org.jetbrains.intellij.plugins:intellij-maven-plugin:run
```

This launches a sandboxed IntelliJ instance with the plugin installed.

### Option 2: Run via IntelliJ Run Configuration

1. Open the project in IntelliJ.
2. Ensure the IntelliJ Platform Plugin SDK is available.
3. Create a new **Maven** Run Configuration:
   - Command line: `intellij:run`
4. Run the configuration to launch the IDE sandbox.

## Notes
- The plugin depends on the Flutter plugin (`io.flutter`). Make sure it is available in the target IDE.
- The Patrol CLI must be on `PATH` or set explicitly in the run configuration.

## Development
- Main requirements and progress tracking:
  - `REQUIREMENTS.md`
  - `CHECKLIST.md`
