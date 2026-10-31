package dev.joguenco.order;

import dev.joguenco.order.model.MenuCategory;
import dev.joguenco.waiter.WaiterService;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;
import java.util.List;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/donpos/order/v1/order")
public class OrderController {

  protected final OrderService orderService;
  protected final WaiterService waiterService;

  public OrderController(OrderService orderService, WaiterService waiterService) {
    this.orderService = orderService;
    this.waiterService = waiterService;
  }

  @Get("/categories")
  public List<MenuCategory> listCategories() {
    return orderService.listCategories();
  }

  @Get("/categories/{categoryId}/products")
  public List<CatalogItemDto> listProducts(String categoryId) {
    return orderService.listProducts(categoryId);
  }

  // Lo que ya se pidió en la mesa (el mesero debe estar dentro)
  @Get("/{placeId}")
  public OrderDto getOrder(String placeId, Authentication authentication) {
    return orderService.getOrder(placeId, waiterService.require(authentication));
  }

  @Post("/{placeId}/lines")
  public OrderDto addProduct(
      String placeId, @Valid @Body AddLineRequest request, Authentication authentication) {
    return orderService.addProduct(
        placeId, request.productId(), waiterService.require(authentication));
  }

  // index = posición de la línea en el pedido (0 es la primera)
  @Delete("/{placeId}/lines/{index}")
  public OrderDto removeLine(String placeId, int index, Authentication authentication) {
    return orderService.removeLine(placeId, index, waiterService.require(authentication));
  }
}
