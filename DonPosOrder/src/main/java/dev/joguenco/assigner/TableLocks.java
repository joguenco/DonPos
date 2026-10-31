package dev.joguenco.assigner;

import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Candados que pusieron los celulares. DonPos solo guarda 'locked' sin hora ni dueño,
// por eso aquí se recuerda qué mesero lo puso y cuándo dio su última señal.
@Singleton
public class TableLocks {

  private record Hold(String waiterId, Instant lastSignal) {}

  // Mesa (places.id) -> candado
  private final Map<String, Hold> holds = new ConcurrentHashMap<>();

  // Pone o renueva el candado del mesero (cada señal del celular pasa por aquí)
  public void hold(String placeId, String waiterId) {
    holds.put(placeId, new Hold(waiterId, Instant.now()));
  }

  public boolean isHeldBy(String placeId, String waiterId) {
    Hold hold = holds.get(placeId);
    return hold != null && hold.waiterId().equals(waiterId);
  }

  // Si algún celular puso el candado; si no fue ninguno, lo puso DonPos (la caja)
  public boolean isHeld(String placeId) {
    return holds.containsKey(placeId);
  }

  public void release(String placeId) {
    holds.remove(placeId);
  }

  // Quita y devuelve las mesas cuyos celulares llevan callados más de timeout
  public List<String> takeExpired(Duration timeout) {
    Instant limit = Instant.now().minus(timeout);
    List<String> expired = new ArrayList<>();

    holds.forEach(
        (placeId, hold) -> {
          // remove(key, value) no borra si justo llegó una señal nueva
          if (hold.lastSignal().isBefore(limit) && holds.remove(placeId, hold)) {
            expired.add(placeId);
          }
        });

    return expired;
  }
}
