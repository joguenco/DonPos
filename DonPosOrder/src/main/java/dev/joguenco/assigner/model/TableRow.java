package dev.joguenco.assigner.model;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

// Una mesa con lo que DonPos sabe de ella: si hay fila en sharedtickets, está ocupada
@Introspected
public record TableRow(
    String id,
    String name,
    @Nullable String waiter,
    @Nullable String ticketId,
    @Nullable String appUser,
    @Nullable String locked) {}
