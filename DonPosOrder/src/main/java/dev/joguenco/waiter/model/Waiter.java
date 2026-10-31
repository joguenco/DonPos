package dev.joguenco.waiter.model;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

// Un usuario de DonPos (tabla people): su id es el que DonPos guarda en sharedtickets.appuser
@MappedEntity("people")
public record Waiter(@Id String id, String name, String role) {}
