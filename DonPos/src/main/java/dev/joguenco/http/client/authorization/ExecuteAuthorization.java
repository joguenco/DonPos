package dev.joguenco.http.client.authorization;

import com.unicenta.pos.ticket.TicketInfo;
import dev.joguenco.pos.service.ServiceInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <Jorge Luis from https://joguenco.dev>
 */
@Slf4j
public class ExecuteAuthorization extends Thread {

    private final ServiceInfo service;
    private final String code;
    private final String number;

    public ExecuteAuthorization(ServiceInfo service, TicketInfo ticket) {
        this(service, ticket.getCode(), ticket.getSerieNumber());
    }

    // La liquidacion, la retencion y la guia no son tickets, asi que entran por
    // aqui con el codigo y el numero, que es lo unico que se usaba del ticket.
    public ExecuteAuthorization(ServiceInfo app, String code, String number) {
        this.service = app;
        this.code = code;
        this.number = number;
    }

    @Override
    public void run() {
        int attempts = 1;

        do {
            if (send()) {
                break;
            }
            try {
                Thread.sleep(60000L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        } while (attempts++ < 3);
    }

    public Boolean send() {
        try {
            AuthorizeClient a = new AuthorizeClient(service);
            final var response = a.post(code, number);
            log.info(code + " " + number + " -> " + response.getStatus());

            if ("ERROR".equalsIgnoreCase(response.getStatus())) {
                return false;
            }

            return true;
        } catch (Exception ex) {
            log.error(ExecuteAuthorization.class.getName() + " " + ex.getMessage());
            return false;
        }
    }
}
