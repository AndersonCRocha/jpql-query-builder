# Query Builder JPQL for Hibernate Queries (Under development)

This is a tool that helps you create queries with Hibernate using JPQL or native SQL


## How to install

#### Using Maven

Add this dependency in your pom.xml

```xml
<dependency>
  <groupId>io.github.andersoncrocha</groupId>
  <artifactId>jpql-query-builder</artifactId>
  <version>1.0.1</version>
</dependency>
```

#### Or download the jar of the lib 

[jpql-query-builder.jar](https://github.com/andersoncrocha/jpql-query-builder)


## How to use

#### Examples

```java
public List<Person> findOrdersWithItemsByClientAndSellerIfNotNull(Client client, Seller seller) {
  return new QueryBuilder(entityManager)
    .from(Person.class, "order")
    .joinFetch("order.items")
    .where("order.client = :client", client)
    .whereIf("order.seller", seller, Objects.nonNull(seller))
    .getResultList(Person.class);
}
```
