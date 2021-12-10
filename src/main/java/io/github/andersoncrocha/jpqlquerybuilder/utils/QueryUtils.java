package io.github.andersoncrocha.jpqlquerybuilder.utils;

import io.github.andersoncrocha.jpqlquerybuilder.operations.QueryOperation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryUtils {

  private static final Pattern PARAMETER_NAME_PATTERN = Pattern.compile(":(?<parameterName>[A-Za-z0-9]*)");

  private QueryUtils() {
    throw new UnsupportedOperationException("Utility class.");
  }

  public static List<String> extractParameterName(String clause) {
    Matcher matcher = PARAMETER_NAME_PATTERN.matcher(clause);
    List<String> parameters = new ArrayList<>();

    while (matcher.find()) {
      String parameterName = matcher.group("parameterName");
      if (!parameters.contains(parameterName)) {
        parameters.add(parameterName);
      }
    }

    return parameters;
  }

  public static String joinOperations(List<QueryOperation> operations) {
    return operations.stream()
      .map(QueryOperation::getOperation)
      .map(String::trim)
      .collect(Collectors.joining(" "))
      .concat(" ");
  }

}
