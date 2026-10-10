package dev.resolvedor.pos.sales;

import com.unicenta.format.Formats;
import com.unicenta.pos.ticket.TicketInfo;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author Jorge Luis
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
public class TicketRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);

        var ticket = (TicketInfo) value;

        if (ticket != null) {

            setText("<html><center>"
                    + ticket.getCode()
                    + " "
                    + ticket.getSerieNumber()
                    + " - "
                    + Formats.TIMESTAMP.formatValue(ticket.getDate())
            );
        }
        return this;
    }
}
