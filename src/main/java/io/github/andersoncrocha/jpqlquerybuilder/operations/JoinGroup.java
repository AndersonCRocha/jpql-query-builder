package io.github.andersoncrocha.jpqlquerybuilder.operations;

import io.github.andersoncrocha.jpqlquerybuilder.operations.types.JoinType;
import io.github.andersoncrocha.jpqlquerybuilder.utils.QueryUtils;

import java.util.ArrayList;
import java.util.List;

public class JoinGroup implements QueryOperation {

  private final List<QueryOperation> joins = new ArrayList<>();

  public void join(Join join) {
    this.joins.add(join);
  }

  @Override
  public String getOperation() {
    return QueryUtils.joinOperations(joins);
  }

  public static class Join implements QueryOperation {

    private final String target;
    private final JoinType type;

    public Join(String target, JoinType type) {
      this.target = target;
      this.type = type;
    }

    @Override
    public String getOperation() {
      return String.format("%s %s", type.getValue(), target);
    }

  }

}
