package dev.joguenco.order.model;

import io.micronaut.core.annotation.Introspected;

// Un lote activo de un producto (products_lots + lots)
@Introspected
public record ProductLot(String lot) {}
