package io.github.andersoncrocha.jpqlquerybuilder;

import io.github.andersoncrocha.jpqlquerybuilder.operations.From;
import io.github.andersoncrocha.jpqlquerybuilder.operations.GroupBy;
import io.github.andersoncrocha.jpqlquerybuilder.operations.JoinGroup;
import io.github.andersoncrocha.jpqlquerybuilder.operations.JoinGroup.Join;
import io.github.andersoncrocha.jpqlquerybuilder.operations.OrderBy;
import io.github.andersoncrocha.jpqlquerybuilder.operations.Select;
import io.github.andersoncrocha.jpqlquerybuilder.operations.WhereGroup;
import io.github.andersoncrocha.jpqlquerybuilder.operations.WhereGroup.Where;
import io.github.andersoncrocha.jpqlquerybuilder.operations.types.JoinType;
import io.github.andersoncrocha.jpqlquerybuilder.operations.types.QueryOperator;
import io.github.andersoncrocha.jpqlquerybuilder.utils.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QueryBuilder {

  private final EntityManager entityManager;

  private Select select;
  private From from;
  private final JoinGroup joinGroup;
  private final WhereGroup whereGroup;
  private GroupBy groupBy;
  private OrderBy orderBy;

  private Integer firstResult = 0;
  private Integer maxResults = Integer.MAX_VALUE;

  private QueryOperator lastOperator;
  private boolean nativeQuery;

  public QueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
    this.joinGroup = new JoinGroup();
    this.whereGroup = new WhereGroup();
  }

  public QueryBuilder select(String select) {
    if (Objects.isNull(this.select)) {
      this.select = new Select(select);
    } else {
      this.select.addSelect(select);
    }
    this.lastOperator = QueryOperator.SELECT;
    return this;
  }

  public QueryBuilder from(Class<?> fromClass) {
    String simpleNameClass = fromClass.getSimpleName();
    String alias = StringUtils.uncapitalize(simpleNameClass);
    return this.from(fromClass, alias);
  }

  public QueryBuilder from(Function<QueryBuilder, QueryBuilder> subQueryFunction, String alias) {
    QueryBuilder queryBuilderSubQuery = subQueryFunction.apply(new QueryBuilder(entityManager));
    return this.from(queryBuilderSubQuery, alias);
  }

  public QueryBuilder from(QueryBuilder queryBuilderSubQuery, String alias) {
    this.from = new From("(" + queryBuilderSubQuery.getQueryString() + ")", alias);
    this.lastOperator = QueryOperator.FROM;
    return this;
  }

  public QueryBuilder from(String table, String alias) {
    this.from = new From(table, alias);
    this.lastOperator = QueryOperator.FROM;
    return this;
  }

  public QueryBuilder from(Class<?> fromClass, String alias) {
    return this.from(fromClass.getSimpleName(), alias);
  }

  private QueryBuilder join(String target, JoinType type) {
    Join join = new Join(target, type);
    this.joinGroup.join(join);
    return this;
  }

  public QueryBuilder join(String target, String condition) {
    return this.join(target.concat(" ON ").concat(condition), JoinType.INNER);
  }

  public QueryBuilder join(String target) {
    return this.join(target, JoinType.INNER);
  }

  public QueryBuilder joinFetch(String target) {
    return this.join(target, JoinType.INNER_FETCH);
  }

  public QueryBuilder leftJoin(String target) {
    return this.join(target, JoinType.LEFT);
  }

  public QueryBuilder leftJoinFetch(String target) {
    return this.join(target, JoinType.LEFT_FETCH);
  }

  public QueryBuilder rightJoin(String target) {
    return this.join(target, JoinType.RIGHT);
  }

  public QueryBuilder rightJoinFetch(String target) {
    return this.join(target, JoinType.RIGHT_FETCH);
  }

  private QueryBuilder where(String clause, QueryOperator type, Object parameter) {
    Where where = new Where(clause, type, parameter);
    this.whereGroup.where(where);
    this.lastOperator = QueryOperator.AND;
    return this;
  }

  public QueryBuilder where(String clause, Object parameter) {
    QueryOperator type = QueryOperator.NONE;

    if (lastOperator == QueryOperator.OR) {
      type = QueryOperator.OR;
    } else if (lastOperator != QueryOperator.OPEN_PARENTHESES) {
      type = QueryOperator.AND;
    }

    return this.where(clause, type, parameter);
  }

  public QueryBuilder whereIf(String clause, Object parameter, boolean shouldAddWhere) {
    return shouldAddWhere ? this.where(clause, parameter) : this;
  }

  public QueryBuilder where(String clause) {
    return this.where(clause, null);
  }

  public QueryBuilder whereIf(String clause, boolean shouldAddWhere) {
    return shouldAddWhere ? this.where(clause) : this;
  }

  public QueryBuilder or() {
    this.lastOperator = QueryOperator.OR;
    return this;
  }

  public QueryBuilder openParentheses() {
    this.whereGroup.openParentheses(lastOperator);
    this.lastOperator = QueryOperator.OPEN_PARENTHESES;
    return this;
  }

  public QueryBuilder closeParentheses() {
    this.whereGroup.closeParentheses();
    this.lastOperator = QueryOperator.CLOSE_PARENTHESES;
    return this;
  }

  public QueryBuilder groupBy(String groupBy) {
    this.groupBy = new GroupBy(groupBy);
    return this;
  }

  public QueryBuilder orderBy(String orderBy) {
    this.orderBy = new OrderBy(orderBy);
    return this;
  }

  public QueryBuilder addOrderBy(String orderBy) {
    this.orderBy.addOrderBy(orderBy);
    return this;
  }

  public QueryBuilder firstResult(Integer firstResult) {
    this.firstResult = firstResult;
    return this;
  }

  public QueryBuilder maxResults(Integer maxResults) {
    this.maxResults = maxResults;
    return this;
  }

  public QueryBuilder nativeQuery() {
    this.nativeQuery = true;
    return this;
  }

  public QueryBuilder parameter(String name, Object value) {
    this.whereGroup.addParameter(name, value);
    return this;
  }

  private void validateQuery() {
    Objects.requireNonNull(from, "It is not allowed to create a query without 'from' clause");
    this.select = Objects.nonNull(select) ? select : new Select(from.getAlias());
  }

  public String getQueryString() {
    this.validateQuery();

    StringBuilder query = new StringBuilder()
      .append(select.getOperation())
      .append(from.getOperation())
      .append(joinGroup.getOperation())
      .append(whereGroup.getOperation());

    if (Objects.nonNull(groupBy)) {
      query.append(groupBy.getOperation());
    }

    if (Objects.nonNull(orderBy)) {
      query.append(orderBy.getOperation());
    }

    return query.toString();
  }

  public <T> TypedQuery<T> getQuery(Class<T> resultClass) {
    String queryString = this.getQueryString();
    TypedQuery<T> query = this.entityManager.createQuery(queryString, resultClass)
      .setFirstResult(firstResult)
      .setMaxResults(maxResults);
    this.whereGroup.getParameters().forEach(query::setParameter);
    return query;
  }

  public Query getNativeQuery() {
    String queryString = this.getQueryString();
    Query query = this.entityManager.createNativeQuery(queryString, Tuple.class)
      .setFirstResult(firstResult)
      .setMaxResults(maxResults);
    this.whereGroup.getParameters().forEach(query::setParameter);
    return query;
  }

  public <T> Optional<T> getSingleResult(Class<T> resultType) {
    try {
      if (nativeQuery) {
        Object resultObject = this.getNativeQuery().getSingleResult();
        T result = resultType.cast(resultObject);
        return Optional.ofNullable(result);
      }

      return Optional.ofNullable(this.getQuery(resultType).getSingleResult());
    } catch (NoResultException exception) {
      return Optional.empty();
    }
  }

  public Optional<Tuple> getSingleResult() {
    return this.getSingleResult(Tuple.class);
  }

  public <T> Optional<T> getSingleResult(Function<Tuple, T> mapper) {
    return this.getSingleResult().map(mapper);
  }

  public <T> List<T> getResultList(Class<T> resultType) {
    return this.getQuery(resultType).getResultList();
  }

  @SuppressWarnings("unchecked")
  public List<Tuple> getResultList() {
    return this.nativeQuery
      ? (List<Tuple>) this.getNativeQuery().getResultList()
      : this.getResultList(Tuple.class);
  }

  public <T> List<T> getResultList(Function<Tuple, T> mapper) {
    return this.getResultList()
      .stream()
      .map(mapper)
      .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return this.getQueryString();
  }

}
