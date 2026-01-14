package com.patrol.jetbrains;

import java.nio.file.Path;
import java.util.Optional;

public interface PatrolCliLocator {
  Optional<Path> findPatrolCli();
}
