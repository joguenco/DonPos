package dev.joguenco.assigner;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TableDto(String id, String name, Status status, @Nullable String waiter) {

  public enum Status {
    // Nadie la ocupa
    FREE,
    // La ocupa este mesero
    MINE,
    // La ocupa otro mesero o la caja
    TAKEN,
    // La caja la tiene abierta en DonPos (facturando)
    LOCKED
  }
}
