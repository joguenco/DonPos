package dev.joguenco.http.client;

import com.unicenta.basic.BasicException;
import com.unicenta.pos.forms.AppView;

import dev.joguenco.pos.subscription.DataLogicSubscription;
import dev.joguenco.pos.subscription.SubscriptionInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpClientSubscription {

    private final DataLogicSubscription dlSubscription;
    private SubscriptionInfo subscription;

    public HttpClientSubscription(AppView app) {
        dlSubscription = (DataLogicSubscription) app.getBean("dev.joguenco.pos.subscription.DataLogicSubscription");        
    }

    public ServiceGenerator generator(String serviceName) throws BasicException {
        this.subscription = dlSubscription.getSubscriptionInfoByName(serviceName);
        return new ServiceGenerator(subscription.getUrl());
    }

    public String getToken() {
        return subscription.getToken();
    }
    
    public Boolean isActive(String serviceName) {
        try {
            return dlSubscription.getSubscriptionStatusByName(serviceName);
        } catch (BasicException ex) {
            log.error(HttpClientSubscription.class.getName() + " " + ex.getMessage());
            return false;
        }
    }
}
