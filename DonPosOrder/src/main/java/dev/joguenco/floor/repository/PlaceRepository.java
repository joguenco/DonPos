package dev.joguenco.floor.repository;

import dev.joguenco.floor.model.Place;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface PlaceRepository extends GenericRepository<Place, String> {

  @Query("select p.* from places p where p.floor = :floorId")
  List<Place> findAllByFloor(@NonNull String floorId);

  Optional<Place> findById(@NonNull String id);

  // Lo mismo que hace DonPos al ocupar una mesa: así en su mapa se ve el mesero y la hora
  @Query(
      "UPDATE places SET waiter = :waiter, ticketid = :ticketId, occupied = NOW() WHERE id = :id")
  void occupy(@NonNull String id, @NonNull String waiter, @NonNull String ticketId);

  // Lo mismo que hace DonPos al cobrar: la mesa queda limpia
  @Query(
      "UPDATE places SET customer = NULL, waiter = NULL, ticketid = NULL, occupied = NULL, "
          + "guests = 0 WHERE id = :id")
  void release(@NonNull String id);
}
