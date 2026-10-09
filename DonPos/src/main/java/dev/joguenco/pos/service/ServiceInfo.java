package dev.joguenco.pos.service;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.SerializableRead;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Jorge Luis
 */
@RequiredArgsConstructor
public class ServiceInfo implements SerializableRead {

    @Getter @Setter private String id;
    @Getter @Setter private String name;
    @Getter @Setter private String url;
    @Getter @Setter private String authenticationMethod;
    @Getter @Setter private String token;
    @Getter @Setter private String username;
    @Getter @Setter private String password;
    @Getter @Setter private Integer timeout;
    @Getter @Setter private Boolean status;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(0);
        name = dr.getString(1);
        url = dr.getString(2);
        authenticationMethod = dr.getString(3);
        token = dr.getString(4);
        username = dr.getString(5);
        password = dr.getString(6);
        timeout = dr.getInt(7);
        status = dr.getBoolean(8);
    }
}
