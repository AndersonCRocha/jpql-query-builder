package io.github.andersoncrocha.jpqlquerybuilder.utils;

import java.util.Objects;

public class StringUtils {

  public static String uncapitalize(String term) {
    if (Objects.isNull(term)) {
      return null;
    }

    String firstLetterLower = String.valueOf(term.charAt(0)).toLowerCase();
    return firstLetterLower.concat(term.substring(1));
  }

}
