package io.github.andersoncrocha.jpqlquerybuilder.utils;

import java.util.Objects;

public class StringUtils {

  private StringUtils() {
    throw new UnsupportedOperationException("Utility class.");
  }

  public static String uncapitalize(String term) {
    if (Objects.isNull(term)) {
      return null;
    }

    String firstLetterLower = String.valueOf(term.charAt(0)).toLowerCase();
    return firstLetterLower.concat(term.substring(1));
  }

  public static boolean isBlank(String text) {
    return Objects.isNull(text) || text.trim().isEmpty();
  }

  public static boolean isNotBlank(String text) {
    return !isBlank(text);
  }
}
