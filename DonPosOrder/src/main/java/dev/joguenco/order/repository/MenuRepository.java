package dev.joguenco.order.repository;

import dev.joguenco.order.model.BundleComponent;
import dev.joguenco.order.model.MenuCategory;
import dev.joguenco.order.model.ProductLot;
import dev.joguenco.order.model.SaleProduct;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.List;
import java.util.Optional;

// Lo mismo que muestra la pantalla de ventas de DonPos (DataLogicSales)
@JdbcRepository(dialect = Dialect.MYSQL)
public interface MenuRepository extends GenericRepository<SaleProduct, String> {

  // getRootCategories: categorías principales y visibles
  @Query(
      "SELECT id, name FROM categories "
          + "WHERE parentid IS NULL AND catshowname = TRUE "
          + "ORDER BY catorder, name")
  List<MenuCategory> findCategories();

  // getProductCatalog: solo los productos que están en el catálogo (products_cat)
  @Query(
      "SELECT p.* FROM products p JOIN products_cat c ON c.product = p.id "
          + "WHERE p.category = :categoryId "
          + "ORDER BY c.catorder, p.name")
  List<SaleProduct> findProducts(@NonNull String categoryId);

  Optional<SaleProduct> findById(@NonNull String id);

  // countProdutLots / getLotOfProduct: los lotes activos del producto
  @Query(
      "SELECT pl.lot FROM products_lots pl JOIN lots l ON l.id = pl.lot "
          + "WHERE pl.product = :productId AND pl.status = TRUE AND l.status = TRUE")
  List<ProductLot> findLots(@NonNull String productId);

  // getProductsBundle: los productos que forman el combo
  @Query("SELECT product_bundle AS product_id FROM products_bundle WHERE product = :productId")
  List<BundleComponent> findBundleComponents(@NonNull String productId);
}
