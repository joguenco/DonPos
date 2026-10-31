package dev.joguenco.assigner;

import dev.joguenco.order.OrderDto;
import dev.joguenco.order.OrderService;
import dev.joguenco.waiter.WaiterService;
import dev.joguenco.waiter.model.Waiter;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import java.util.List;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/donpos/order/v1/assigner")
public class AssignerController {

  protected final AssignerService assignerService;
  protected final OrderService orderService;
  protected final WaiterService waiterService;

  public AssignerController(
      AssignerService assignerService, OrderService orderService, WaiterService waiterService) {
    this.assignerService = assignerService;
    this.orderService = orderService;
    this.waiterService = waiterService;
  }

  // Mesas del piso con su estado para este mesero
  @Get("/floors/{floorId}/tables")
  public List<TableDto> listTables(String floorId, Authentication authentication) {
    return assignerService.listTables(floorId, waiterService.require(authentication));
  }

  // Entrar a la mesa: la ocupa o le pone el candado, y devuelve lo que ya se pidió
  @Post("/{placeId}/open")
  public OrderDto open(String placeId, Authentication authentication) {
    Waiter waiter = waiterService.require(authentication);
    assignerService.open(placeId, waiter);
    return orderService.getOrder(placeId, waiter);
  }

  // "Sigo aquí": el celular lo manda cada 30 s mientras está dentro de la mesa
  @Post("/{placeId}/signal")
  @Status(HttpStatus.NO_CONTENT)
  public void signal(String placeId, Authentication authentication) {
    assignerService.signal(placeId, waiterService.require(authentication));
  }

  // Salir de la mesa: sigue ocupada, pero sin candado
  @Post("/{placeId}/close")
  @Status(HttpStatus.NO_CONTENT)
  public void close(String placeId, Authentication authentication) {
    assignerService.close(placeId, waiterService.require(authentication));
  }
}
