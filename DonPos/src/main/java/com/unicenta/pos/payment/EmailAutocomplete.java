package com.unicenta.pos.payment;

import dev.joguenco.xml.Provider;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public final class EmailAutocomplete {

    private EmailAutocomplete() {
    }

    public static void install(JTextField txtEmail, String xml) throws JAXBException {
        StringReader reader = new StringReader(xml);

        JAXBContext jaxbContext = JAXBContext.newInstance(Provider.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        Provider provider = (Provider) unmarshaller.unmarshal(reader);

        final List<String> domains = provider.getDomain();

        final JPopupMenu popup = new JPopupMenu();
        popup.setFocusable(false);
        final JList<String> list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFocusable(false);
        popup.add(new JScrollPane(list));

        final Runnable updatePopup = () -> {
            String text = txtEmail.getText();
            if (text == null) {
                popup.setVisible(false);
                return;
            }
            int caret = txtEmail.getCaretPosition();
            int at = text.lastIndexOf('@', Math.max(0, caret - 1));
            if (at < 0) {
                popup.setVisible(false);
                return;
            }
            String typed = text.substring(at + 1, Math.min(caret, text.length())).toLowerCase();
            List<String> matches = new ArrayList<>();
            for (String domain : domains) {
                if (domain.toLowerCase().startsWith(typed)) {
                    matches.add(domain);
                }
            }
            if (matches.isEmpty() || (matches.size() == 1 && matches.get(0).equalsIgnoreCase(typed))) {
                popup.setVisible(false);
                return;
            }
            list.setListData(matches.toArray(new String[0]));
            list.setSelectedIndex(0);
            int rows = Math.min(matches.size(), 8);
            popup.setPopupSize(Math.max(txtEmail.getWidth(), 220), rows * 22 + 4);
            if (txtEmail.isShowing()) {
                if (!popup.isVisible()) {
                    popup.show(txtEmail, 0, txtEmail.getHeight());
                }
            } else {
                popup.setVisible(false);
            }
        };

        txtEmail.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(updatePopup);
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(updatePopup);
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(updatePopup);
            }
        });

        final java.awt.event.ActionListener acceptSelection = e -> {
            String selected = list.getSelectedValue();
            if (selected == null) {
                return;
            }
            String text = txtEmail.getText();
            int caret = txtEmail.getCaretPosition();
            int at = text.lastIndexOf('@', Math.max(0, caret - 1));
            if (at < 0) {
                return;
            }
            String before = text.substring(0, at + 1);
            String after = caret < text.length() ? text.substring(caret) : "";
            String newText = before + selected + after;
            txtEmail.setText(newText);
            txtEmail.setCaretPosition((before + selected).length());
            popup.setVisible(false);
        };

        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                acceptSelection.actionPerformed(null);
            }
        });

        txtEmail.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (!popup.isVisible()) {
                    return;
                }
                int size = list.getModel().getSize();
                if (size == 0) {
                    return;
                }
                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_DOWN:
                        list.setSelectedIndex(Math.min(list.getSelectedIndex() + 1, size - 1));
                        list.ensureIndexIsVisible(list.getSelectedIndex());
                        e.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_UP:
                        list.setSelectedIndex(Math.max(list.getSelectedIndex() - 1, 0));
                        list.ensureIndexIsVisible(list.getSelectedIndex());
                        e.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_ENTER:
                    case java.awt.event.KeyEvent.VK_TAB:
                        acceptSelection.actionPerformed(null);
                        e.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_ESCAPE:
                        popup.setVisible(false);
                        e.consume();
                        break;
                    default:
                        break;
                }
            }
        });

        txtEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                popup.setVisible(false);
            }
        });
    }
}
