package dev.joguenco.order.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedEntity("volume_discount")
public class VolumeDiscount {

  @Id private Integer id;

  private String product;

  private double minimumQuantity;

  private String type;

  // Porcentaje de descuento
  private double value;

  private Boolean status;
}
