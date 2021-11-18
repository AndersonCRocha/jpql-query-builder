package com.github.andersoncrocha.operations;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupBy implements QueryOperation {

  private final Set<String> fields;

  public GroupBy(String fields) {
    this.fields = this.prepareFields(fields);
  }

  @Override
  public String getOperation() {
    String joinedFields = String.join(", ", this.fields);
    return String.format("GROUP BY %s ", joinedFields);
  }

  private Set<String> prepareFields(String fields) {
    String[] separatedFields = fields.split(",");
    return Stream.of(separatedFields)
      .map(String::trim)
      .collect(Collectors.toSet());
  }

}
