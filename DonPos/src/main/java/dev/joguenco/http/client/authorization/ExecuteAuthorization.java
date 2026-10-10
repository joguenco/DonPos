package dev.joguenco.http.client.authorization;

import com.unicenta.pos.forms.AppView;
import com.unicenta.pos.ticket.TicketInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <Jorge Luis from https://joguenco.dev>
 */
@Slf4j
public class ExecuteAuthorization extends Thread {

    private final AppView app;
    private final String code;
    private final String number;

    public ExecuteAuthorization(AppView app, TicketInfo ticket) {
        this(app, ticket.getCode(), ticket.getSerieNumber());
    }

    // La liquidacion, la retencion y la guia no son tickets, asi que entran por
    // aqui con el codigo y el numero, que es lo unico que se usaba del ticket.
    public ExecuteAuthorization(AppView app, String code, String number) {
        this.app = app;
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
            AuthorizeClient a = new AuthorizeClient(app);
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
