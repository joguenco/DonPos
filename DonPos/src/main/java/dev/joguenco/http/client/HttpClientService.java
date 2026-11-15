package dev.joguenco.http.client;

import com.unicenta.basic.BasicException;
import dev.joguenco.pos.service.ServiceInfo;

/**
 * @author <Jorge Luis from https://joguenco.dev>
 */
public class HttpClientService {

    private final ServiceInfo service;

    public HttpClientService(ServiceInfo service) throws BasicException {
        this.service = service;
    }

    public ServiceGenerator generator() {
        return new ServiceGenerator(service.getUrl(), service.getTimeout());
    }

    // Dice como se autentica este servicio: Token lo usa ReIdi, y X-API-KEY
    // manda la clave en la cabecera para autorizar documentos.
    public String getAuthenticationMethod() {
        return service.getAuthenticationMethod();
    }

    public String getToken() {
        return service.getToken();
    }
}
