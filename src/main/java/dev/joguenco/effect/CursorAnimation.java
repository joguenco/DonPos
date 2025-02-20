package dev.joguenco.effect;

import java.awt.Component;
import javax.swing.JComponent;

/**
 *
 * @author Jorge Luis
 */
public class CursorAnimation {

    /**
     * Sets cursor for specified component to Wait cursor
     */
    public static void startWaitCursor(JComponent component) {
        Component c = component.getRootPane().getGlassPane();
        c.setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.WAIT_CURSOR));
        c.setVisible(true);
    }

    /**
     * Sets cursor for specified component to normal cursor
     */
    public static void stopWaitCursor(JComponent component) {
        Component c = component.getRootPane().getGlassPane();
        c.setCursor(java.awt.Cursor.getPredefinedCursor(
                java.awt.Cursor.DEFAULT_CURSOR));
        c.setVisible(false);
    }
}
