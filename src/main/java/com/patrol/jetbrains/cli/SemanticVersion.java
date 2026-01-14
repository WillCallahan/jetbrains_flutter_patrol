package com.patrol.jetbrains.cli;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SemanticVersion {
  private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

  private final int major;
  private final int minor;
  private final int patch;

  public SemanticVersion(int major, int minor, int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }

  public static Optional<SemanticVersion> parse(@Nullable String text) {
    if (text == null) {
      return Optional.empty();
    }
    Matcher matcher = VERSION_PATTERN.matcher(text);
    if (!matcher.find()) {
      return Optional.empty();
    }
    int major = Integer.parseInt(matcher.group(1));
    int minor = Integer.parseInt(matcher.group(2));
    int patch = Integer.parseInt(matcher.group(3));
    return Optional.of(new SemanticVersion(major, minor, patch));
  }

  public boolean isAtLeast(int major, int minor, int patch) {
    if (this.major != major) {
      return this.major > major;
    }
    if (this.minor != minor) {
      return this.minor > minor;
    }
    return this.patch >= patch;
  }

  @Override
  public String toString() {
    return major + "." + minor + "." + patch;
  }
}
