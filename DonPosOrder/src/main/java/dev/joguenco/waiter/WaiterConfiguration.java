package dev.joguenco.waiter;

import io.micronaut.context.annotation.ConfigurationProperties;
import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("donpos.waiter")
public class WaiterConfiguration {

  // Roles de DonPos que pueden entrar a la app Waiter (nombre del rol sin " role")
  private List<String> roles = List.of("Command");

  // Si el celular deja de dar señales este tiempo, se suelta el candado de la mesa
  private Duration lockTimeout = Duration.ofMinutes(2);
}
