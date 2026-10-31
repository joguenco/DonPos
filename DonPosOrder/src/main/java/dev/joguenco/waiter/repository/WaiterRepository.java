package dev.joguenco.waiter.repository;

import dev.joguenco.waiter.model.Waiter;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface WaiterRepository extends GenericRepository<Waiter, String> {

  // Igual que la vista v_users, pero con el id real de la persona
  @Query(
      "SELECT p.id, p.name, REPLACE(r.name, ' role', '') AS role "
          + "FROM people p JOIN roles r ON p.role = r.id "
          + "WHERE p.name = :name AND p.visible = TRUE")
  Optional<Waiter> findByName(@NonNull String name);
}
