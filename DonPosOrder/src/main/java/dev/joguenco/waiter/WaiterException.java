package dev.joguenco.waiter;

import io.micronaut.http.HttpStatus;
import lombok.Getter;

// Error que la app Waiter muestra tal cual: code para decidir qué hacer, message para el mesero
@Getter
public class WaiterException extends RuntimeException {

  private final HttpStatus status;
  private final String code;

  public WaiterException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}
