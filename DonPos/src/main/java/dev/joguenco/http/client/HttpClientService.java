package dev.joguenco.http.client;

import com.unicenta.basic.BasicException;
import com.unicenta.pos.forms.AppView;
import dev.joguenco.pos.service.DataLogicService;
import dev.joguenco.pos.service.ServiceInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <Jorge Luis from https://resolvedor.dev>
 */
@Slf4j
public class HttpClientService {

    private final DataLogicService dlService;
    private ServiceInfo service;

    public HttpClientService(AppView app, String serviceName)  throws BasicException {
        dlService = (DataLogicService) app.getBean("dev.joguenco.pos.service.DataLogicService");        
        this.service = dlService.getServiceInfoByName(serviceName);
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

    public Boolean isActive(String serviceName) {
        try {
            var isActive = dlService.getServiceStatusByName(serviceName);
            
            if (isActive == null)
                return false;
            
            return isActive;
            
        } catch (BasicException ex) {
            log.error(this.getClass().getName() + " " + ex.getMessage());
            return false;
        }
    }
}
