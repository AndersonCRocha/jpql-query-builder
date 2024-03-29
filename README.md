# Query Builder JPQL for JPA Queries (Under development)

This is a tool that helps you create queries with JPA using JPQL or native SQL


## How to install

#### Using Maven

Add this dependency in your pom.xml

```xml
<dependency>
  <groupId>io.github.andersoncrocha</groupId>
  <artifactId>jpql-query-builder</artifactId>
  <version>1.2.5</version>
</dependency>
```

#### Or download the jar of the lib 

[jpql-query-builder.jar](https://github.com/andersoncrocha/jpql-query-builder)


## How to use

#### Examples

```java
public List<Order> findOrdersWithItemsByClientAndSellerIfNotNull(Client client, Seller seller) {
  return QueryBuilder.newQuery(entityManager)
    .from(Order.class, "order")
    .joinFetch("order.items")
    .where("order.client = :client", client)
    .whereIf("order.seller = :seller", seller, Objects.nonNull(seller))
    .getResultList(Order.class);
}
```
