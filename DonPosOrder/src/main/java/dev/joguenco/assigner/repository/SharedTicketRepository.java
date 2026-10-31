package dev.joguenco.assigner.repository;

import dev.joguenco.assigner.model.SharedTicket;
import dev.joguenco.assigner.model.TableRow;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface SharedTicketRepository extends GenericRepository<SharedTicket, String> {

  // Las mesas del piso en orden natural (Mesa 2 antes que Mesa 10)
  @Query(
      "SELECT p.id, p.name, p.waiter, s.id AS ticket_id, s.appuser, s.locked "
          + "FROM places p LEFT JOIN sharedtickets s ON s.id = p.id "
          + "WHERE p.floor = :floorId "
          + "ORDER BY LENGTH(p.name), p.name")
  List<TableRow> findTables(@NonNull String floorId);

  Optional<SharedTicket> findById(@NonNull String id);

  boolean existsById(@NonNull String id);

  // Si dos meseros ocupan la misma mesa a la vez, la llave primaria deja pasar solo a uno
  SharedTicket save(@NonNull SharedTicket sharedTicket);

  // Solo pone el candado si nadie lo tiene: devuelve 1 si lo puso, 0 si ya estaba puesto
  @Query("UPDATE sharedtickets SET locked = 'locked' WHERE id = :id AND locked IS NULL")
  long lock(@NonNull String id);

  @Query("UPDATE sharedtickets SET locked = NULL WHERE id = :id")
  long unlock(@NonNull String id);

  @Query(
      "UPDATE sharedtickets SET name = :name, content = :content, appuser = :appUser "
          + "WHERE id = :id")
  long updateContent(
      @NonNull String id, @NonNull String name, byte[] content, @NonNull String appUser);

  void deleteById(@NonNull String id);
}
