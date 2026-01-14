# Product Requirements: Patrol Flutter JetBrains Plugin

## Core Objectives
Enable developers to run and debug Patrol Flutter tests directly from their IDE with the same convenience as standard Flutter applications.

## Functional Requirements

### 1. Test Execution via Run Button
- **Run Configuration**: Custom run configuration type for Patrol tests
  - Detect `patrol_test.dart` files in the project
  - Support running individual test files or test suites
  - Allow target device selection (iOS simulator, Android emulator, physical devices)
  - Configuration options for:
    - Test target specification
    - Command mode: `develop` (watch for changes) or `test` (single run)
    - Custom Patrol CLI arguments
    - Environment variables
    - Working directory override
    - Display detected Patrol CLI version when CLI path is not set

- **Gutter Icons**: Display run/debug icons in the editor gutter next to:
  - Individual `patrolTest()` declarations
  - Test groups
  - Test files
  - Use the Patrol icon from https://patrol.leancode.co/_next/static/media/patrol_icon.94a710ce.svg

- **Run Actions**: 
  - Run single test
  - Run all tests in file
  - Run all tests in directory
  - Debug mode support with breakpoints

### 1.1 Test Discovery Rules
- **Default Discovery**:
  - Files matching `**/*patrol_test.dart` under `integration_test/` and `test/`
  - Ignore `build/`, `.dart_tool/`, and hidden directories by default
- **Override**: Allow include/exclude globs per run configuration
- **Indexing**: Discovery uses IDE indexes; fallback to filesystem scan if index is stale

### 1.2 Device Management
- **Default Device**: Select last used device; if none, choose first available emulator/simulator
- **Unavailable Devices**: Surface a clear error and link to device manager
- **Concurrency**: Allow one device per test session; explicitly block multi-device runs for now

### 1.3 Run/Debug Lifecycle
- **Stop/Cancel**: Terminate Patrol CLI, detach from device, and clean up temp artifacts
- **Rerun**: Support rerun last test/session from Run tool window
- **Timeouts**: Configurable overall timeout per run configuration
- **Crash Recovery**: If the Patrol process crashes, surface a concise error and offer "Rerun"

### 2. Hot Reload Integration
- **File Watcher**: Monitor Dart file changes in the project
- **Automatic Hot Reload**: Trigger Patrol hot reload when:
  - User saves a file (configurable)
  - File changes are detected during active test session
- **Manual Hot Reload**: Keyboard shortcut and toolbar button
- **Reload Feedback**: Display reload status in:
  - Run tool window console
  - Status bar notification
  - Show reload time and success/failure state
 - **Failure Handling**: On reload failure, show reason and keep session running when possible

### 3. Test Output & Results
- **Console Integration**: Stream Patrol test output to Run tool window
- **Test Results UI**: Display test results in JetBrains test runner format
  - Pass/fail status with visual indicators
  - Test hierarchy (suites → groups → individual tests)
  - Execution time per test
  - Failure messages and stack traces
  - Click-to-navigate to failing test code
 - **Parsing Fallback**: If output cannot be parsed, show raw logs with a warning

### 4. Project Detection & Setup
- **Auto-detection**: Identify Patrol-enabled Flutter projects by:
  - Presence of `patrol` dependency in `pubspec.yaml`
  - Existence of `integration_test/` or test directories with Patrol tests
- **Setup Wizard**: Guide users through:
  - Installing Patrol CLI if not present
  - Configuring patrol in `pubspec.yaml`
  - Setting up test directories
- **Validation**: Check for required dependencies and configuration
- **Version Checks**: Detect Patrol CLI and Flutter SDK versions at setup time

## Non-Functional Requirements

### Performance
- Hot reload trigger latency < 500ms after file save
- Test discovery completes within 2 seconds for projects with < 100 test files
- UI remains responsive during test execution
- File watcher CPU usage < 3% on idle projects

### Compatibility
- IntelliJ IDEA 2023.1+
- Android Studio Flamingo (2022.2.1)+
- Flutter SDK 3.0+
- Patrol 2.0+
- **Compatibility Matrix**:
  - Warn on unsupported Patrol CLI versions
  - Provide a link to upgrade instructions

### Usability
- Zero-configuration for standard Patrol projects
- Familiar UX matching existing Flutter plugin patterns
- Clear error messages with actionable guidance
- Keyboard shortcuts consistent with JetBrains conventions
- Single-click access to device selection and Patrol run configs

### Security & Privacy
- Do not collect telemetry by default
- Do not log environment variables unless explicitly enabled
- Redact device identifiers in logs when displayed in UI

### Reliability & Error Handling
- Clearly classify errors (configuration, device, build, runtime)
- Provide recovery actions for common failures (install CLI, start device, fix SDK)

## Technical Constraints
- Must integrate with existing Flutter plugin (not replace it)
- Use Patrol CLI as the underlying test runner
- Support both native automation backends (iOS/Android)
- Handle multiple simultaneous test sessions

## Technical Requirements

### Build & Project Structure
- Use Maven with a `pom.xml`
- Standard JetBrains plugin layout:
  - `src/main/java` for plugin sources
  - `src/main/resources` for plugin resources
  - `src/test/java` for unit tests
- Keep plugin configuration in `src/main/resources/META-INF/plugin.xml`

### JetBrains Platform
- Target IntelliJ Platform 2023.1+
- Use the IntelliJ Platform Plugin SDK and follow recommended extension points
- No direct replacement of Flutter plugin services; integrate via extension points only

### CLI Integration
- Resolve Patrol CLI path via:
  1. Project settings override
  2. PATH environment
  3. Fallback detection with a clear error if missing
- Capture stdout/stderr separately for structured parsing
- Support configurable working directory per run configuration
- Show detected Patrol CLI version in the run configuration UI when not overridden

### Testing
- Unit tests for parsing Patrol output into the test runner model
- Integration tests for run configuration creation and execution lifecycle
- Mockable interfaces around CLI invocation for deterministic tests

### Logging & Diagnostics
- Use JetBrains logging APIs
- Provide a diagnostic mode that increases log verbosity
- Ensure logs avoid sensitive values by default
- Diagnostic mode must never log environment variables
- Diagnostic mode should log the resolved CLI path, working directory, and exit code

### Repository Hygiene
- Provide a `.gitignore` that excludes:
  - IDE metadata (`.idea/`, `*.iml`)
  - Build output (`target/`, `out/`)
  - JetBrains sandbox/system dirs (`idea-sandbox/`, `system/`)
  - OS artifacts (`.DS_Store`, `Thumbs.db`)

### Compatibility & Upgrades
- Detect incompatible Patrol CLI versions and warn with upgrade guidance
- Backward-compatible handling of CLI output changes where feasible

### Branding
- Use the Patrol icon for the plugin metadata displayed in IntelliJ

## Success Metrics
- Developers can run Patrol tests without switching to terminal
- Hot reload works reliably during test sessions
- Test results are clearly visible and actionable
- Setup time < 5 minutes for new projects
- Reload success rate >= 95% in stable sessions
- 90% of first-time runs succeed without manual CLI invocation

## Out of Scope (Future Considerations)
- Visual test recording/playback
- Test generation from UI interactions
- Custom test report formats
- CI/CD pipeline integration
- Code coverage visualization

## Implementation Language

**Recommendation: Java**

### Rationale:
- **Broader compatibility**: Java plugins work across all JetBrains IDEs without Kotlin runtime dependencies
- **Simpler build setup**: No Kotlin compiler configuration needed
- **Easier debugging**: More straightforward for developers unfamiliar with Kotlin
- **JetBrains SDK examples**: Most official documentation uses Java
- **Your preference**: Aligns with your stated lean towards Java

### Trade-offs:
- More verbose than Kotlin (but manageable for plugin development)
- Kotlin would offer null-safety and modern language features
- However, Java 11+ provides sufficient modern features for this use case

**Decision: Use Java for this plugin.**
