package dev.joguenco.http.client.kent;

import com.unicenta.basic.BasicException;
import com.unicenta.pos.forms.AppLocal;
import dev.joguenco.http.client.HttpClientService;
import dev.joguenco.pos.service.ServiceInfo;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;

/**
 * Muestra los datos del cliente en la tablet del cajero por medio del servidor
 * Kent, configurado en Mantenimiento > Servicios con el nombre "Kent".
 *
 * Los envios salen por detras en un solo hilo: el cajero nunca espera aunque el
 * servidor este apagado, y los mensajes llegan en orden (primero mostrar,
 * despues limpiar). Si un envio falla solo queda en el log y la venta sigue.
 */
@Slf4j
public class KentClient {

    private static final ExecutorService sender = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable, "Kent");
        thread.setDaemon(true);
        return thread;
    });

    private KentService httpService;

    public KentClient(ServiceInfo service) {
        try {
            var httpClient = new HttpClientService(service);

            if (isActive() && service.getStatus()) {
                // La API va en la cabecera X-API-KEY, igual que en Autorizar
                httpService = httpClient.generator().createServiceWithApiKey(
                        KentService.class,
                        httpClient.getToken(),
                        AppLocal.APP_NAME + "/" + AppLocal.APP_VERSION);
            }
        } catch (BasicException | IllegalArgumentException ex) {
            log.error(this.getClass().getName() + " " + ex.getMessage());
        }
    }

    // Falso cuando el servicio Kent no existe, esta desactivado o su URL es invalida
    public boolean isActive() {
        return httpService != null;
    }

    public void send(String cashier, KentCustomer customer) {
        execute(httpService.sendMessage(new KentMessage(cashier, customer)));
    }

    public void clear(String cashier) {
        execute(httpService.clearMessage(new KentMessage(cashier, null)));
    }

    private void execute(Call<Void> call) {
        sender.submit(() -> {
            try {
                var response = call.execute();

                // 401 = la API de Mantenimiento > Servicios no es la del servidor
                if (!response.isSuccessful()) {
                    log.warn(this.getClass().getName() + " HTTP " + response.code());
                }
            } catch (IOException | RuntimeException ex) {
                log.warn(this.getClass().getName() + " " + ex.getMessage());
            }
        });
    }
}
