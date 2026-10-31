package dev.joguenco.security.model;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

// password es NULL en los usuarios sin clave de DonPos (Hashcypher ya lo contempla)
@MappedEntity("v_users")
public record User(
    @Id String id, String username, @Nullable String password, String role, Boolean status) {}
