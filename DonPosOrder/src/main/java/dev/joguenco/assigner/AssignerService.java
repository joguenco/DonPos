package dev.joguenco.assigner;

import com.unicenta.pos.ticket.TicketInfo;
import com.unicenta.pos.ticket.UserInfo;
import dev.joguenco.assigner.model.SharedTicket;
import dev.joguenco.assigner.model.TableRow;
import dev.joguenco.assigner.repository.SharedTicketRepository;
import dev.joguenco.floor.model.Place;
import dev.joguenco.floor.repository.PlaceRepository;
import dev.joguenco.util.TicketSerializer;
import dev.joguenco.waiter.WaiterConfiguration;
import dev.joguenco.waiter.WaiterException;
import dev.joguenco.waiter.model.Waiter;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.http.HttpStatus;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

// Ocupar, entrar y salir de las mesas con la misma lógica de DonPos (JTicketsBagRestaurantMap)
@Slf4j
@Singleton
public class AssignerService {

  private static final String LOCKED = "locked";

  protected final SharedTicketRepository sharedTicketRepository;
  protected final PlaceRepository placeRepository;
  protected final TableLocks tableLocks;
  protected final WaiterConfiguration configuration;

  public AssignerService(
      SharedTicketRepository sharedTicketRepository,
      PlaceRepository placeRepository,
      TableLocks tableLocks,
      WaiterConfiguration configuration) {
    this.sharedTicketRepository = sharedTicketRepository;
    this.placeRepository = placeRepository;
    this.tableLocks = tableLocks;
    this.configuration = configuration;
  }

  public static WaiterException tableGone() {
    return new WaiterException(HttpStatus.GONE, "GONE", "La mesa ya fue cobrada en caja");
  }

  public List<TableDto> listTables(String floorId, Waiter waiter) {
    return sharedTicketRepository.findTables(floorId).stream()
        .map(row -> new TableDto(row.id(), row.name(), status(row, waiter), row.waiter()))
        .toList();
  }

  private TableDto.Status status(TableRow row, Waiter waiter) {
    if (row.ticketId() == null) {
      return TableDto.Status.FREE;
    }
    // Amarillo solo si el candado lo puso DonPos (la caja facturando); si lo puso el celular
    // de un mesero, la mesa se muestra como suya o de ese mesero
    if (LOCKED.equals(row.locked()) && !tableLocks.isHeld(row.id())) {
      return TableDto.Status.LOCKED;
    }
    return isOwner(row.waiter(), row.appUser(), waiter)
        ? TableDto.Status.MINE
        : TableDto.Status.TAKEN;
  }

  // DonPos anota el mesero en places.waiter y no lo cambia aunque la caja abra la mesa.
  // appuser sí cambia a quien la abrió último, por eso solo cuenta si la mesa no tiene mesero.
  private boolean isOwner(String placeWaiter, String appUser, Waiter waiter) {
    if (placeWaiter != null && !placeWaiter.isBlank()) {
      return placeWaiter.equals(waiter.name());
    }
    return waiter.id().equals(appUser);
  }

  // El mesero entra a una mesa libre (la ocupa) o a una suya (le pone el candado)
  public void open(String placeId, Waiter waiter) {
    Place place =
        placeRepository
            .findById(placeId)
            .orElseThrow(
                () -> new WaiterException(HttpStatus.NOT_FOUND, "NOT_FOUND", "La mesa no existe"));
    Optional<SharedTicket> current = sharedTicketRepository.findById(placeId);

    if (current.isEmpty()) {
      occupy(place, waiter);
      return;
    }

    if (!isOwner(place.getWaiter(), current.get().getAppUser(), waiter)) {
      String owner =
          place.getWaiter() == null || place.getWaiter().isBlank()
              ? "otro usuario"
              : place.getWaiter();
      throw new WaiterException(HttpStatus.CONFLICT, "TAKEN", "Esta mesa es de " + owner);
    }

    // Si el candado ya era suyo (se le cerró la app o usa otro celular) vuelve a entrar
    if (!tableLocks.isHeldBy(placeId, waiter.id()) && sharedTicketRepository.lock(placeId) == 0) {
      throw new WaiterException(
          HttpStatus.CONFLICT, "LOCKED", "La están facturando en caja, intenta en un momento");
    }
    tableLocks.hold(placeId, waiter.id());
  }

  // Igual que DonPos al tocar una mesa vacía: pedido nuevo a nombre del mesero y con candado
  private void occupy(Place place, Waiter waiter) {
    TicketInfo ticket = new TicketInfo();
    ticket.setUser(new UserInfo(waiter.id(), waiter.name()));

    SharedTicket sharedTicket =
        new SharedTicket(
            place.getId(),
            ticketName(ticket, waiter),
            TicketSerializer.write(ticket),
            waiter.id(),
            0,
            LOCKED);

    try {
      sharedTicketRepository.save(sharedTicket);
    } catch (DataAccessException e) {
      // Otro mesero la ocupó en el mismo instante: la base solo acepta una fila por mesa
      if (sharedTicketRepository.existsById(place.getId())) {
        throw new WaiterException(HttpStatus.CONFLICT, "TAKEN", "Acaba de ocuparla otro mesero");
      }
      throw e;
    }

    placeRepository.occupy(place.getId(), waiter.name(), ticket.getId());
    tableLocks.hold(place.getId(), waiter.id());
  }

  // Cada señal del celular renueva el candado y avisa si la mesa ya se cobró
  public void signal(String placeId, Waiter waiter) {
    if (!sharedTicketRepository.existsById(placeId)) {
      tableLocks.release(placeId);
      throw tableGone();
    }
    if (!tableLocks.isHeldBy(placeId, waiter.id())) {
      throw new WaiterException(
          HttpStatus.CONFLICT, "NOT_HELD", "Se perdió la señal con la mesa, vuelve a entrar");
    }
    tableLocks.hold(placeId, waiter.id());
  }

  public void close(String placeId, Waiter waiter) {
    if (!tableLocks.isHeldBy(placeId, waiter.id())) {
      return;
    }
    tableLocks.release(placeId);
    leave(placeId);
  }

  // Celular apagado o sin WiFi dentro de una mesa: pasado el tiempo se suelta el candado
  @Scheduled(fixedDelay = "30s")
  public void releaseExpired() {
    for (String placeId : tableLocks.takeExpired(configuration.getLockTimeout())) {
      log.info("Releasing table without signal: {}", placeId);
      try {
        leave(placeId);
      } catch (RuntimeException e) {
        log.error("Could not release table {}", placeId, e);
      }
    }
  }

  // Quita el candado; si el mesero no pidió nada, la mesa vuelve a quedar libre
  private void leave(String placeId) {
    Optional<SharedTicket> current = sharedTicketRepository.findById(placeId);
    if (current.isEmpty()) {
      return;
    }

    TicketInfo ticket = TicketSerializer.read(current.get().getContent());
    if (ticket.getLinesCount() == 0) {
      sharedTicketRepository.deleteById(placeId);
      placeRepository.release(placeId);
    } else {
      sharedTicketRepository.unlock(placeId);
    }
  }

  // El mismo nombre que DonPos le pone al pedido: mesero - (hora) - cliente
  public String ticketName(TicketInfo ticket, Waiter waiter) {
    List<String> parts = new ArrayList<>();
    parts.add(waiter.name());

    Date date = ticket.getDate();
    parts.add("(" + new SimpleDateFormat("hh:mm").format(date) + " " + date.getTime() % 1000 + ")");

    if (ticket.getCustomer() != null) {
      parts.add(ticket.getCustomer().getName());
    }
    return String.join(" - ", parts);
  }
}
