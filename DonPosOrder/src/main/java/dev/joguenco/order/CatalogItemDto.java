package dev.joguenco.order;

import io.micronaut.serde.annotation.Serdeable;

// Un producto del catálogo con su precio final (con IVA)
@Serdeable
public record CatalogItemDto(String id, String name, double price, boolean variablePrice) {}
