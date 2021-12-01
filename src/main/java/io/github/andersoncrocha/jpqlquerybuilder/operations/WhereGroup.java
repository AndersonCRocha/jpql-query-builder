package io.github.andersoncrocha.jpqlquerybuilder.operations;

import io.github.andersoncrocha.jpqlquerybuilder.operations.types.QueryOperator;
import io.github.andersoncrocha.jpqlquerybuilder.utils.QueryUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class WhereGroup implements QueryOperation {

  final List<QueryOperation> wheres = new ArrayList<>();
  final Map<String, Object> parameters = new HashMap<>();

  public void where(Where where) {
    if (wheres.isEmpty()) {
      where.type = QueryOperator.WHERE;
    }

    this.wheres.add(where);

    if (Objects.nonNull(where.parameters) && !where.parameters.isEmpty()) {
      List<String> parameterNames = QueryUtils.extractParameterName(where.clause);
      Objects.requireNonNull(parameterNames, "A parameter was entered but no named parameter was found in the clause");
      IntStream.range(0, where.parameters.size())
        .forEachOrdered(index -> this.parameters.put(parameterNames.get(index), where.parameters.get(index)));
    }
  }

  public void openParentheses(QueryOperator operator) {
    Where parentheses = new Where("(", operator);
    this.wheres.add(parentheses);
  }

  public void closeParentheses() {
    Where parentheses = new Where(")", QueryOperator.NONE);
    this.wheres.add(parentheses);
  }

  public void addParameter(String name, Object value) {
    this.parameters.put(name, value);
  }

  public void addParameters(Map<String, Object> parameters) {
    this.parameters.putAll(parameters);
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override
  public String getOperation() {
    return QueryUtils.joinOperations(wheres);
  }

  public static class Where implements QueryOperation {

    private final String clause;
    private QueryOperator type;
    private List<Object> parameters;

    public Where(String clause, QueryOperator type) {
      this.clause = clause;
      this.type = type;
    }

    public Where(String clause, QueryOperator type, List<Object> parameters) {
      this.clause = clause;
      this.type = type;
      this.parameters = parameters;
    }

    @Override
    public String getOperation() {
      return String.format("%s %s", type.getValue(), clause);
    }

  }
}
