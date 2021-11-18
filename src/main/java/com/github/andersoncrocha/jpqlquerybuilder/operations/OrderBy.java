package com.github.andersoncrocha.jpqlquerybuilder.operations;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderBy implements QueryOperation {

  private final Set<String> fields;

  public OrderBy(String fields) {
    this.fields = this.prepareFields(fields);
  }

  public void addOrderBy(String fields) {
    Objects.requireNonNull(fields, "addOrderBy() cannot be called before orderBy() method");
    Set<String> preparedFields = this.prepareFields(fields);
    this.fields.addAll(preparedFields);
  }

  @Override
  public String getOperation() {
    String joinedFields = String.join(", ", this.fields);
    return String.format("ORDER BY %s ", joinedFields);
  }

  private Set<String> prepareFields(String fields) {
    String[] separatedFields = fields.split(",");
    return Stream.of(separatedFields)
      .map(String::trim)
      .collect(Collectors.toSet());
  }

}
