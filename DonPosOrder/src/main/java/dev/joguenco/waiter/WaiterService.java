package dev.joguenco.waiter;

import dev.joguenco.waiter.model.Waiter;
import dev.joguenco.waiter.repository.WaiterRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Singleton;

@Singleton
public class WaiterService {

  protected final WaiterRepository waiterRepository;
  protected final WaiterConfiguration configuration;

  public WaiterService(WaiterRepository waiterRepository, WaiterConfiguration configuration) {
    this.waiterRepository = waiterRepository;
    this.configuration = configuration;
  }

  public boolean isWaiterRole(String role) {
    return configuration.getRoles().contains(role);
  }

  // El token solo trae el nombre; aquí se busca la persona y se confirma que siga siendo mesero
  public Waiter require(Authentication authentication) {
    return waiterRepository
        .findByName(authentication.getName())
        .filter(waiter -> isWaiterRole(waiter.role()))
        .orElseThrow(
            () ->
                new WaiterException(
                    HttpStatus.FORBIDDEN, "NOT_WAITER", "Este usuario no es mesero"));
  }
}
