package dev.joguenco.order.repository;

import dev.joguenco.order.model.VolumeDiscount;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface VolumeDiscountRepository extends GenericRepository<VolumeDiscount, Integer> {

  // Mismo criterio que DataLogicSales.getVolumeDiscount
  @Query(
      "SELECT * FROM volume_discount "
          + "WHERE product = :productId AND minimum_quantity <= :quantity "
          + "AND status = TRUE AND type = 'percentage' "
          + "ORDER BY minimum_quantity DESC LIMIT 1")
  Optional<VolumeDiscount> findBest(@NonNull String productId, double quantity);
}
