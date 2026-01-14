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
    - Custom Patrol CLI arguments
    - Environment variables
    - Working directory override

- **Gutter Icons**: Display run/debug icons in the editor gutter next to:
  - Individual `patrolTest()` declarations
  - Test groups
  - Test files

- **Run Actions**: 
  - Run single test
  - Run all tests in file
  - Run all tests in directory
  - Debug mode support with breakpoints

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

### 3. Test Output & Results
- **Console Integration**: Stream Patrol test output to Run tool window
- **Test Results UI**: Display test results in JetBrains test runner format
  - Pass/fail status with visual indicators
  - Test hierarchy (suites → groups → individual tests)
  - Execution time per test
  - Failure messages and stack traces
  - Click-to-navigate to failing test code

### 4. Project Detection & Setup
- **Auto-detection**: Identify Patrol-enabled Flutter projects by:
  - Presence of `patrol` dependency in `pubspec.yaml`
  - Existence of `integration_test/` or test directories with Patrol tests
- **Setup Wizard**: Guide users through:
  - Installing Patrol CLI if not present
  - Configuring patrol in `pubspec.yaml`
  - Setting up test directories
- **Validation**: Check for required dependencies and configuration

## Non-Functional Requirements

### Performance
- Hot reload trigger latency < 500ms after file save
- Test discovery completes within 2 seconds for projects with < 100 test files
- UI remains responsive during test execution

### Compatibility
- IntelliJ IDEA 2023.1+
- Android Studio Flamingo (2022.2.1)+
- Flutter SDK 3.0+
- Patrol 2.0+

### Usability
- Zero-configuration for standard Patrol projects
- Familiar UX matching existing Flutter plugin patterns
- Clear error messages with actionable guidance
- Keyboard shortcuts consistent with JetBrains conventions

## Technical Constraints
- Must integrate with existing Flutter plugin (not replace it)
- Use Patrol CLI as the underlying test runner
- Support both native automation backends (iOS/Android)
- Handle multiple simultaneous test sessions

## Success Metrics
- Developers can run Patrol tests without switching to terminal
- Hot reload works reliably during test sessions
- Test results are clearly visible and actionable
- Setup time < 5 minutes for new projects

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
