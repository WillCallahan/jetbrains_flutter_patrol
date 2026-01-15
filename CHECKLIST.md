# Requirements Implementation Checklist

Legend: [x] done, [ ] pending, [~] partial

## Functional Requirements
- [~] Run configuration type for Patrol tests
- [~] Detect `patrol_test.dart` files
- [ ] Run single test / file / directory
- [ ] Debug mode with breakpoints
- [~] Gutter icons for patrolTest(), groups, files
- [x] Command mode: develop (watch) and test (single run)
- [x] Device selection passed to Patrol CLI
- [x] Modify options for Patrol CLI flags
- [ ] Hot reload integration (auto + manual)
- [ ] Reload status feedback (console, status bar)
- [ ] Test results in JetBrains test runner format
- [ ] Click-to-navigate for failures
- [~] Project auto-detection
- [ ] Setup wizard
- [ ] Version checks at setup

## Test Discovery Rules
- [ ] Default discovery globs under `integration_test/` and `test/`
- [ ] Ignore build/tool/hidden dirs
- [ ] Include/exclude globs per run configuration
- [ ] Indexing fallback to filesystem scan

## Device Management
- [ ] Default device selection
- [ ] Unavailable device error + device manager link
- [ ] One device per session (explicitly block multi-device)

## Run/Debug Lifecycle
- [ ] Stop/cancel with cleanup
- [x] Single active Patrol run/debug instance
- [ ] Rerun last test/session
- [ ] Per-run timeout
- [ ] Crash recovery with rerun prompt

## Output & Results
- [ ] Console streaming
- [ ] Parsing fallback to raw logs
- [ ] Structured pass/fail hierarchy

## Non-Functional
- [ ] Hot reload latency < 500ms
- [ ] Test discovery < 2s for < 100 files
- [ ] File watcher idle CPU < 3%
- [ ] UI remains responsive

## Compatibility
- [ ] IntelliJ 2023.1+ enforced
- [ ] Android Studio Flamingo+
- [ ] Flutter SDK 3.0+
- [ ] Patrol 2.0+
- [~] CLI version warning + upgrade link

## Security & Privacy
- [ ] No telemetry by default
- [ ] No env var logging by default
- [ ] Redact device identifiers in UI logs

## Technical Requirements
- [x] Maven `pom.xml` and standard source layout
- [x] `plugin.xml` in `src/main/resources/META-INF`
- [x] CLI path resolution (override + PATH)
- [x] IDE settings (default CLI path + test root configuration)
- [x] Project-specific CLI override
- [x] Pubspec `patrol.test_directory` warning in settings
- [x] Settings grouped under Languages & Frameworks
- [ ] CLI stdout/stderr separation
- [x] Configurable working directory
- [x] JetBrains logging APIs
- [~] Diagnostic mode for verbose logs
- [x] Show detected Patrol CLI version in UI
- [ ] Unit tests for output parsing
- [ ] Integration tests for run configuration lifecycle
- [x] Plugin icon metadata
- [x] `patrol_test/` folder uses test folder icon

## Repository Hygiene
- [x] `.gitignore` covering IDE/build/sandbox/OS artifacts
