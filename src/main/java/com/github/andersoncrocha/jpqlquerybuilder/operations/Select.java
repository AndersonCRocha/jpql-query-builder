package com.github.andersoncrocha.jpqlquerybuilder.operations;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Select implements QueryOperation {

  private final List<String> fields;

  public Select(String fields) {
    this.fields = this.prepareFields(fields);
  }

  public void addSelect(String fields) {
    Objects.requireNonNull(fields, "addSelect() cannot be called before select() method");
    List<String> preparedFields = this.prepareFields(fields);
    this.fields.addAll(preparedFields);
  }

  @Override
  public String getOperation() {
    String joinedFields = String.join(", ", this.fields);
    return String.format("SELECT %s ", joinedFields);
  }

  private List<String> prepareFields(String fields) {
    String[] separatedFields = fields.split(",");
    return Stream.of(separatedFields)
      .map(String::trim)
      .collect(Collectors.toList());
  }

}
