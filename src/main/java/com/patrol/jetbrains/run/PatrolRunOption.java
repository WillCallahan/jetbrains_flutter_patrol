package com.patrol.jetbrains.run;

import java.util.EnumSet;

public enum PatrolRunOption {
  EXCLUDE("exclude", "Exclude targets", "--exclude", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  GENERATE_BUNDLE("generateBundle", "Generate bundle", "--generate-bundle", PatrolRunOptionType.TOGGLE, true, true, EnumSet.allOf(PatrolCommandMode.class)),
  DEBUG("debug", "Debug build", "--debug", PatrolRunOptionType.TOGGLE, true, true, EnumSet.allOf(PatrolCommandMode.class)),
  PROFILE("profile", "Profile build", "--profile", PatrolRunOptionType.TOGGLE, true, false, EnumSet.allOf(PatrolCommandMode.class)),
  RELEASE("release", "Release build", "--release", PatrolRunOptionType.TOGGLE, true, false, EnumSet.allOf(PatrolCommandMode.class)),
  FLAVOR("flavor", "Flavor", "--flavor", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  DART_DEFINE("dartDefine", "Dart define (KEY=VALUE)", "--dart-define", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  DART_DEFINE_FILE("dartDefineFile", "Dart define from file", "--dart-define-from-file", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  LABEL("label", "Show label", "--label", PatrolRunOptionType.TOGGLE, true, true, EnumSet.allOf(PatrolCommandMode.class)),
  TEST_SERVER_PORT("testServerPort", "Test server port", "--test-server-port", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  APP_SERVER_PORT("appServerPort", "App server port", "--app-server-port", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  TAGS("tags", "Tags", "--tags", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  EXCLUDE_TAGS("excludeTags", "Exclude tags", "--exclude-tags", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  HIDE_TEST_STEPS("hideTestSteps", "Hide test steps", "--hide-test-steps", PatrolRunOptionType.TOGGLE, true, false, EnumSet.allOf(PatrolCommandMode.class)),
  CLEAR_TEST_STEPS("clearTestSteps", "Clear test steps", "--clear-test-steps", PatrolRunOptionType.TOGGLE, true, true, EnumSet.allOf(PatrolCommandMode.class)),
  CHECK_COMPATIBILITY("checkCompatibility", "Check compatibility", "--check-compatibility", PatrolRunOptionType.TOGGLE, true, true, EnumSet.allOf(PatrolCommandMode.class)),
  UNINSTALL("uninstall", "Uninstall before/after", "--uninstall", PatrolRunOptionType.TOGGLE, true, true, EnumSet.allOf(PatrolCommandMode.class)),
  BUILD_NAME("buildName", "Build name", "--build-name", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  BUILD_NUMBER("buildNumber", "Build number", "--build-number", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  PACKAGE_NAME("packageName", "Android package name", "--package-name", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  BUNDLE_ID("bundleId", "iOS bundle ID", "--bundle-id", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  CLEAR_PERMISSIONS("clearPermissions", "Clear iOS permissions", "--clear-permissions", PatrolRunOptionType.TOGGLE, false, false, EnumSet.allOf(PatrolCommandMode.class)),
  FULL_ISOLATION("fullIsolation", "Full isolation (iOS Simulator)", "--full-isolation", PatrolRunOptionType.TOGGLE, false, false, EnumSet.allOf(PatrolCommandMode.class)),
  IOS_VERSION("iosVersion", "iOS version", "--ios", PatrolRunOptionType.VALUE, false, null, EnumSet.allOf(PatrolCommandMode.class)),
  OPEN_DEVTOOLS("openDevtools", "Open DevTools", "--open-devtools", PatrolRunOptionType.TOGGLE, true, false, EnumSet.of(PatrolCommandMode.DEVELOP)),
  COVERAGE("coverage", "Coverage", "--coverage", PatrolRunOptionType.TOGGLE, true, false, EnumSet.of(PatrolCommandMode.TEST)),
  COVERAGE_IGNORE("coverageIgnore", "Coverage ignore globs", "--coverage-ignore", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  COVERAGE_PACKAGE("coveragePackage", "Coverage package regex", "--coverage-package", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  SHOW_FLUTTER_LOGS("showFlutterLogs", "Show Flutter logs", "--show-flutter-logs", PatrolRunOptionType.TOGGLE, true, false, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_RESULTS_DIR("webResultsDir", "Web results dir", "--web-results-dir", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_REPORT_DIR("webReportDir", "Web report dir", "--web-report-dir", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_RETRIES("webRetries", "Web retries", "--web-retries", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_VIDEO("webVideo", "Web video", "--web-video", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_TIMEOUT("webTimeout", "Web timeout (ms)", "--web-timeout", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_WORKERS("webWorkers", "Web workers", "--web-workers", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_REPORTER("webReporter", "Web reporter", "--web-reporter", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_LOCALE("webLocale", "Web locale", "--web-locale", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_TIMEZONE("webTimezone", "Web timezone", "--web-timezone", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_COLOR_SCHEME("webColorScheme", "Web color scheme", "--web-color-scheme", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_GEOLOCATION("webGeolocation", "Web geolocation", "--web-geolocation", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_PERMISSIONS("webPermissions", "Web permissions", "--web-permissions", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_USER_AGENT("webUserAgent", "Web user agent", "--web-user-agent", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_VIEWPORT("webViewport", "Web viewport", "--web-viewport", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_GLOBAL_TIMEOUT("webGlobalTimeout", "Web global timeout (ms)", "--web-global-timeout", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_SHARD("webShard", "Web shard", "--web-shard", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST)),
  WEB_HEADLESS("webHeadless", "Web headless (true/false)", "--web-headless", PatrolRunOptionType.VALUE, false, null, EnumSet.of(PatrolCommandMode.TEST));

  private final String id;
  private final String label;
  private final String flag;
  private final PatrolRunOptionType type;
  private final boolean negatable;
  private final Boolean defaultToggleValue;
  private final EnumSet<PatrolCommandMode> modes;

  PatrolRunOption(String id,
                  String label,
                  String flag,
                  PatrolRunOptionType type,
                  boolean negatable,
                  Boolean defaultToggleValue,
                  EnumSet<PatrolCommandMode> modes) {
    this.id = id;
    this.label = label;
    this.flag = flag;
    this.type = type;
    this.negatable = negatable;
    this.defaultToggleValue = defaultToggleValue;
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

  public boolean appliesTo(PatrolCommandMode mode) {
    return modes.contains(mode);
  }

  public String getNegatedFlag() {
    return "--no-" + flag.substring(2);
  }
}
