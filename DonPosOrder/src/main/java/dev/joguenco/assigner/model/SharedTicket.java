package dev.joguenco.assigner.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una mesa ocupada de DonPos: el id es el mismo de la mesa (places.id)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedEntity("sharedtickets")
public class SharedTicket {

  @Id private String id;

  private String name;

  // El pedido (TicketInfo) guardado como objeto Java
  private byte[] content;

  // Id de la persona dueña de la mesa (people.id)
  @MappedProperty("appuser")
  private String appUser;

  @MappedProperty("pickupid")
  private int pickupId;

  // 'locked' mientras alguien tiene la mesa abierta, NULL cuando nadie
  private String locked;
}
