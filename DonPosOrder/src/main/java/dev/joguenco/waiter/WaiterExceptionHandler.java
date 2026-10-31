package dev.joguenco.waiter;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {WaiterException.class, ExceptionHandler.class})
public class WaiterExceptionHandler
    implements ExceptionHandler<WaiterException, HttpResponse<WaiterExceptionHandler.Error>> {

  @Serdeable
  public record Error(String code, String message) {}

  @Override
  public HttpResponse<Error> handle(HttpRequest request, WaiterException exception) {
    return HttpResponse.<Error>status(exception.getStatus())
        .body(new Error(exception.getCode(), exception.getMessage()));
  }
}
