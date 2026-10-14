package com.patrol.jetbrains.run;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.execution.ParametersListUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsing and formatting for the {@code --dart-define} run option.
 *
 * <p>Accepts the shapes people actually have on hand, so a define list can be pasted straight from
 * a build script or a terminal history entry:
 *
 * <pre>
 *   --dart-define ENV=dev --dart-define API_BASE_URL=https://example.com
 *   --dart-define=ENV=dev
 *   ENV=dev;API_BASE_URL=https://example.com
 *   ENV=dev
 *   API_BASE_URL=https://example.com
 * </pre>
 *
 * <p>Existing run configurations may contain {@code KEY=VALUE;KEY=VALUE}. The editor converts
 * that legacy form to CLI flags, while {@link #split(String)} accepts both forms when launching.
 */
public final class DartDefines {
  private static final String FLAG = "--dart-define";
  private DartDefines() {
  }

  /** Splits into {@code KEY=VALUE} entries without interpreting them. */
  public static @NotNull List<String> split(@Nullable String raw) {
    List<String> entries = new ArrayList<>();
    if (StringUtil.isEmptyOrSpaces(raw)) {
      return entries;
    }
    String text = raw.trim();

    if (text.contains(FLAG)) {
      // Shell-style: tokenize so quoted values keep their spaces, then take each flag's argument.
      List<String> tokens = ParametersListUtil.parse(text.replace('\n', ' '));
      for (int i = 0; i < tokens.size(); i++) {
        String token = tokens.get(i);
        if (token.equals(FLAG)) {
          if (i + 1 < tokens.size()) {
            addEntry(entries, tokens.get(++i));
          }
        } else if (token.startsWith(FLAG + "=")) {
          addEntry(entries, token.substring(FLAG.length() + 1));
        } else {
          // A bare KEY=VALUE mixed in with flag form.
          addEntry(entries, token);
        }
      }
      return entries;
    }

    for (String part : text.split("[;\n]")) {
      addEntry(entries, part);
    }
    return entries;
  }

  /** Shows previously saved key/value lists as the CLI flags users can paste and edit directly. */
  public static @NotNull String toFlagSyntax(@Nullable String raw) {
    if (StringUtil.isEmptyOrSpaces(raw)) {
      return "";
    }
    if (raw.contains(FLAG)) {
      return raw.trim();
    }
    List<String> flags = new ArrayList<>();
    for (String entry : split(raw)) {
      flags.add(FLAG + " " + quoteIfNeeded(entry));
    }
    return String.join(" ", flags);
  }

  private static @NotNull String quoteIfNeeded(@NotNull String value) {
    if (value.chars().noneMatch(c -> Character.isWhitespace(c) || c == '"' || c == '\\')) {
      return value;
    }
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  private static void addEntry(@NotNull List<String> entries, @Nullable String candidate) {
    if (candidate == null) {
      return;
    }
    String trimmed = candidate.trim();
    if (!trimmed.isEmpty()) {
      entries.add(trimmed);
    }
  }
}
