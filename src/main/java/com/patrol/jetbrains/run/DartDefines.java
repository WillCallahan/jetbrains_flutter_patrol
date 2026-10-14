package com.patrol.jetbrains.run;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.execution.ParametersListUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * <p>The canonical stored form stays {@code KEY=VALUE;KEY=VALUE}, which is what earlier versions
 * wrote, so existing run configurations keep working.
 */
public final class DartDefines {
  private static final String FLAG = "--dart-define";
  /** Separators for the stored form. Comma is deliberately absent: values such as lists contain it. */
  private static final String ENTRY_SEPARATORS = ";\n";

  private DartDefines() {
  }

  /** Parses any accepted shape into ordered KEY -> VALUE pairs. Later keys win. */
  public static @NotNull Map<String, String> parse(@Nullable String raw) {
    Map<String, String> defines = new LinkedHashMap<>();
    for (String entry : split(raw)) {
      int idx = entry.indexOf('=');
      if (idx < 0) {
        defines.put(entry, "");
      } else {
        defines.put(entry.substring(0, idx).trim(), entry.substring(idx + 1).trim());
      }
    }
    return defines;
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

  /** Formats pairs into the canonical stored form. */
  public static @NotNull String format(@NotNull Map<String, String> defines) {
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, String> entry : defines.entrySet()) {
      if (StringUtil.isEmptyOrSpaces(entry.getKey())) {
        continue;
      }
      if (builder.length() > 0) {
        builder.append(';');
      }
      builder.append(entry.getKey().trim());
      if (!StringUtil.isEmptyOrSpaces(entry.getValue())) {
        builder.append('=').append(entry.getValue().trim());
      }
    }
    return builder.toString();
  }

  /** Normalizes arbitrary pasted text into the canonical stored form. */
  public static @NotNull String normalize(@Nullable String raw) {
    return format(parse(raw));
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
