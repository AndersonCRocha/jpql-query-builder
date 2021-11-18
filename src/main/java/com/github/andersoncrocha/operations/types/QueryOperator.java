package com.github.andersoncrocha.operations.types;

public enum QueryOperator {

  SELECT ("SELECT"),
  ADD_SELECT ("ADD_SELECT"),
  FROM ("FROM"),
  JOIN ("JOIN"),
  LEFT_JOIN ("LEFT_JOIN"),
  RIGHT_JOIN ("RIGHT_JOIN"),
  WHERE ("WHERE"),
  OPEN_PARENTHESES ("OPEN_PARENTHESES"),
  CLOSE_PARENTHESES ("CLOSE_PARENTHESES"),
  GROUP_BY ("GROUP_BY"),
  ORDER_BY ("ORDER_BY"),
  ADD_ORDER_BY ("ADD_ORDER_BY"),
  AND ("AND"),
  OR ("OR"),
  NONE ("");

  private final String value;

  QueryOperator(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

}
