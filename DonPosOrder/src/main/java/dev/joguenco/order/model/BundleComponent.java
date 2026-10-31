package dev.joguenco.order.model;

import io.micronaut.core.annotation.Introspected;

// Un producto que forma parte de un combo (products_bundle)
@Introspected
public record BundleComponent(String productId) {}
