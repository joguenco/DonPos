package dev.joguenco.http.client.reidi;

import com.unicenta.basic.BasicException;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.forms.AppView;
import dev.joguenco.http.client.HttpClientSubscription;
import dev.joguenco.http.client.entity.EntityResponse;
import dev.joguenco.http.client.entity.EntityService;
import java.awt.HeadlessException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

/**
 *
 * @author Jorge Luis from https://joguenco.dev
 * @web jorgeluis@resolvedor.dev
 */
@Slf4j
public class ReIdiClient {

    public ReIdiResponse get(String identification, AppView appView) {
        final var serviceName = "ReIdi";
        try {
            var httpClient = new HttpClientSubscription(appView);

            if (!httpClient.isActive(serviceName)) {
                return new ReIdiResponse("Service is disable");
            }

            final var userAgent = AppLocal.APP_NAME + "/" + AppLocal.APP_VERSION;

            var service = httpClient.generator(serviceName)
                    .createService(EntityService.class, httpClient.getToken(), userAgent);
            var callSync = service.getEntity(identification);

            Response<EntityResponse> response = callSync.execute();
            if (response.isSuccessful()) {
                EntityResponse entity = response.body();
                return new ReIdiResponse(new ReIdiData(entity.getName()));
            } else {
                return new ReIdiResponse("Unsuccessful response");
            }
        } catch (IllegalArgumentException | HeadlessException | IOException | BasicException ex) {
            log.error(this.getClass().getName() + " " + ex.getMessage());
            return new ReIdiResponse(ex.getMessage());
        }
    }
}
