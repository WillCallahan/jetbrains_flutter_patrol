package com.patrol.jetbrains.run;

import java.util.EnumSet;

public enum PatrolRunOption {
  EXCLUDE("exclude", "Exclude targets", "--exclude", PatrolRunOptionType.VALUE, false, null,
      "Exclude integration test targets from the run (repeatable).",
      EnumSet.allOf(PatrolCommandMode.class)),
  GENERATE_BUNDLE("generateBundle", "Generate bundle", "--generate-bundle", PatrolRunOptionType.TOGGLE, true, true,
      "Generate a bundled Dart test file before running.",
      EnumSet.allOf(PatrolCommandMode.class)),
  DEBUG("debug", "Debug build", "--debug", PatrolRunOptionType.TOGGLE, true, true,
      "Build a debug version of the app.",
      EnumSet.allOf(PatrolCommandMode.class)),
  PROFILE("profile", "Profile build", "--profile", PatrolRunOptionType.TOGGLE, true, false,
      "Build a profile version of the app.",
      EnumSet.allOf(PatrolCommandMode.class)),
  RELEASE("release", "Release build", "--release", PatrolRunOptionType.TOGGLE, true, false,
      "Build a release version of the app.",
      EnumSet.allOf(PatrolCommandMode.class)),
  FLAVOR("flavor", "Flavor", "--flavor", PatrolRunOptionType.VALUE, false, null,
      "Flutter flavor to run.",
      EnumSet.allOf(PatrolCommandMode.class)),
  DART_DEFINE("dartDefine", "Dart define (KEY=VALUE)", "--dart-define", PatrolRunOptionType.VALUE, false, null,
      "Define a compile-time environment variable (repeatable).",
      EnumSet.allOf(PatrolCommandMode.class)),
  DART_DEFINE_FILE("dartDefineFile", "Dart define from file", "--dart-define-from-file", PatrolRunOptionType.VALUE, false, null,
      "Load Dart defines from a JSON file.",
      EnumSet.allOf(PatrolCommandMode.class)),
  LABEL("label", "Show label", "--label", PatrolRunOptionType.TOGGLE, true, true,
      "Show the label overlay on the app under test.",
      EnumSet.allOf(PatrolCommandMode.class)),
  TEST_SERVER_PORT("testServerPort", "Test server port", "--test-server-port", PatrolRunOptionType.VALUE, false, null,
      "Port for the test instrumentation server.",
      EnumSet.allOf(PatrolCommandMode.class)),
  APP_SERVER_PORT("appServerPort", "App server port", "--app-server-port", PatrolRunOptionType.VALUE, false, null,
      "Port for the app-under-test server.",
      EnumSet.allOf(PatrolCommandMode.class)),
  TAGS("tags", "Tags", "--tags", PatrolRunOptionType.VALUE, false, null,
      "Run tests matching the given tags.",
      EnumSet.allOf(PatrolCommandMode.class)),
  EXCLUDE_TAGS("excludeTags", "Exclude tags", "--exclude-tags", PatrolRunOptionType.VALUE, false, null,
      "Exclude tests matching the given tags.",
      EnumSet.of(PatrolCommandMode.TEST)),
  HIDE_TEST_STEPS("hideTestSteps", "Hide test steps", "--hide-test-steps", PatrolRunOptionType.TOGGLE, true, false,
      "Hide step list while tests run.",
      EnumSet.allOf(PatrolCommandMode.class)),
  CLEAR_TEST_STEPS("clearTestSteps", "Clear test steps", "--clear-test-steps", PatrolRunOptionType.TOGGLE, true, true,
      "Clear step list after tests complete.",
      EnumSet.allOf(PatrolCommandMode.class)),
  CHECK_COMPATIBILITY("checkCompatibility", "Check compatibility", "--check-compatibility", PatrolRunOptionType.TOGGLE, true, true,
      "Verify dependencies are compatible.",
      EnumSet.allOf(PatrolCommandMode.class)),
  UNINSTALL("uninstall", "Uninstall before/after", "--uninstall", PatrolRunOptionType.TOGGLE, true, true,
      "Uninstall the app before and after tests.",
      EnumSet.allOf(PatrolCommandMode.class)),
  BUILD_NAME("buildName", "Build name", "--build-name", PatrolRunOptionType.VALUE, false, null,
      "Version name for the app.",
      EnumSet.allOf(PatrolCommandMode.class)),
  BUILD_NUMBER("buildNumber", "Build number", "--build-number", PatrolRunOptionType.VALUE, false, null,
      "Version code for the app.",
      EnumSet.allOf(PatrolCommandMode.class)),
  PACKAGE_NAME("packageName", "Android package name", "--package-name", PatrolRunOptionType.VALUE, false, null,
      "Android package name override.",
      EnumSet.allOf(PatrolCommandMode.class)),
  BUNDLE_ID("bundleId", "iOS bundle ID", "--bundle-id", PatrolRunOptionType.VALUE, false, null,
      "iOS bundle identifier override.",
      EnumSet.allOf(PatrolCommandMode.class)),
  CLEAR_PERMISSIONS("clearPermissions", "Clear iOS permissions", "--clear-permissions", PatrolRunOptionType.TOGGLE, false, false,
      "Clear iOS protected resource permissions.",
      EnumSet.allOf(PatrolCommandMode.class)),
  FULL_ISOLATION("fullIsolation", "Full isolation (iOS Simulator)", "--full-isolation", PatrolRunOptionType.TOGGLE, false, false,
      "Uninstall between runs on iOS Simulator.",
      EnumSet.allOf(PatrolCommandMode.class)),
  IOS_VERSION("iosVersion", "iOS version", "--ios", PatrolRunOptionType.VALUE, false, null,
      "iOS simulator version (defaults to latest).",
      EnumSet.allOf(PatrolCommandMode.class)),
  OPEN_DEVTOOLS("openDevtools", "Open DevTools", "--open-devtools", PatrolRunOptionType.TOGGLE, true, false,
      "Open Patrol DevTools when ready.",
      EnumSet.of(PatrolCommandMode.DEVELOP)),
  COVERAGE("coverage", "Coverage", "--coverage", PatrolRunOptionType.TOGGLE, true, false,
      "Collect coverage while running tests.",
      EnumSet.of(PatrolCommandMode.TEST)),
  COVERAGE_IGNORE("coverageIgnore", "Coverage ignore globs", "--coverage-ignore", PatrolRunOptionType.VALUE, false, null,
      "Exclude files from coverage using globs (repeatable).",
      EnumSet.of(PatrolCommandMode.TEST)),
  COVERAGE_PACKAGE("coveragePackage", "Coverage package regex", "--coverage-package", PatrolRunOptionType.VALUE, false, null,
      "Regex of packages to include in coverage.",
      EnumSet.of(PatrolCommandMode.TEST)),
  SHOW_FLUTTER_LOGS("showFlutterLogs", "Show Flutter logs", "--show-flutter-logs", PatrolRunOptionType.TOGGLE, true, false,
      "Show Flutter logs in the console.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_RESULTS_DIR("webResultsDir", "Web results dir", "--web-results-dir", PatrolRunOptionType.VALUE, false, null,
      "Directory for web test results.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_REPORT_DIR("webReportDir", "Web report dir", "--web-report-dir", PatrolRunOptionType.VALUE, false, null,
      "Directory for web test reports.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_RETRIES("webRetries", "Web retries", "--web-retries", PatrolRunOptionType.VALUE, false, null,
      "Retry failed web tests.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_VIDEO("webVideo", "Web video", "--web-video", PatrolRunOptionType.VALUE, false, null,
      "Web video recording mode.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_TIMEOUT("webTimeout", "Web timeout (ms)", "--web-timeout", PatrolRunOptionType.VALUE, false, null,
      "Per-test timeout in milliseconds.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_WORKERS("webWorkers", "Web workers", "--web-workers", PatrolRunOptionType.VALUE, false, null,
      "Max parallel web workers.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_REPORTER("webReporter", "Web reporter", "--web-reporter", PatrolRunOptionType.VALUE, false, null,
      "JSON array of Playwright reporters.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_LOCALE("webLocale", "Web locale", "--web-locale", PatrolRunOptionType.VALUE, false, null,
      "Locale for browser emulation.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_TIMEZONE("webTimezone", "Web timezone", "--web-timezone", PatrolRunOptionType.VALUE, false, null,
      "Timezone for browser emulation.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_COLOR_SCHEME("webColorScheme", "Web color scheme", "--web-color-scheme", PatrolRunOptionType.VALUE, false, null,
      "Preferred color scheme for web tests.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_GEOLOCATION("webGeolocation", "Web geolocation", "--web-geolocation", PatrolRunOptionType.VALUE, false, null,
      "Geolocation JSON object for browser context.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_PERMISSIONS("webPermissions", "Web permissions", "--web-permissions", PatrolRunOptionType.VALUE, false, null,
      "Permissions JSON array for browser context.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_USER_AGENT("webUserAgent", "Web user agent", "--web-user-agent", PatrolRunOptionType.VALUE, false, null,
      "Custom user agent string.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_VIEWPORT("webViewport", "Web viewport", "--web-viewport", PatrolRunOptionType.VALUE, false, null,
      "Viewport JSON object for browser context.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_GLOBAL_TIMEOUT("webGlobalTimeout", "Web global timeout (ms)", "--web-global-timeout", PatrolRunOptionType.VALUE, false, null,
      "Maximum total time for web test run.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_SHARD("webShard", "Web shard", "--web-shard", PatrolRunOptionType.VALUE, false, null,
      "Shard tests by current/total.",
      EnumSet.of(PatrolCommandMode.TEST)),
  WEB_HEADLESS("webHeadless", "Web headless (true/false)", "--web-headless", PatrolRunOptionType.VALUE, false, null,
      "Run web tests in headless mode.",
      EnumSet.of(PatrolCommandMode.TEST));

  private final String id;
  private final String label;
  private final String flag;
  private final PatrolRunOptionType type;
  private final boolean negatable;
  private final Boolean defaultToggleValue;
  private final String description;
  private final EnumSet<PatrolCommandMode> modes;

  PatrolRunOption(String id,
                  String label,
                  String flag,
                  PatrolRunOptionType type,
                  boolean negatable,
                  Boolean defaultToggleValue,
                  String description,
                  EnumSet<PatrolCommandMode> modes) {
    this.id = id;
    this.label = label;
    this.flag = flag;
    this.type = type;
    this.negatable = negatable;
    this.defaultToggleValue = defaultToggleValue;
    this.description = description;
    this.modes = modes;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public String getFlag() {
    return flag;
  }

  public PatrolRunOptionType getType() {
    return type;
  }

  public boolean isNegatable() {
    return negatable;
  }

  public Boolean getDefaultToggleValue() {
    return defaultToggleValue;
  }

  public String getDescription() {
    return description;
  }

  public boolean appliesTo(PatrolCommandMode mode) {
    return modes.contains(mode);
  }

  public String getNegatedFlag() {
    return "--no-" + flag.substring(2);
  }
}
