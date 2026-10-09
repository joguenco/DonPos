package dev.joguenco.pos.service;

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
public class ServicePanel extends JPanelTable {

    private TableDefinition tdService;
    private ServiceEditor serviceEditor;

    @Override
    protected void init() {
        DataLogicService dlService = (DataLogicService) app.getBean("dev.joguenco.pos.service.DataLogicService");
        tdService = dlService.getTableService();
        serviceEditor = new ServiceEditor(app, dirty);

    }

    @Override
    public EditorRecord getEditor() {
        return serviceEditor;
    }

    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(tdService);
    }

    @Override
    public SaveProvider getSaveProvider() {
        return new SaveProvider(tdService);
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Services");
    }

    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tdService.getRenderStringBasic(new int[]{1}));
    }
}
