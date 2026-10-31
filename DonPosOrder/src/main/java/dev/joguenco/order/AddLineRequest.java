package dev.joguenco.order;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record AddLineRequest(@NotBlank String productId) {}
