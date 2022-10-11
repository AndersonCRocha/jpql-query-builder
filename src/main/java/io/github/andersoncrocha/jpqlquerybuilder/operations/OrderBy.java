package io.github.andersoncrocha.jpqlquerybuilder.operations;

import io.github.andersoncrocha.jpqlquerybuilder.operations.types.SortDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrderBy implements QueryOperation {

  private final Map<String, SortDirection> fields;

  public OrderBy(String fields) {
    this.fields = this.prepareFields(fields);
  }

  public OrderBy(String field, SortDirection sortDirection) {
    this.fields = new HashMap<>();
    this.fields.put(field, sortDirection);
  }

  public void addOrderBy(String fields) {
    Map<String, SortDirection> preparedFields = this.prepareFields(fields);
    this.fields.putAll(preparedFields);
  }

  public void addOrderBy(String field, SortDirection sortDirection) {
    this.fields.put(field, sortDirection);
  }

  @Override
  public String getOperation() {
    String joinedFields = this.fields.entrySet()
            .stream()
            .map(entry -> {
              String field = entry.getKey();
              SortDirection sortDirection = entry.getValue();
              return String.format("%s %s", field, sortDirection.name());
            }).collect(Collectors.joining(", "));
    return String.format("ORDER BY %s ", joinedFields);
  }

  private Map<String, SortDirection> prepareFields(String fields) {
    String[] separatedFields = fields.split(",");
    return Stream.of(separatedFields)
      .map(String::trim)
      .collect(Collectors.toMap(Function.identity(), field -> SortDirection.ASC));
  }

}
