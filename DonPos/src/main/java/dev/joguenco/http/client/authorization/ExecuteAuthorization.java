package dev.joguenco.http.client.authorization;

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

    public ExecuteAuthorization(ServiceInfo service, String code, String number) {
        this.service = service;
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
