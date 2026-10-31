package dev.joguenco.security;

import dev.joguenco.security.repository.UserRepository;
import dev.joguenco.util.Hashcypher;
import dev.joguenco.waiter.WaiterService;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
class AuthenticationProviderUserPassword<B> implements HttpRequestAuthenticationProvider<B> {

  protected final UserRepository userRepository;
  protected final WaiterService waiterService;

  AuthenticationProviderUserPassword(UserRepository userRepository, WaiterService waiterService) {
    this.userRepository = userRepository;
    this.waiterService = waiterService;
  }

  @Override
  public AuthenticationResponse authenticate(
      @Nullable HttpRequest<B> httpRequest,
      @NonNull AuthenticationRequest<String, String> authenticationRequest) {
    final var username = authenticationRequest.getIdentity();
    final var password = authenticationRequest.getSecret();

    final var user = userRepository.findByUsername(username).orElse(null);

    if (user == null) {
      log.warn("User not found: {}", username);
      return AuthenticationResponse.failure(AuthenticationFailureReason.USER_NOT_FOUND);
    }
    if (!user.status()) {
      return AuthenticationResponse.failure(AuthenticationFailureReason.ACCOUNT_LOCKED);
    }

    if (Hashcypher.authenticate(password, user.password())) {
      // La clave es correcta, pero a la app Waiter solo entran los meseros
      if (!waiterService.isWaiterRole(user.role())) {
        log.warn("User is not a waiter: {}", username);
        return AuthenticationResponse.failure("Este usuario no es mesero");
      }
      return AuthenticationResponse.success(username);
    }

    return AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH);
  }
}
