package dev.resolvedor.pos.inventory.lot;

import com.github.f4b6a3.uuid.UuidCreator;
import com.unicenta.basic.BasicException;
import com.unicenta.beans.JCalendarDialog;
import com.unicenta.data.user.DirtyManager;
import com.unicenta.data.user.EditorRecord;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.forms.AppView;
import dev.joguenco.error.ErrorMessage;
import java.awt.Component;
import java.util.Date;

import javax.swing.JPanel;

/**
 *
 * @author Jorge Luis
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
public class LotEditor extends JPanel implements EditorRecord {

    private AppView app;
    private Object id;

    public LotEditor(AppView app, DirtyManager dirty) {
        initComponents();

        this.app = app;
        txtName.getDocument().addDocumentListener(dirty);
        txtExpirationDate.getDocument().addDocumentListener(dirty);
        chkStatus.addActionListener(dirty);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblExpirationDate = new javax.swing.JLabel();
        txtExpirationDate = new javax.swing.JTextField();
        chkStatus = new javax.swing.JCheckBox();
        cmdExpirationDate = new javax.swing.JButton();
        cmdAssignToProduct = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(600, 300));

        lblName.setLabelFor(txtName);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        lblName.setText(bundle.getString("label.name")); // NOI18N

        lblExpirationDate.setLabelFor(txtName);
        lblExpirationDate.setText(bundle.getString("label.cardexpdate")); // NOI18N

        chkStatus.setText(bundle.getString("label.Status")); // NOI18N

        cmdExpirationDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/date.png"))); // NOI18N
        cmdExpirationDate.setPreferredSize(new java.awt.Dimension(38, 38));
        cmdExpirationDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdExpirationDateActionPerformed(evt);
            }
        });

        cmdAssignToProduct.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/payments.png"))); // NOI18N
        cmdAssignToProduct.setText(bundle.getString("form.productslist")); // NOI18N
        cmdAssignToProduct.setToolTipText("");
        cmdAssignToProduct.setPreferredSize(new java.awt.Dimension(38, 38));
        cmdAssignToProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAssignToProductActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(98, 98, 98)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblExpirationDate)
                    .addComponent(lblName))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkStatus)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(cmdAssignToProduct, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtExpirationDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(cmdExpirationDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(112, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtExpirationDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblExpirationDate)
                    .addComponent(cmdExpirationDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmdAssignToProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmdExpirationDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdExpirationDateActionPerformed
        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(txtExpirationDate.getText());
        } catch (BasicException e) {
            date = null;
        }
        date = JCalendarDialog.showCalendarTime(this, date);
        if (date != null) {
            txtExpirationDate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }//GEN-LAST:event_cmdExpirationDateActionPerformed

    private void cmdAssignToProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAssignToProductActionPerformed

        if (id != null) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    ProductLotDialog dialog = new ProductLotDialog(app, id, new javax.swing.JFrame()) {
                    };
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) {
                            dialog.setVisible(false);
                        }
                    });
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                }
            });
        }
    }//GEN-LAST:event_cmdAssignToProductActionPerformed

    @Override
    public void writeValueEOF() {
        id = null;
        txtName.setText(null);
        txtExpirationDate.setText(null);
        chkStatus.setSelected(false);

        txtName.setEnabled(false);
        txtExpirationDate.setEnabled(false);
        chkStatus.setEnabled(false);
        cmdExpirationDate.setEnabled(false);
    }

    @Override
    public void writeValueInsert() {
        id = null;
        txtName.setText(null);
        txtExpirationDate.setText(null);
        chkStatus.setSelected(true);

        txtName.setEnabled(true);
        txtExpirationDate.setEnabled(true);
        chkStatus.setEnabled(true);
        cmdExpirationDate.setEnabled(true);

        txtName.requestFocus();
    }

    @Override
    public void writeValueEdit(Object value) {
        Object[] lot = (Object[]) value;

        id = lot[0];
        txtName.setText(Formats.STRING.formatValue(lot[1]));
        txtExpirationDate.setText(Formats.DATE.formatValue(lot[2]));

        var status = Boolean.valueOf(Formats.BOOLEAN.formatValue(lot[3]));
        if (status) {
            chkStatus.setSelected(true);
        } else {
            chkStatus.setSelected(false);
        }

        txtName.setEnabled(true);
        txtExpirationDate.setEnabled(true);
        chkStatus.setEnabled(true);
        cmdExpirationDate.setEnabled(true);
    }

    @Override
    public void writeValueDelete(Object value) {
        Object[] lot = (Object[]) value;

        id = lot[0];
        txtName.setText(Formats.STRING.formatValue(lot[1]));
        txtExpirationDate.setText(Formats.DATE.formatValue(lot[2]));

        var status = Boolean.valueOf(Formats.BOOLEAN.formatValue(lot[3]));
        if (status) {
            chkStatus.setSelected(true);
        } else {
            chkStatus.setSelected(false);
        }

        txtName.setEnabled(false);
        txtExpirationDate.setEnabled(false);
        chkStatus.setEnabled(false);
        cmdExpirationDate.setEnabled(false);
    }

    @Override
    public void refresh() {
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public Object createValue() throws BasicException {
        ErrorMessage validate = validateData();

        if (validate.getIsError()) {
            throw new BasicException(validate.getMessage());
        }

        Object[] lot = new Object[4];

        lot[0] = id == null ? UuidCreator.getTimeOrderedEpoch().toString() : id;
        lot[1] = txtName.getText();
        lot[2] = Formats.TIMESTAMP.parseValue(txtExpirationDate.getText());
        if (chkStatus.isSelected()) {
            lot[3] = true;
        } else {
            lot[3] = false;
        }

        return lot;
    }

    private ErrorMessage validateData() {
        if (txtName.getText().trim().isEmpty()) {
            return new ErrorMessage(AppLocal.getIntString("message.lot.empty"));
        }
        return new ErrorMessage();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkStatus;
    private javax.swing.JButton cmdAssignToProduct;
    private javax.swing.JButton cmdExpirationDate;
    private javax.swing.JLabel lblExpirationDate;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField txtExpirationDate;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}
