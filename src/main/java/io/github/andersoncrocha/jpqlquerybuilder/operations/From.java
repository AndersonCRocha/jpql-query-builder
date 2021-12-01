package io.github.andersoncrocha.jpqlquerybuilder.operations;

import java.util.Objects;

public class From implements QueryOperation {

  private final String from;
  private final String alias;

  public From(String from, String alias) {
    this.from = from;
    this.alias = alias;
  }

  public String getAlias() {
    return alias;
  }

  @Override
  public String getOperation() {
    StringBuilder fromClause = new StringBuilder("FROM ").append(from);
    if (Objects.nonNull(alias)) {
      fromClause.append(" AS ").append(alias);
    }
    return fromClause.toString();
  }

}
