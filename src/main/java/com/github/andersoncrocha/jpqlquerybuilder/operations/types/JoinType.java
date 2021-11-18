package com.github.andersoncrocha.jpqlquerybuilder.operations.types;

public enum JoinType {

  INNER ("JOIN"),
  LEFT ("LEFT JOIN"),
  RIGHT ("RIGHT JOIN"),
  INNER_FETCH ("JOIN FETCH"),
  LEFT_FETCH ("LEFT JOIN FETCH"),
  RIGHT_FETCH ("RIGHT JOIN FETCH");

  private final String value;

  JoinType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
