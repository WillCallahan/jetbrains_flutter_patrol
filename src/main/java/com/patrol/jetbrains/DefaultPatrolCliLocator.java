package com.patrol.jetbrains;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class DefaultPatrolCliLocator implements PatrolCliLocator {
  private final Path overridePath;

  public DefaultPatrolCliLocator(Path overridePath) {
    this.overridePath = overridePath;
  }

  @Override
  public Optional<Path> findPatrolCli() {
    if (overridePath != null && Files.isExecutable(overridePath)) {
      return Optional.of(overridePath);
    }

    Optional<Path> pubCache = resolvePubCacheCli();
    if (pubCache.isPresent()) {
      return pubCache;
    }

    String pathEnv = System.getenv("PATH");
    if (pathEnv == null || pathEnv.isEmpty()) {
      return Optional.empty();
    }

    String[] entries = pathEnv.split(System.getProperty("path.separator"));
    for (String entry : entries) {
      Path candidate = Paths.get(entry).resolve("patrol");
      if (Files.isExecutable(candidate)) {
        return Optional.of(candidate);
      }
    }

    return Optional.empty();
  }

  private Optional<Path> resolvePubCacheCli() {
    String home = System.getProperty("user.home");
    if (home == null || home.isEmpty()) {
      return Optional.empty();
    }
    Path unixPath = Path.of(home, ".pub-cache", "bin", "patrol");
    if (Files.isExecutable(unixPath)) {
      return Optional.of(unixPath);
    }
    Path windowsPath = Path.of(home, ".pub-cache", "bin", "patrol.bat");
    if (Files.isExecutable(windowsPath)) {
      return Optional.of(windowsPath);
    }
    return Optional.empty();
  }
}
