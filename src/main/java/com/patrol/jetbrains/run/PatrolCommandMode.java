package com.patrol.jetbrains.run;

public enum PatrolCommandMode {
  TEST("Test"),
  DEVELOP("Develop");

  private final String displayName;

  PatrolCommandMode(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
