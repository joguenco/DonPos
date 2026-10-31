package dev.joguenco.order;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

// Lo que ya se pidió en una mesa; los precios van con IVA
@Serdeable
public record OrderDto(String placeId, String placeName, List<Line> lines, double total) {

  @Serdeable
  public record Line(
      int index, String productId, String name, double quantity, double price, double total) {}
}
