package dev.joguenco.order.repository;

import dev.joguenco.order.model.Tax;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.List;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface TaxRepository extends GenericRepository<Tax, String> {

  // Mismo orden que DataLogicSales.getTaxList: el primero que calce es el que se usa
  @Query("SELECT * FROM taxes ORDER BY name")
  List<Tax> findAllOrderByName();
}
