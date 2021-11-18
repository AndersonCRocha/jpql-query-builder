package com.github.andersoncrocha.utils;

import com.github.andersoncrocha.operations.QueryOperation;
import com.github.andersoncrocha.operations.WhereGroup;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryUtils {

  private static final Pattern PARAMETER_NAME_PATTERN = Pattern.compile(":(?<parameterName>[A-Za-z0-9]*)");

  public static String extractParameterName(String clause) {
    Matcher matcher = PARAMETER_NAME_PATTERN.matcher(clause);

    if (matcher.find()) {
      return matcher.group("parameterName");
    }

    return null;
  }

  public static String joinOperations(List<QueryOperation> operations) {
    return operations.stream()
      .map(QueryOperation::getOperation)
      .map(String::trim)
      .collect(Collectors.joining(" "))
      .concat(" ");
  }

}
