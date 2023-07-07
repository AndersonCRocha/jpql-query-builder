package io.github.andersoncrocha.jpqlquerybuilder;

import io.github.andersoncrocha.jpqlquerybuilder.mapper.AliasToPojoResultMapper;
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
import io.github.andersoncrocha.jpqlquerybuilder.operations.types.SortDirection;
import io.github.andersoncrocha.jpqlquerybuilder.utils.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class QueryBuilder {

  private static final Object EMPTY = new Object();

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

  private QueryBuilder() {
    this(null);
  }

  private QueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
    this.joinGroup = new JoinGroup();
    this.whereGroup = new WhereGroup();
  }

  public static QueryBuilder newQuery() {
    return new QueryBuilder();
  }

  public static QueryBuilder newQuery(EntityManager entityManager) {
    return new QueryBuilder(entityManager);
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

  public QueryBuilder from(UnaryOperator<QueryBuilder> subQueryFunction, String alias) {
    QueryBuilder queryBuilderSubQuery = subQueryFunction.apply(new QueryBuilder(entityManager));
    return this.from(queryBuilderSubQuery, alias);
  }

  public QueryBuilder from(QueryBuilder subQuery, String alias) {
    Map<String, Object> subQueryParameters = subQuery.whereGroup.getParameters();
    this.whereGroup.addParameters(subQueryParameters);
    this.from = new From("(" + subQuery.getQueryString() + ")", alias);
    this.lastOperator = QueryOperator.FROM;
    return this;
  }

  public QueryBuilder from(String table, String alias) {
    this.from = new From(table, alias);
    this.lastOperator = QueryOperator.FROM;
    return this;
  }

  public QueryBuilder from(String table) {
    return this.from(table, null);
  }

  public QueryBuilder from(Class<?> fromClass, String alias) {
    return this.from(fromClass.getSimpleName(), alias);
  }

  private QueryBuilder join(String target, JoinType type) {
    Join join = new Join(target, type);
    this.joinGroup.join(join);
    return this;
  }

  private QueryBuilder join(String target, String alias, String condition, JoinType type) {
    String joinTargetWithAlias = StringUtils.isNotBlank(alias)
        ? target.concat(" ").concat(alias)
        : target;
    String finalTarget = StringUtils.isNotBlank(condition)
        ? joinTargetWithAlias.concat(" ON ").concat(condition)
        : joinTargetWithAlias;
    return this.join(finalTarget, type);
  }

  private QueryBuilder join(Class<?> target, String alias, String condition, JoinType type) {
    return this.join(target.getSimpleName(), alias, condition, type);
  }

  public QueryBuilder join(Class<?> joinClass, String alias, String condition) {
    return this.join(joinClass, alias, condition, JoinType.INNER);
  }

  public QueryBuilder join(Class<?> joinClass, String alias) {
    return this.join(joinClass, alias, null);
  }

  public QueryBuilder join(String target, String condition) {
    return this.join(target, null, condition, JoinType.INNER);
  }

  public QueryBuilder join(String target) {
    return this.join(target, JoinType.INNER);
  }

  public QueryBuilder joinFetch(Class<?> joinClass, String alias, String condition) {
    return this.join(joinClass, alias, condition, JoinType.INNER_FETCH);
  }

  public QueryBuilder joinFetch(Class<?> joinClass, String alias) {
    return this.joinFetch(joinClass, alias, null);
  }

  public QueryBuilder joinFetch(String target) {
    return this.join(target, JoinType.INNER_FETCH);
  }

  public QueryBuilder leftJoin(Class<?> joinClass, String alias, String condition) {
    return this.join(joinClass, alias, condition, JoinType.LEFT);
  }

  public QueryBuilder leftJoin(Class<?> joinClass, String alias) {
    return this.leftJoin(joinClass, alias, null);
  }

  public QueryBuilder leftJoin(String target) {
    return this.join(target, JoinType.LEFT);
  }

  public QueryBuilder leftJoinFetch(Class<?> joinClass, String alias, String condition) {
    return this.join(joinClass, alias, condition, JoinType.LEFT_FETCH);
  }

  public QueryBuilder leftJoinFetch(Class<?> joinClass, String alias) {
    return this.leftJoinFetch(joinClass, alias, null);
  }

  public QueryBuilder leftJoinFetch(String target) {
    return this.join(target, JoinType.LEFT_FETCH);
  }

  public QueryBuilder rightJoin(Class<?> joinClass, String alias, String condition) {
    return this.join(joinClass, alias, condition, JoinType.RIGHT);
  }

  public QueryBuilder rightJoin(Class<?> joinClass, String alias) {
    return this.rightJoin(joinClass, alias, null);
  }

  public QueryBuilder rightJoin(String target) {
    return this.join(target, JoinType.RIGHT);
  }

  public QueryBuilder rightJoinFetch(Class<?> joinClass, String alias, String condition) {
    return this.join(joinClass, alias, condition, JoinType.RIGHT_FETCH);
  }

  public QueryBuilder rightJoinFetch(Class<?> joinClass, String alias) {
    return this.rightJoinFetch(joinClass, alias, null);
  }

  public QueryBuilder rightJoinFetch(String target) {
    return this.join(target, JoinType.RIGHT_FETCH);
  }

  private QueryBuilder where(String clause, QueryOperator type, Object... parameters) {
    List<Object> listOfParameters = Arrays.stream(parameters)
            .filter(parameter -> parameter != EMPTY)
            .collect(Collectors.toList());
    Where where = new Where(clause, type, listOfParameters);
    this.whereGroup.where(where);
    this.lastOperator = QueryOperator.AND;
    return this;
  }

  public QueryBuilder where(String clause, Object... parameters) {
    QueryOperator type = QueryOperator.NONE;

    if (lastOperator == QueryOperator.OR) {
      type = QueryOperator.OR;
    } else if (lastOperator != QueryOperator.OPEN_PARENTHESES) {
      type = QueryOperator.AND;
    }

    return this.where(clause, type, parameters);
  }

  public QueryBuilder whereIf(String clause, Object parameter, boolean shouldAddWhere) {
    return shouldAddWhere ? this.where(clause, parameter) : this;
  }

  public QueryBuilder whereNotIf(String clause, Object parameter, boolean shouldAddWhere) {
    return this.whereIf(clause, parameter, !shouldAddWhere);
  }

  public QueryBuilder where(String clause) {
    return this.where(clause, EMPTY);
  }

  public QueryBuilder whereIf(String clause, boolean shouldAddWhere) {
    return shouldAddWhere ? this.where(clause) : this;
  }

  public QueryBuilder whereNotIf(String clause, boolean shouldAddWhere) {
    return this.whereIf(clause, !shouldAddWhere);
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

  public QueryBuilder orderBy(String orderBy, SortDirection sortDirection) {
    if (Objects.nonNull(this.orderBy)) {
      this.orderBy.addOrderBy(orderBy, sortDirection);
    } else {
      this.orderBy = new OrderBy(orderBy, sortDirection);
    }
    return this;
  }

  public QueryBuilder orderBy(String orderBy) {
    return this.orderBy(orderBy, SortDirection.ASC);
  }

  public QueryBuilder firstResult(int firstResult) {
    this.firstResult = firstResult;
    return this;
  }

  public QueryBuilder maxResults(int maxResults) {
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
    String selectIfNonSpecified = nativeQuery ? "*" : from.getAlias();
    this.select = Objects.nonNull(select) ? select : new Select(selectIfNonSpecified);
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
    Objects.requireNonNull(entityManager, "It is not allowed to execute a query without inject a entity manager");

    String queryString = this.getQueryString();
    TypedQuery<T> query = this.entityManager.createQuery(queryString, resultClass)
      .setFirstResult(firstResult)
      .setMaxResults(maxResults);
    this.whereGroup.getParameters().forEach(query::setParameter);
    return query;
  }

  public String getJoinsString() {
    return this.joinGroup.getOperation();
  }

  public String getWheresString() {
    return this.whereGroup.getOperation();
  }

  public Map<String, Object> getParameters() {
    return this.whereGroup.getParameters();
  }

  public Query getNativeQuery() {
    Objects.requireNonNull(entityManager, "It is not allowed to execute a query without inject a entity manager");

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

  public <T> List<T> getResultListToPojo(Class<T> resultType) {
    return this.getResultList(AliasToPojoResultMapper.from(resultType));
  }

  @Override
  public String toString() {
    return this.getQueryString();
  }

}
