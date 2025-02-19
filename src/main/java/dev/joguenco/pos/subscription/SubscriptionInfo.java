package dev.joguenco.pos.subscription;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.SerializableRead;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Jorge Luis
 */
@RequiredArgsConstructor
public class SubscriptionInfo implements SerializableRead {

    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String url;
    @Getter
    @Setter
    private String token;
    @Getter
    @Setter
    private Integer timeout;
    @Getter
    @Setter
    private Boolean status;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(0);
        name = dr.getString(1);
        url = dr.getString(2);
        token = dr.getString(3);
        timeout = dr.getInt(4);
        status = dr.getBoolean(5);
    }
}
