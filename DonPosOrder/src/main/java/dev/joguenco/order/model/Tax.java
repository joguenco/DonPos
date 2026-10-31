package dev.joguenco.order.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedEntity("taxes")
public class Tax {

  @Id private String id;

  private String name;

  private String category;

  @MappedProperty("custcategory")
  private String custCategory;

  @MappedProperty("parentid")
  private String parentId;

  private double rate;

  @MappedProperty("ratecascade")
  private boolean rateCascade;

  @MappedProperty("rateorder")
  private Integer rateOrder;

  @MappedProperty("legalcode")
  private String legalCode;

  @MappedProperty("datestart")
  private LocalDateTime dateStart;
}
