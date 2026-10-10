package dev.resolvedor.pos.inventory.lot;

import com.unicenta.data.gui.ListCellRendererBasic;
import com.unicenta.data.loader.ComparatorCreator;
import com.unicenta.data.loader.TableDefinition;
import com.unicenta.data.loader.Vectorer;
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
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
public class LotPanel extends JPanelTable {

    private TableDefinition tdLot;
    private LotEditor lotEditor;

    @Override
    protected void init() {
        var dlLot = (DataLogicLot) app.getBean("dev.resolvedor.pos.inventory.lot.DataLogicLot");
        tdLot = dlLot.getTableLot();
        lotEditor = new LotEditor(app, dirty);
    }

    @Override
    public EditorRecord getEditor() {
        return lotEditor;
    }

    @Override
    public ListProvider getListProvider() {
        return new ListProviderCreator(tdLot);
    }

    @Override
    public SaveProvider getSaveProvider() {
        return new SaveProvider(tdLot);
    }
    
    @Override
    public Vectorer getVectorer() {
        return tdLot.getVectorerBasic(new int[] {1, 2});
    }
    
    @Override
    public ComparatorCreator getComparatorCreator() {
        return tdLot.getComparatorCreator(new int[] {1, 2});
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Lot");
    }

    @Override
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tdLot.getRenderStringBasic(new int[]{1, 2, 3}));
    }
}
