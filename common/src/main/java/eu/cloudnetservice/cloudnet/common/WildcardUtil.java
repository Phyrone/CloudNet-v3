/*
 * Copyright 2019-2022 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cloudnetservice.cloudnet.common;

import eu.cloudnetservice.cloudnet.common.log.LogManager;
import eu.cloudnetservice.cloudnet.common.log.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Util for RegEx pattern matching and pattern fixing
 */
public final class WildcardUtil {

  private static final Logger LOGGER = LogManager.logger(WildcardUtil.class);

  private WildcardUtil() {
    throw new UnsupportedOperationException();
  }

  /**
   * Filters all values out of the given inputValues which are matching the given pattern.
   *
   * @param inputValues the input values to search trough.
   * @param regex       the regex to use for searching.
   * @param <T>         the type of the input values.
   * @return all input values matching the given pattern.
   * @see #filterWildcard(Collection, String, boolean)
   */
  public static <T extends Nameable> @NonNull Collection<T> filterWildcard(
    @NonNull Collection<T> inputValues,
    @NonNull String regex
  ) {
    return filterWildcard(inputValues, regex, true);
  }

  /**
   * Checks if any of the given values matches the given regex.
   *
   * @param values the values to search trough.
   * @param regex  the regex to use for searching.
   * @return true if any of the values matches the given regex, false otherwise.
   * @see #anyMatch(Collection, String, boolean)
   */
  public static boolean anyMatch(@NonNull Collection<? extends Nameable> values, @NonNull String regex) {
    return anyMatch(values, regex, true);
  }

  /**
   * Filters all values out of the given inputValues which are matching the given pattern.
   *
   * @param inputValues   the input values to search trough.
   * @param regex         the regex to use for searching.
   * @param caseSensitive if the search should be case-sensitive.
   * @param <T>           the type of the input values.
   * @return all input values matching the given pattern.
   */
  public static <T extends Nameable> @NonNull Collection<T> filterWildcard(
    @NonNull Collection<T> inputValues,
    @NonNull String regex,
    boolean caseSensitive
  ) {
    if (inputValues.isEmpty()) {
      return inputValues;
    } else {
      var pattern = prepare(regex, caseSensitive);
      return pattern == null ? new ArrayList<>() : inputValues.stream()
        .filter(t -> pattern.matcher(t.name()).matches())
        .collect(Collectors.toList());
    }
  }

  /**
   * Checks if any of the given values matches the given regex.
   *
   * @param values        the values to search trough.
   * @param regex         the regex to use for searching.
   * @param caseSensitive if the search should be case-sensitive.
   * @return true if any of the values matches the given regex, false otherwise.
   */
  public static boolean anyMatch(
    @NonNull Collection<? extends Nameable> values,
    @NonNull String regex,
    boolean caseSensitive
  ) {
    if (values.isEmpty()) {
      return false;
    } else {
      var pattern = prepare(regex, caseSensitive);
      return pattern != null && values.stream()
        .anyMatch(t -> pattern.matcher(t.name()).matches());
    }
  }

  /**
   * Prepares the given regex string, grouping every {@literal *} in the provided string to a separate regex group.
   *
   * @param regex         the regex string to prepare and compile.
   * @param caseSensitive if the pattern should be case-insensitive
   * @return the compiled pattern or null if the compilation failed
   */
  private static @Nullable Pattern prepare(@NonNull String regex, boolean caseSensitive) {
    regex = regex.replace("*", "(.*)");
    return tryCompile(regex, caseSensitive);
  }

  /**
   * Tries to compile the given pattern string and tries to automatically fix it if the input pattern is not
   * compilable.
   *
   * @param pattern       the pattern string to compile
   * @param caseSensitive if the pattern should be case-insensitive
   * @return the compiled pattern or null if the compilation failed
   */
  private static @Nullable Pattern tryCompile(@NonNull String pattern, boolean caseSensitive) {
    try {
      return Pattern.compile(pattern, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
    } catch (PatternSyntaxException exception) {
      return tryFixPattern(exception, caseSensitive);
    } catch (StackOverflowError error) {
      return null;
    }
  }

  /**
   * Tries to automatically fix a pattern based on the given exception.
   *
   * @param exception     the exception occurred during the compile of the pattern string
   * @param caseSensitive if the pattern check should be case-insensitive
   * @return a fixed, compiled version of the pattern or null if the given exception is unclear
   */
  private static @Nullable Pattern tryFixPattern(@NonNull PatternSyntaxException exception, boolean caseSensitive) {
    if (exception.getPattern() != null && exception.getIndex() != -1) {
      var pattern = exception.getPattern();
      if (pattern.length() > exception.getIndex()) {
        // index represents the char before the failed to parsed char index
        var firstPart = pattern.substring(0, exception.getIndex() + 1);
        var secondPart = pattern.substring(exception.getIndex() + 1);
        // escape the specific character which caused the failure using a \ and retry
        return tryCompile(firstPart + '\\' + secondPart, caseSensitive);
      } else if (exception.getDescription() != null
        && exception.getDescription().equals("Unclosed group")
        && exception.getIndex() == pattern.length()) {
        // an unclosed group is a special case which can only occur at the end of the string
        // meaning that a group was opened but not closed, we need to filter that out and escape
        // the group start
        return tryCompile(fixUnclosedGroups(pattern), caseSensitive);
      }
    }
    LOGGER.severe("Unable to fix pattern input " + exception.getPattern(), exception);
    return null;
  }

  /**
   * Searches for unclosed groups in the given patternInput and replaces the group openers {@literal (} with an escaped
   * {@literal \(} while taking care of completed groups.
   *
   * @param patternInput the pattern to check.
   * @return the same pattern as given but with fixed groups.
   */
  @VisibleForTesting
  static @NonNull String fixUnclosedGroups(@NonNull String patternInput) {
    var result = new StringBuilder();
    var content = patternInput.toCharArray();
    // we need to record the group closings to actually find the group opening which is not escaped
    var metGroupClosings = 0;
    // we loop reversed over it as we know that the group start must be before the group end, and we
    // are searching for it
    for (var index = content.length - 1; index >= 0; index--) {
      var c = content[index];
      if (c == ')' && isPartOfPattern(content, index)) {
        metGroupClosings++;
      } else if (c == '(' && isPartOfPattern(content, index) && --metGroupClosings < 0) {
        // we found an unclosed start of a group, escape it!
        // as we are looping backwards we first need to append the actual char and then the escaping backslash
        result.append(c).append("\\");
        metGroupClosings = 0;
        continue;
      }
      result.append(c);
    }
    // we looped backwards over the string so we need to reverse it to get it back in the correct sequence
    return result.reverse().toString();
  }

  /**
   * Checks if the current content index is part of the pattern or escaped.
   *
   * @param content the whole content of the pattern as char array.
   * @param index   the current reader index of the char to check.
   * @return true if the char is part of the pattern or false if escaped.
   */
  private static boolean isPartOfPattern(char[] content, int index) {
    return index <= 0 || content[--index] != '\\';
  }
}