package dev.joguenco.util;

import com.unicenta.pos.ticket.TicketInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Lee y escribe sharedtickets.content igual que ImageUtils de DonPos: el objeto Java en bytes.
// Por eso TicketInfo y sus clases están copiados con el mismo paquete y serialVersionUID.
public final class TicketSerializer {

  private TicketSerializer() {}

  public static byte[] write(TicketInfo ticket) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      out.writeObject(ticket);
    } catch (IOException e) {
      throw new IllegalStateException("Could not write the table ticket", e);
    }
    return bytes.toByteArray();
  }

  public static TicketInfo read(byte[] content) {
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(content))) {
      return (TicketInfo) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new IllegalStateException("Could not read the table ticket", e);
    }
  }
}
