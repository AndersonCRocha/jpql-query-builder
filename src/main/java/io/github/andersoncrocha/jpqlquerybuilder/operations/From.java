package io.github.andersoncrocha.jpqlquerybuilder.operations;

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
    return String.format("FROM %s AS %s ", from, alias);
  }

}
