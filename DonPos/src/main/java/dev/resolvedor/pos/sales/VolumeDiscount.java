package dev.resolvedor.pos.sales;

import com.unicenta.basic.BasicException;
import com.unicenta.data.gui.ComboBoxValModel;
import com.unicenta.data.loader.LocalRes;
import com.unicenta.data.loader.SentenceList;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.forms.AppView;
import com.unicenta.pos.forms.BeanFactoryApp;
import com.unicenta.pos.forms.BeanFactoryException;
import com.unicenta.pos.forms.DataLogicSales;
import com.unicenta.pos.forms.JPanelView;
import com.unicenta.pos.sales.TaxesLogic;
import com.unicenta.pos.ticket.TaxInfo;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Jorge Luis
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
@Slf4j
public class VolumeDiscount extends JPanel implements JPanelView, BeanFactoryApp {

    private AppView app;
    private DataLogicDiscount dlDiscount;
    private DataLogicSales dlSales;

    private ComboBoxValModel modelCategory;
    private VolumeDiscount.DiscountTableModel modelDiscount;

    private SentenceList sentTax;
    private TaxInfo tax;
    private TaxesLogic taxeslogic;

    public VolumeDiscount() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panFilter = new javax.swing.JPanel();
        lblCategories = new javax.swing.JLabel();
        cboCategories = new javax.swing.JComboBox<>();
        cmdReload = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableProducts = new javax.swing.JTable();
        cmdFindProduct = new javax.swing.JButton();
        cmdOkProducts = new javax.swing.JButton();
        cmdDeleteProduct = new javax.swing.JButton();
        cmdUpdateStatus = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        panFilter.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("label.find"))); // NOI18N

        lblCategories.setText(bundle.getString("label.prodcategory")); // NOI18N
        lblCategories.setPreferredSize(new java.awt.Dimension(90, 36));

        cboCategories.setPreferredSize(new java.awt.Dimension(90, 36));
        cboCategories.addActionListener(this::cboCategoriesActionPerformed);

        cmdReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/reload.png"))); // NOI18N
        cmdReload.setPreferredSize(new java.awt.Dimension(36, 36));
        cmdReload.addActionListener(this::cmdReloadActionPerformed);

        javax.swing.GroupLayout panFilterLayout = new javax.swing.GroupLayout(panFilter);
        panFilter.setLayout(panFilterLayout);
        panFilterLayout.setHorizontalGroup(
            panFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panFilterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCategories, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cboCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmdReload, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(348, Short.MAX_VALUE))
        );
        panFilterLayout.setVerticalGroup(
            panFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panFilterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCategories, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdReload, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        tableProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tableProducts);

        cmdFindProduct.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/editnew.png"))); // NOI18N
        cmdFindProduct.setPreferredSize(new java.awt.Dimension(120, 36));
        cmdFindProduct.addActionListener(this::cmdFindProductActionPerformed);

        cmdOkProducts.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/ok.png"))); // NOI18N
        cmdOkProducts.setPreferredSize(new java.awt.Dimension(120, 36));
        cmdOkProducts.addActionListener(this::cmdOkProductsActionPerformed);

        cmdDeleteProduct.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/editdelete.png"))); // NOI18N
        cmdDeleteProduct.setPreferredSize(new java.awt.Dimension(120, 36));
        cmdDeleteProduct.addActionListener(this::cmdDeleteProductActionPerformed);

        cmdUpdateStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/plugin.png"))); // NOI18N
        cmdUpdateStatus.setPreferredSize(new java.awt.Dimension(120, 36));
        cmdUpdateStatus.addActionListener(this::cmdUpdateStatusActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cmdDeleteProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdUpdateStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmdFindProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdOkProducts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmdOkProducts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdFindProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdDeleteProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdUpdateStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmdOkProductsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdOkProductsActionPerformed
        try {
            for (int i = 0; i < modelDiscount.getRowCount(); i++) {
                VolumeDiscountInfo d = modelDiscount.discount.get(i);
                dlDiscount.update(d);
            }
            JOptionPane.showMessageDialog(this, AppLocal.getIntString("button.discount"),
                    LocalRes.getIntString("sgn.success"), JOptionPane.INFORMATION_MESSAGE);
        } catch (BasicException ex) {
            log.error(VolumeDiscount.class.getName() + " cmdOkProductsActionPerformed " + ex);
        }

        reset();
    }//GEN-LAST:event_cmdOkProductsActionPerformed

    private void cboCategoriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboCategoriesActionPerformed
        loadVolumeDiscount(modelCategory.getSelectedKey().toString());
    }//GEN-LAST:event_cboCategoriesActionPerformed

    private void cmdFindProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFindProductActionPerformed
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ProductFinder dialog = new ProductFinder(dlSales, new javax.swing.JFrame());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        dialog.setVisible(false);
                    }
                });
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                if (dialog.getReturnStatus() == ProductFinder.RET_OK) {
                    var discount = dialog.getSelectedDiscount();
                    try {
                        dlDiscount.add(discount);
                        reset();
                    } catch (BasicException ex) {
                        log.error(VolumeDiscount.class.getName() + "cmdFindProductActionPerformed" + ex);
                    }
                }
            }
        });
    }//GEN-LAST:event_cmdFindProductActionPerformed

    private void cmdReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdReloadActionPerformed
        loadCategory();
        loadVolumeDiscount("");
    }//GEN-LAST:event_cmdReloadActionPerformed

    private void reset() {
        if (modelCategory.getSelectedKey() == null) {
            loadVolumeDiscount("");
        } else {
            loadVolumeDiscount(modelCategory.getSelectedKey().toString());
        }
    }

    private void cmdDeleteProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDeleteProductActionPerformed
        int selectedRow = tableProducts.getSelectedRow();
        if (selectedRow >= 0) {
            var discount = (VolumeDiscountInfo) modelDiscount.discount.get(selectedRow);
            var status = JOptionPane.showConfirmDialog(
                    this,
                    AppLocal.getIntString("message.deletelineyes"),
                    AppLocal.getIntString("button.discount"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (status == JOptionPane.YES_OPTION) {
                try {
                    dlDiscount.delete(discount.getId());
                    reset();
                } catch (BasicException ex) {
                    log.error(VolumeDiscount.class.getName() + " cmdDeleteProductActionPerformed " + ex);
                }
            }
        }
    }//GEN-LAST:event_cmdDeleteProductActionPerformed

    private void cmdUpdateStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdUpdateStatusActionPerformed
        if (tableProducts.getSelectedRow() >= 0) {
            try {
                var id = (Integer) tableProducts
                        .getValueAt(tableProducts.getSelectedRow(), 0);

                if (tableProducts.getValueAt(tableProducts.getSelectedRow(), 7).equals("Active")) {
                    int status = dlDiscount.updateStatusDiscount(id, false);
                    if (status == 1) {
                        modelDiscount.discount.get(tableProducts.getSelectedRow()).setStatus("Inactive");
                        tableProducts.repaint();
                    }
                } else {
                    int status = dlDiscount.updateStatusDiscount(id, true);
                    if (status == 1) {
                        modelDiscount.discount.get(tableProducts.getSelectedRow()).setStatus("Active");
                        tableProducts.repaint();
                    }
                }

            } catch (BasicException ex) {
                JOptionPane.showMessageDialog(this, LocalRes.getIntString("exception.noupdate"));
                log.error(VolumeDiscount.class.getName() + " cmdUpdateStatusActionPerformed " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_cmdUpdateStatusActionPerformed

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Discounts");
    }

    @Override
    public void activate() throws BasicException {
        dlSales = (DataLogicSales) app.getBean("com.unicenta.pos.forms.DataLogicSales");

        sentTax = dlSales.getTaxList();
        java.util.List<TaxInfo> taxlist;
        try {
            taxlist = sentTax.list();
            taxeslogic = new TaxesLogic(taxlist);
        } catch (BasicException ex) {
            log.error(ProductFinder.class.getName() + " " + ex.getMessage());
        }

        loadCategory();
        loadVolumeDiscount("");
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        this.app = app;
        dlDiscount = (DataLogicDiscount) app.getBean("dev.resolvedor.pos.sales.DataLogicDiscount");
    }

    @Override
    public Object getBean() {
        return this;
    }

    private void loadCategory() {
        try {
            modelCategory = new ComboBoxValModel(dlDiscount.getCategoryList().list());
            cboCategories.setModel(modelCategory);

        } catch (BasicException ex) {
            log.error(VolumeDiscount.class.getName() + " loadCategory " + ex);
        }
    }

    private void loadVolumeDiscount(String categoryId) {
        try {
            List<VolumeDiscountInfo> discount = dlDiscount.getVolumeDiscountList(categoryId).list();

            modelDiscount = new VolumeDiscount.DiscountTableModel(discount);

            for (int i = 0; i < modelDiscount.getRowCount(); i++) {
                var volumeDiscount = modelDiscount.discount.get(i);
                var priceSell = volumeDiscount.getPriceSell();
                double priceSellFinal = priceSell - (priceSell * (volumeDiscount.getValue() / 100));

                tax = taxeslogic.getTaxInfo(volumeDiscount.getProduct().getTaxCategoryID(), null);
                double taxValue = tax.getRate() + 1;

                modelDiscount.discount.get(i).setPriceSell(priceSell * taxValue);
                double rounded = BigDecimal.valueOf(priceSellFinal * taxValue)
                        .setScale(2, RoundingMode.HALF_UP).doubleValue();
                modelDiscount.discount.get(i).setPriceSellFinal(rounded);
            }

            tableProducts.setModel(modelDiscount);

        } catch (BasicException ex) {
            log.error(VolumeDiscount.class.getName() + " loadVolumeDiscount " + ex);
        }
        resetTable(tableProducts);
    }

    public void resetTable(JTable table) {

        DefaultTableCellRenderer rightRendererEdit = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT);

                if (isSelected) {
                    component.setBackground(new java.awt.Color(100, 150, 255)); // Custom selection color
                    component.setForeground(java.awt.Color.WHITE);
                } else {
                    component.setBackground(java.awt.Color.WHITE);
                    component.setForeground(java.awt.Color.BLACK);
                }

                return component;
            }
        };

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT);

                return component;
            }
        };

        Font headerFont = new Font("Arial", Font.BOLD, 14);
        Font bodyFont = new Font("Arial", Font.PLAIN, 18);

        JTableHeader header = table.getTableHeader();
        header.setFont(headerFont);
        table.setFont(bodyFont);
        table.setRowHeight(27);
        table.setPreferredScrollableViewportSize(
                new java.awt.Dimension(table.getPreferredScrollableViewportSize().width, 24 * 8)
        );

        table.getTableHeader().setReorderingAllowed(true);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRendererEdit);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRendererEdit);
        table.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

        table.repaint();
    }

    class DiscountTableModel extends AbstractTableModel {

        String id = AppLocal.getIntString("label.catid");
        String code = AppLocal.getIntString("label.prodbarcode");
        String name = AppLocal.getIntString("label.name");
        String priceSell = AppLocal.getIntString("label.pricetax");
        String priceSellFinal = AppLocal.getIntString("label.pricetax.final");
        String minimumQuantity = AppLocal.getIntString("label.units2");
        String value = AppLocal.getIntString("button.discount");
        String status = AppLocal.getIntString("label.Status");

        List<VolumeDiscountInfo> discount;
        String[] columnNames = {id, code, name, priceSell, priceSellFinal, minimumQuantity, value, status};

        public DiscountTableModel(List<VolumeDiscountInfo> list) {
            discount = list;
        }

        @Override
        public int getColumnCount() {
            return 8;
        }

        @Override
        public int getRowCount() {
            return discount.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            VolumeDiscountInfo volumeDiscount = discount.get(row);

            switch (column) {
                case 0:
                    return volumeDiscount.getId();
                case 1:
                    return volumeDiscount.getProduct().getCode();
                case 2:
                    return volumeDiscount.getProduct().getName();
                case 3:
                    return volumeDiscount.getPriceSell();
                case 4:
                    return volumeDiscount.getPriceSellFinal();
                case 5:
                    return volumeDiscount.getMinimumQuantity();
                case 6:
                    return volumeDiscount.getValue();
                case 7:
                    return volumeDiscount.getStatus();
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column == 6 && row >= 0 && row < discount.size()) {
                VolumeDiscountInfo productStock = discount.get(row);
                if (value instanceof Number) {
                    productStock.setValue(((Number) value).doubleValue());
                } else if (value instanceof String) {
                    try {
                        productStock.setValue(Double.parseDouble((String) value));
                    } catch (NumberFormatException ex) {
                        // ignore invalid input and keep existing value
                    }
                }
                fireTableCellUpdated(row, column);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 6;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cboCategories;
    private javax.swing.JButton cmdDeleteProduct;
    private javax.swing.JButton cmdFindProduct;
    private javax.swing.JButton cmdOkProducts;
    private javax.swing.JButton cmdReload;
    private javax.swing.JButton cmdUpdateStatus;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCategories;
    private javax.swing.JPanel panFilter;
    private javax.swing.JTable tableProducts;
    // End of variables declaration//GEN-END:variables
}
