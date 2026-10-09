package dev.joguenco.pos.service;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.Datas;
import com.unicenta.data.loader.PreparedSentence;
import com.unicenta.data.loader.SerializerWriteString;
import com.unicenta.data.loader.Session;
import com.unicenta.data.loader.TableDefinition;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.BeanFactoryDataSingle;

/**
 * @author Jorge Luis
 */
public class DataLogicService extends BeanFactoryDataSingle {

    private Session s;
    private TableDefinition tdService;

    @Override
    public void init(Session s) {
        this.s = s;

        tdService =
                new TableDefinition(
                        s,
                        "services",
                        new String[] {
                            "id",
                            "name",
                            "url",
                            "authentication_method",
                            "token",
                            "username",
                            "password",
                            "timeout",
                            "status"
                        },
                        new Datas[] {
                            Datas.STRING,
                            Datas.STRING,
                            Datas.STRING,
                            Datas.STRING,
                            Datas.STRING,
                            Datas.STRING,
                            Datas.STRING,
                            Datas.INT,
                            Datas.BOOLEAN,
                        },
                        new Formats[] {
                            Formats.STRING,
                            Formats.STRING,
                            Formats.STRING,
                            Formats.STRING,
                            Formats.STRING,
                            Formats.STRING,
                            Formats.STRING,
                            Formats.INT,
                            Formats.BOOLEAN,
                        },
                        new int[] {0});
    }

    public final TableDefinition getTableService() {
        return tdService;
    }

    public final PreparedSentence getServiceInfo() {
        return new PreparedSentence(
                s,
                "select id, name, url, authentication_method, token, username, password, timeout, status "
                        + "from services "
                        + "where id = ?",
                SerializerWriteString.INSTANCE,
                (DataRead dr) -> {
                    var serviceInfo = new ServiceInfo();
                    serviceInfo.readValues(dr);

                    return serviceInfo;
                });
    }

    public final ServiceInfo getServiceInfoByName(String name) throws BasicException {
        return (ServiceInfo)
                new PreparedSentence(
                                s,
                                "select url, authentication_method, token, username, password, timeout "
                                        + "from services "
                                        + "where name = ? "
                                        + "and status = true",
                                SerializerWriteString.INSTANCE,
                                (DataRead dr) -> {
                                    var s = new ServiceInfo();

                                    s.setUrl(dr.getString(1));
                                    s.setAuthenticationMethod(dr.getString(2));
                                    s.setToken(dr.getString(3));
                                    s.setUsername(dr.getString(4));
                                    s.setPassword(dr.getString(5));
                                    s.setTimeout(dr.getInt(6));

                                    return s;
                                })
                        .find(name);
    }

    public final Boolean getServiceStatusByName(String name) throws BasicException {
        return (Boolean)
                new PreparedSentence(
                                s,
                                "select status " + "from services " + "where name = ?",
                                SerializerWriteString.INSTANCE,
                                (DataRead dr) -> {
                                    return dr.getBoolean(1);
                                })
                        .find(name);
    }
}
