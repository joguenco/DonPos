package dev.resolvedor.pos.inventory.management;

import com.unicenta.format.Formats;
import com.unicenta.pos.ticket.ProductInfoExt;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author Jorge Luis
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
public class ProductRenderer extends DefaultListCellRenderer {
                
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
        
        ProductInfoExt prod = (ProductInfoExt) value;
       
        if (prod != null) {
                        
            setText("<html><center>" 
                    + prod.getReference() 
                    + " - " 
                    + prod.getName()
                    );            
        }
        return this;
    }      
}
