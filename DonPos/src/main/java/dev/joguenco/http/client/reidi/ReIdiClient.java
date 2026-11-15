package dev.joguenco.http.client.reidi;

import com.unicenta.basic.BasicException;
import com.unicenta.pos.forms.AppLocal;
import dev.joguenco.http.client.HttpClientService;
import dev.joguenco.http.client.entity.EntityResponse;
import dev.joguenco.http.client.entity.EntityService;
import dev.joguenco.pos.service.ServiceInfo;
import java.awt.HeadlessException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;

/**
 *
 * @author Jorge Luis from https://joguenco.dev
 * @web jorgeluis@joguenco.dev
 */
@Slf4j
public class ReIdiClient {

    public ReIdiResponse get(String identification, ServiceInfo service) {

        try {
            var httpClient = new HttpClientService(service);

            if (!service.getStatus()) {
                return new ReIdiResponse("Service is disable");
            }

            final var userAgent = AppLocal.APP_NAME + "/" + AppLocal.APP_VERSION;

            var httpService = httpClient.generator()
                    .createService(EntityService.class, httpClient.getToken(), userAgent);
            var callSync = httpService.getEntity(identification);

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
