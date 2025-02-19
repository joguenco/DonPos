package dev.joguenco.pos.subscription;

import com.unicenta.data.gui.ListCellRendererBasic;
import com.unicenta.data.loader.TableDefinition;
import com.unicenta.data.user.EditorRecord;
import com.unicenta.data.user.ListProvider;
import com.unicenta.data.user.ListProviderCreator;
import com.unicenta.data.user.SaveProvider;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.panels.JPanelTable;
import javax.swing.ListCellRenderer;

/**
 *
 * @author Jorge Luis
 */
public class SubscriptionPanel extends JPanelTable {

    private TableDefinition tdSubscription;
    private SubscriptionEditor subscriptionEditor;

    @Override
    protected void init() {
        DataLogicSubscription dlSubscription = (DataLogicSubscription) app.getBean("dev.joguenco.pos.subscription.DataLogicSubscription");
        tdSubscription = dlSubscription.getTableSubscription();
        subscriptionEditor = new SubscriptionEditor(app, dirty);

    }

    @Override
    public EditorRecord getEditor() {
        return subscriptionEditor;
    }

    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(tdSubscription);
    }

    @Override
    public SaveProvider getSaveProvider() {
        return new SaveProvider(tdSubscription);
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Subscription");
    }

    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tdSubscription.getRenderStringBasic(new int[]{1}));
    }
}
