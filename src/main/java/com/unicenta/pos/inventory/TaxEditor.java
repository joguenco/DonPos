//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2018 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.unicenta.pos.inventory;

import com.unicenta.basic.BasicException;
import com.unicenta.data.gui.ComboBoxValModel;
import com.unicenta.data.gui.MessageInf;
import com.unicenta.data.loader.SentenceList;
import com.unicenta.data.loader.Session;
import com.unicenta.data.user.DirtyManager;
import com.unicenta.data.user.EditorRecord;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.forms.AppView;
import com.unicenta.pos.forms.DataLogicSales;
import com.unicenta.pos.forms.DataLogicSystem;
import dev.joguenco.pos.taxes.DataLogicTaxes;
import dev.joguenco.pos.taxes.TaxInfo;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author JG uniCenta
 */
@Slf4j
public class TaxEditor extends JPanel implements EditorRecord {

    private Session s;

    private Object m_oId;
    
    private SentenceList taxcatsent;
    private ComboBoxValModel taxcatmodel;    
    
    private SentenceList taxcustcatsent;
    private ComboBoxValModel taxcustcatmodel;   
    
    private SentenceList taxparentsent;
    private ComboBoxValModel taxparentmodel;    
    
    DataLogicSystem dlSystem;
    private DataLogicTaxes dlTaxes;
    private TaxInfo currentTax;
    
    /** Creates new form taxEditor
     * @param app
     * @param dirty */
    public TaxEditor(AppView app, DirtyManager dirty) {

        s = app.getSession();
        DataLogicSales dlSales = (DataLogicSales) app.getBean("com.unicenta.pos.forms.DataLogicSales");
        dlSystem = (DataLogicSystem) app.getBean("com.unicenta.pos.forms.DataLogicSystem");
        dlTaxes = (DataLogicTaxes) app.getBean("dev.joguenco.pos.taxes.DataLogicTaxes");

        initComponents();
        
        taxcatsent = dlSales.getTaxCategoriesList();
        taxcatmodel = new ComboBoxValModel();        
        
        taxcustcatsent = dlSales.getTaxCustCategoriesList();
        taxcustcatmodel = new ComboBoxValModel();    
        
        taxparentsent = dlSales.getTaxList();
        taxparentmodel = new ComboBoxValModel();    

        txtId.getDocument().addDocumentListener(dirty);
        m_jName.getDocument().addDocumentListener(dirty);
        m_jTaxCategory.addActionListener(dirty);
        m_jCustTaxCategory.addActionListener(dirty);
        m_jTaxParent.addActionListener(dirty);
        m_jRate.getDocument().addDocumentListener(dirty);
        jCascade.addActionListener(dirty);
        jOrder.getDocument().addDocumentListener(dirty);
        txtLegalCode.getDocument().addDocumentListener(dirty);
        txtDateStart.getDocument().addDocumentListener(dirty);
        chkStatus.addActionListener(dirty);

        setCurrentTax();

        writeValueEOF();
    }
    
    /**
     *
     * @throws BasicException
     */
    public void activate() throws BasicException {
        
        List a = taxcatsent.list();
        taxcatmodel = new ComboBoxValModel(a);
        m_jTaxCategory.setModel(taxcatmodel);
        
        a = taxcustcatsent.list();
        a.add(0, null); // The null item
        taxcustcatmodel = new ComboBoxValModel(a);
        m_jCustTaxCategory.setModel(taxcustcatmodel);    
        
       
    }
    
    /**
     *
     */
    @Override
    public void refresh() {
        
        List a;
        
        try {
            a = taxparentsent.list();
        } catch (BasicException eD) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotloadlists"), eD);
            msg.show(this);
            a = new ArrayList();
        }
        
        a.add(0, null); // The null item
        taxparentmodel = new ComboBoxValModel(a);
        m_jTaxParent.setModel(taxparentmodel);    
    }
    
    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_oId = null;
        txtId.setText(null);
        m_jName.setText(null);
        taxcatmodel.setSelectedKey(null);
        taxcustcatmodel.setSelectedKey(null);
        taxparentmodel.setSelectedKey(null);
        m_jRate.setText(null);
        jCascade.setSelected(false);
        jOrder.setText(null);
        txtLegalCode.setText(null);
        txtDateStart.setText(null);
        chkStatus.setSelected(false);

        txtId.setEnabled(false);
        m_jName.setEnabled(false);
        m_jTaxCategory.setEnabled(false);
        m_jCustTaxCategory.setEnabled(false);
        m_jTaxParent.setEnabled(false);
        m_jRate.setEnabled(false);
        jCascade.setEnabled(false);
        jOrder.setEnabled(false);
        txtLegalCode.setEnabled(false);
        txtDateStart.setEnabled(false);
        chkStatus.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_oId = UUID.randomUUID().toString();
        txtId.setText(null);
        m_jName.setText(null);
        taxcatmodel.setSelectedKey(null);
        taxcustcatmodel.setSelectedKey(null);
        taxparentmodel.setSelectedKey(null);
        m_jRate.setText(null);
        jCascade.setSelected(false);
        jOrder.setText(null);
        txtLegalCode.setText(null);
        txtDateStart.setText(null);
        chkStatus.setSelected(false);

        txtId.setEnabled(true);
        m_jName.setEnabled(true);
        m_jTaxCategory.setEnabled(true);
        m_jCustTaxCategory.setEnabled(true);
        m_jTaxParent.setEnabled(true);        
        m_jRate.setEnabled(true);
        jCascade.setEnabled(true);    
        jOrder.setEnabled(true);
        txtLegalCode.setEnabled(true);
        txtDateStart.setEnabled(true);
        chkStatus.setEnabled(true);
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {

        Object[] tax = (Object[]) value;
        m_oId = tax[0];
        txtId.setText(Formats.STRING.formatValue(tax[0]));
        m_jName.setText(Formats.STRING.formatValue(tax[1]));
        taxcatmodel.setSelectedKey(tax[2]);
        taxcustcatmodel.setSelectedKey(tax[3]);
        taxparentmodel.setSelectedKey(tax[4]);        
        m_jRate.setText(Formats.PERCENT.formatValue(tax[5]));
        jCascade.setSelected((Boolean) tax[6]);
        jOrder.setText(Formats.INT.formatValue(tax[7]));
        txtLegalCode.setText(Formats.STRING.formatValue(tax[8]));
        txtDateStart.setText(Formats.DATE.formatValue(tax[9]));
        chkStatus.setSelected((Boolean) tax[10]);

        txtId.setEnabled(false);
        m_jName.setEnabled(false);
        m_jTaxCategory.setEnabled(false);
        m_jCustTaxCategory.setEnabled(false);
        m_jTaxParent.setEnabled(false);
        m_jRate.setEnabled(false);
        jCascade.setEnabled(false);
        jOrder.setEnabled(false);
        txtLegalCode.setEditable(false);
        txtDateStart.setEditable(false);
        chkStatus.setEnabled(false);
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {

        Object[] tax = (Object[]) value;
        m_oId = tax[0];
        txtId.setText(Formats.STRING.formatValue(tax[0]));
        m_jName.setText(Formats.STRING.formatValue(tax[1]));
        taxcatmodel.setSelectedKey(tax[2]);
        taxcustcatmodel.setSelectedKey(tax[3]);
        taxparentmodel.setSelectedKey(tax[4]);        
        m_jRate.setText(Formats.PERCENT.formatValue(tax[5]));
        jCascade.setSelected((Boolean) tax[6]);
        jOrder.setText(Formats.INT.formatValue(tax[7]));
        txtLegalCode.setText(Formats.STRING.formatValue(tax[8]));
        txtDateStart.setText(Formats.DATE.formatValue(tax[9]));
        chkStatus.setSelected((Boolean) tax[10]);

        txtId.setEnabled(false);
        m_jName.setEnabled(true);
        m_jTaxCategory.setEnabled(true);
        m_jCustTaxCategory.setEnabled(true);
        m_jTaxParent.setEnabled(true);        
        m_jRate.setEnabled(true);
        jCascade.setEnabled(true);
        jOrder.setEnabled(true);
        txtLegalCode.setEditable(true);
        txtDateStart.setEditable(true);
        chkStatus.setEnabled(true);
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        
        Object[] tax = new Object[11];

        if (txtId.getText().isEmpty()) {
            tax[0] = m_oId;
        } else {
            tax[0] = txtId.getText();
        }

        tax[1] = m_jName.getText();
        tax[2] = taxcatmodel.getSelectedKey();
        tax[3] = taxcustcatmodel.getSelectedKey(); 
        tax[4] = taxparentmodel.getSelectedKey(); 
        tax[5] = Formats.PERCENT.parseValue(m_jRate.getText());
        tax[6] = Boolean.valueOf(jCascade.isSelected());
        tax[7] = Formats.INT.parseValue(jOrder.getText());
        tax[8] = Formats.STRING.parseValue(txtLegalCode.getText());
        tax[9] = Formats.DATE.parseValue(txtDateStart.getText());
        tax[10] = Boolean.valueOf(chkStatus.isSelected());

        return tax;
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }
    
    private void setCurrentTax() {
        String taxNormalDay = dlSystem.getResourceAsText("Tax.Normal.Days");

        try {
            var taxInfo = (TaxInfo) dlTaxes.getTax().find(taxNormalDay);
            
            if (taxInfo != null) {
                currentTax = taxInfo;
                lblCurrentTax.setText(AppLocal.getIntString("label.currentTax")
                        + ": "
                        + Math.round(taxInfo.getRate() * 100)
                        + "%");
            } else {
                JOptionPane.showMessageDialog(null, 
                AppLocal.getIntString("message.tax.normal.days"));
            }
        } catch (BasicException ex) {
            log.error(ex.getMessage());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel8 = new javax.swing.JLabel();
        txtId = new javax.swing.JTextField();
        m_jName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        m_jRate = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jCascade = new javax.swing.JCheckBox();
        m_jTaxCategory = new javax.swing.JComboBox();
        m_jTaxParent = new javax.swing.JComboBox();
        m_jCustTaxCategory = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jOrder = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtLegalCode = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtDateStart = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        chkStatus = new javax.swing.JCheckBox();
        lblCurrentTax = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        checkUpdatePrice = new javax.swing.JCheckBox();
        cmdUpdateTax = new javax.swing.JButton();

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText(AppLocal.getIntString("label.dutyid")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(170, 30));

        txtId.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtId.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/info.png"))); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.name")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(170, 30));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.dutyrate")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(170, 30));

        m_jRate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jRate.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.taxcategory")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(170, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.custtaxcategory")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(170, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.taxparent")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(170, 30));

        jCascade.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCascade.setText(AppLocal.getIntString("label.cascade")); // NOI18N
        jCascade.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jTaxCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTaxCategory.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jTaxParent.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTaxParent.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jCustTaxCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCustTaxCategory.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.order")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(170, 30));

        jOrder.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jOrder.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.LegalCode")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(170, 30));

        txtLegalCode.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtLegalCode.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText(AppLocal.getIntString("label.date")); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(170, 30));

        txtDateStart.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtDateStart.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText(AppLocal.getIntString("label.Status")); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(170, 30));

        chkStatus.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        chkStatus.setPreferredSize(new java.awt.Dimension(0, 30));

        lblCurrentTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblCurrentTax.setForeground(new java.awt.Color(51, 51, 255));
        lblCurrentTax.setText(AppLocal.getIntString("label.currentTax")); // NOI18N
        lblCurrentTax.setPreferredSize(new java.awt.Dimension(170, 30));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Intercambiar impuestos"));

        checkUpdatePrice.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        checkUpdatePrice.setText(AppLocal.getIntString("label.maintainPrices")); // NOI18N
        checkUpdatePrice.setPreferredSize(new java.awt.Dimension(0, 30));

        cmdUpdateTax.setFont(new java.awt.Font("Noto Sans", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        cmdUpdateTax.setText(bundle.getString("button.priceupdate")); // NOI18N
        cmdUpdateTax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdUpdateTaxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkUpdatePrice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cmdUpdateTax)
                .addGap(108, 108, 108))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkUpdatePrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmdUpdateTax)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(m_jTaxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(m_jCustTaxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(m_jTaxParent, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(m_jRate, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(16, 16, 16)
                            .addComponent(jCascade, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtId, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(lblCurrentTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(chkStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtLegalCode, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDateStart, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(338, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtId, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jTaxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jCustTaxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jTaxParent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCascade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLegalCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDateStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCurrentTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        
        if (evt.getClickCount() == 2) {
            String uuidString = m_oId.toString();
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);
        
            JOptionPane.showMessageDialog(null, 
                AppLocal.getIntString("message.uuidcopy"));
        }
    }//GEN-LAST:event_jLabel2MouseClicked

    private void cmdUpdateTaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdUpdateTaxActionPerformed
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Elija un impuesto destino",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            var tax = (TaxInfo) dlTaxes.getTax().find(txtId.getText());

            if (tax.getRate() == 0) {
                JOptionPane.showMessageDialog(this,
                        "El impuesto elejido deber ser mayor a 0",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (BasicException ex) {
            log.error(ex.getMessage());
        }

        if (txtId.getText().equals(currentTax.getId())) {
            JOptionPane.showMessageDialog(this,
                    "El impuesto destino debe ser diferente al actual",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var message = "Está seguro de actualizar el impuesto "
                + currentTax.getTaxcategoryid() + " -> "
                + Math.round(currentTax.getRate() * 100) + "%"
                + " al impuesto " + taxcatmodel.getSelectedKey() + " -> " + m_jRate.getText();

        var reply = JOptionPane.showConfirmDialog(this,
                message,
                "Update Tax", JOptionPane.YES_NO_OPTION);

        if (reply == JOptionPane.YES_OPTION) {
            try {
                var targetTax = (TaxInfo) dlTaxes.getTax().find(taxcatmodel.getSelectedKey());

                if (checkUpdatePrice.isSelected()) {
                    var products = dlTaxes.getProducts(s, currentTax.getTaxcategoryid());

                    dlTaxes.updatePriceByTax(s,
                            products,
                            targetTax.getRate(),
                            currentTax.getRate());
                    System.out.println("Updated sell prices");
                }

                var r = dlTaxes.updateProductByTax(s,
                        targetTax.getId(),
                        currentTax.getTaxcategoryid());
                System.out.println("Update tax, " + r + " records updated");

                dlSystem.setResource("Tax.Normal.Days",
                        0,
                        targetTax.getId().getBytes());
                setCurrentTax();

            } catch (BasicException ex) {
                log.error(ex.getMessage());
            }
        }
    }//GEN-LAST:event_cmdUpdateTaxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkUpdatePrice;
    private javax.swing.JCheckBox chkStatus;
    private javax.swing.JButton cmdUpdateTax;
    private javax.swing.JCheckBox jCascade;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField jOrder;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCurrentTax;
    private javax.swing.JComboBox m_jCustTaxCategory;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jRate;
    private javax.swing.JComboBox m_jTaxCategory;
    private javax.swing.JComboBox m_jTaxParent;
    private javax.swing.JTextField txtDateStart;
    private javax.swing.JTextField txtId;
    private javax.swing.JTextField txtLegalCode;
    // End of variables declaration//GEN-END:variables
    
}
