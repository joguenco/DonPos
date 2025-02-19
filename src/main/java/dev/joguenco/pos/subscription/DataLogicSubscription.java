package dev.joguenco.pos.subscription;

import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.Datas;
import com.unicenta.data.loader.PreparedSentence;
import com.unicenta.data.loader.SerializerWriteString;
import com.unicenta.data.loader.Session;
import com.unicenta.data.loader.TableDefinition;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.BeanFactoryDataSingle;

/**
 *
 * @author Jorge Luis
 */
public class DataLogicSubscription extends BeanFactoryDataSingle {

    private Session s;
    private TableDefinition tdSubscription;

    @Override
    public void init(Session s) {
        this.s = s;

        tdSubscription = new TableDefinition(s,
                "subscriptions",
                new String[]{"id", "name", "url", "token", "timeout", "status"},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.INT ,Datas.BOOLEAN,},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.STRING, Formats.STRING,
                    Formats.INT ,Formats.BOOLEAN,},
                new int[]{0});
    }

    public final TableDefinition getTableSubscription() {
        return tdSubscription;
    }

    public final PreparedSentence getSubscriptionInfo() {
        return new PreparedSentence(s,
                "select id, name, url, token, timeout, status"
                + "from subscriptions "
                + "where id = ?",
                SerializerWriteString.INSTANCE,
                (DataRead dr) -> {
                    var subscriptionInfo = new SubscriptionInfo();
                    subscriptionInfo.readValues(dr);

                    return subscriptionInfo;
                });
    }
}
