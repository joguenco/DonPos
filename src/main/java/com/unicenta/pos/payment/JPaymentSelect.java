//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2018 uniCenta
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

package com.unicenta.pos.payment;

import com.unicenta.basic.BasicException;
import com.unicenta.data.gui.ComboBoxValModel;
import com.unicenta.data.loader.SentenceList;
import com.unicenta.format.Formats;
import com.unicenta.pos.customers.CustomerInfoBasic;
import com.unicenta.pos.customers.CustomerInfoExt;
import com.unicenta.pos.customers.DataLogicCustomers;
import com.unicenta.pos.forms.AppConfig;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.forms.AppView;
import com.unicenta.pos.forms.DataLogicSales;
import com.unicenta.pos.forms.DataLogicSystem;
import dev.joguenco.effect.CursorAnimation;
import dev.joguenco.error.ErrorMessage;
import dev.joguenco.http.client.HttpClientSubscription;
import dev.joguenco.http.client.entity.EntityResponse;
import dev.joguenco.http.client.entity.EntityService;
import dev.joguenco.identification.Validator;
import dev.joguenco.pos.establishment.DataLogicEstablishment;
import dev.joguenco.pos.establishment.EstablishmentInfo;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.WordUtils;
import retrofit2.Response;

/**
 *
 * @author adrianromero
 */
@Slf4j
public abstract class JPaymentSelect extends javax.swing.JDialog
        implements JPaymentNotifier {

    private PaymentInfoList m_aPaymentInfo;
    private boolean printselected;

    private boolean accepted;

    private AppView app;
    private double m_dTotal;
    private CustomerInfoExt customerext;
    private DataLogicSystem dlSystem;
    private DataLogicCustomers dlCustomers    ;
    DataLogicSales dlSales;

    // JG 16 May 12 use diamond inference
    private Map<String, JPaymentInterface> payments = new HashMap<>();
    private String m_sTransactionID;
    private static PaymentInfo returnPayment = null;
    private String cityWhenAddressIsEmpty = "";
    // For identification type combobox 
    private SentenceList sentenceIdentificationType;
    private ComboBoxValModel modelIdentificationType;
    private String ticketType = "";
    private String customerDefault;
    private String country;

    public CustomerInfoExt getCustomerext() {
        return customerext;
    }

    public String getTicketType() {
        return ticketType;
    }

    public static PaymentInfo getReturnPayment() {
        return returnPayment;
    }

    public static void setReturnPayment(PaymentInfo returnPayment) {
        JPaymentSelect.returnPayment = returnPayment;
    }


    /**
     * Creates new form JPaymentSelect
     *
     * @param parent
     * @param modal
     * @param o
     */
    protected JPaymentSelect(java.awt.Frame parent, boolean modal, ComponentOrientation o) {
        super(parent, modal);
        initComponents();
        this.applyComponentOrientation(o);
        getRootPane().setDefaultButton(m_jButtonOK);

    }

    /** Creates new form JPaymentSelect
     * @param parent
     * @param modal
     * @param o */
    protected JPaymentSelect(java.awt.Dialog parent, boolean modal, ComponentOrientation o) {
        super(parent, modal);
        initComponents();

        m_jButtonPrint.setVisible(true);
        this.applyComponentOrientation(o);
        if (printselected) {
            jlblPrinterStatus.setText("Printer ON");
        } else {
            jlblPrinterStatus.setText("Printer OFF");
        }
    }

    public void init(AppView app) {
        final var config = new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
        config.load();
        country = config.getProperty("user.country");
        
        this.app = app;
        dlSystem = (DataLogicSystem) app.getBean("com.unicenta.pos.forms.DataLogicSystem");
        dlCustomers= (DataLogicCustomers) app.getBean("com.unicenta.pos.customers.DataLogicCustomers");
        dlSales = (DataLogicSales) app.getBean("com.unicenta.pos.forms.DataLogicSales");

        txtIdentification.setFocusTraversalKeysEnabled(false);
        
        printselected = false;
        if (printselected) {
            jlblPrinterStatus.setText("Printer ON");
        } else {
            jlblPrinterStatus.setText("Printer OFF");
        }

        tabData.setTitleAt(0, AppLocal.getIntString("label.customer"));
        this.customerDefault = dlSystem.getResourceAsText("Customer.Default");
    }

    public void setPrintSelected(boolean value) {
        printselected = value;
    }

    public boolean isPrintSelected() {
        return printselected;
    }

    public List<PaymentInfo> getSelectedPayments() {
        return m_aPaymentInfo.getPayments();
    }

    public boolean showDialog(double total, CustomerInfoExt customerext,double deposit) {
        m_aPaymentInfo = new PaymentInfoList();
        accepted = false;
        total -= deposit;
        m_dTotal = total;

        this.customerext = customerext;
        m_jButtonPrint.setVisible(true);
        m_jButtonPrint.setSelected(printselected);
        m_jTotalEuros.setText(Formats.CURRENCY.formatValue(m_dTotal));

        addTabs();

        // gets the print button state
        printselected = m_jButtonPrint.isSelected();

        // remove all tabs
        m_jTabPayment.removeAll();

        return accepted;
    }

    public boolean showDialog(double total, CustomerInfoExt customerext, String serie) {

        m_aPaymentInfo = new PaymentInfoList();
        dlSales = (DataLogicSales) app.getBean("com.unicenta.pos.forms.DataLogicSales");

        sentenceIdentificationType = dlSales.getIdentificationTypeList(country);
        try {
            modelIdentificationType = new ComboBoxValModel(
                    sentenceIdentificationType.list());
            cbxIdentificationType.setModel(modelIdentificationType);

        } catch (BasicException ex) {
            log.error(ex.getMessage());
        }

        accepted = false;

        m_dTotal = total;

        this.customerext = customerext;

        setPrintSelected(!Boolean.parseBoolean(app.getProperties().getProperty("till.receiptprintoff")));
        m_jButtonPrint.setSelected(printselected);
        m_jTotalEuros.setText(Formats.CURRENCY.formatValue(m_dTotal));

        // Get customer from JPanelTicket finder
        if (this.customerext != null && existCustomer(this.customerext.getTaxid())) {
            CustomerInfoBasic customer = getCustomerById(this.customerext.getId());

            requestIdentification();
            txtIdentification.setText(customer.getTaxid());
            modelIdentificationType.setSelectedKey(customer.getType());
            txtName.setText(customer.getName());
            txtEmail.setText(customer.getEmail());
            txtAddress.setText(customer.getAddress());
            txtPhone.setText(customer.getPhone());

            m_jButtonOK.requestFocus();
            
            if (customer.getType().equals("CF")) {
                requestFinalConsumer();
            }
            // If customer is read only, disable form
            if (customerext.getIsReadOnly()) {

                txtIdentification.setEditable(false);
                txtName.setEditable(false);
                txtEmail.setEditable(false);
                txtAddress.setEditable(false);
                txtPhone.setEditable(false);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                            AppLocal.getIntString("message.default.customer"),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (txtAddress.getText().isEmpty()) {
            if (serie.length() >= 3) {
                setAddressWhenIsEmpty(serie.substring(0, 3));
            } else {
                setAddressWhenIsEmpty(serie);
            }
        }
        
        cmdUnderground.setSelected(false);

        if (printselected) {
            jlblPrinterStatus.setText("Printer ON");
        } else {
            jlblPrinterStatus.setText("Printer OFF");
        }

// N. Deppe 08/11/2018
// Fix issue where dialog keeps moving lower and lower on the screen
// Get the size of the screen, and center the dialog in the window
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension thisDim = this.getSize();
        int x = (screenDim.width - thisDim.width) / 2;
        int y = (screenDim.height - thisDim.height) / 2;
        this.setLocation(x, y);

        addTabs();

        if (m_jTabPayment.getTabCount() == 0) {
            // No payment panels available
            m_aPaymentInfo.add(getDefaultPayment(total));
            accepted = true;
        } else {
            getRootPane().setDefaultButton(m_jButtonOK);
            printState();
            setVisible(true);
        }

        // gets the print button state
        printselected = m_jButtonPrint.isSelected();

        // remove all tabs
        m_jTabPayment.removeAll();

        return accepted;
    }

    protected abstract void addTabs();
    protected abstract void setStatusPanel(boolean isPositive, boolean isComplete);
    protected abstract PaymentInfo getDefaultPayment(double total);

    protected void setOKEnabled(boolean value) {
        m_jButtonOK.setEnabled(value);
    }

    protected void setAddEnabled(boolean value) {
        m_jButtonAdd.setEnabled(value);
    }

    protected void addTabPayment(JPaymentCreator jpay) {
        if (app.getAppUserView().getUser().hasPermission(jpay.getKey())) {

            JPaymentInterface jpayinterface = payments.get(jpay.getKey());
            if (jpayinterface == null) {
                jpayinterface = jpay.createJPayment();
                payments.put(jpay.getKey(), jpayinterface);
            }

            jpayinterface.getComponent().applyComponentOrientation(getComponentOrientation());
            m_jTabPayment.addTab(
                    AppLocal.getIntString(jpay.getLabelKey()),
                    new javax.swing.ImageIcon(getClass().getResource(jpay.getIconKey())),
                    jpayinterface.getComponent());
        }
    }


    public interface JPaymentCreator {
        public JPaymentInterface createJPayment();
        public String getKey();
        public String getLabelKey();
        public String getIconKey();
    }

    public class JPaymentCashCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentCashPos(JPaymentSelect.this, dlSystem);
        }
        @Override
        public String getKey() {
            return "payment.cash"; }
        @Override
        public String getLabelKey() {
            return "tab.cash"; }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/cash.png"; }
    }

    public class JPaymentChequeCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentCheque(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.cheque"; }
        @Override
        public String getLabelKey() {
            return "tab.cheque"; }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/cheque.png"; }
    }

    public class JPaymentVoucherCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentVoucher(app, JPaymentSelect.this, "voucherin");
        }
        @Override
        public String getKey() {
            return "payment.voucher"; }
        @Override
        public String getLabelKey() {
            return "tab.voucher"; }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/voucher.png"; }
    }

    public class JPaymentMagcardCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentMagcard(app, JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.magcard"; }
        @Override
        public String getLabelKey() {
            return "tab.magcard"; }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/ccard.png"; }
    }



    public class JPaymentFreeCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentFree(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.free";
        }
        @Override
        public String getLabelKey() {
            return "tab.free";
        }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/wallet.png";
        }
    }

    public class JPaymentDebtCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentDebt(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.debt";
        }
        @Override
        public String getLabelKey() {
            return "tab.debt";
        }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/customer.png";
        }
    }

    public class JPaymentCashRefundCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "cashrefund");
        }
        @Override
        public String getKey() {
            return "refund.cash";
        }
        @Override
        public String getLabelKey() {
            return "tab.cashrefund";
        }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/cash.png";
        }
    }

    public class JPaymentChequeRefundCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "chequerefund");
        }
        @Override
        public String getKey() {
            return "refund.cheque";
        }
        @Override
        public String getLabelKey() {
            return "tab.chequerefund";
        }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/cheque.png";
        }
    }

    public class JPaymentVoucherRefundCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "voucherout");
        }
        @Override
        public String getKey() {
            return "refund.voucher";
        }
        @Override
        public String getLabelKey() {
            return "tab.voucher";
        }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/voucher.png";
        }
    }

    public class JPaymentMagcardRefundCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentMagcard(app, JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "refund.magcard";
        }
        @Override
        public String getLabelKey() {
            return "tab.magcard";
        }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/ccard.png";
        }
    }

    public class JPaymentBankCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentBank(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.bank";
        }
        @Override
        public String getLabelKey() {
            return "tab.bank";
        }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/bank.png";
        }
    }

    public class JPaymentSlipCreator implements JPaymentCreator {
        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentSlip(JPaymentSelect.this);
        }
        @Override
        public String getKey() {
            return "payment.slip";
        }
        @Override
        public String getLabelKey() {
            return "tab.slip";
        }
        @Override
        public String getIconKey() {
            return "/com/unicenta/images/slip.png";
        }
    }
    
    public class JPaymentDeunaCreator implements JPaymentCreator {

        @Override
        public JPaymentInterface createJPayment() {
            return new JPaymentDeuna(JPaymentSelect.this);
        }

        @Override
        public String getKey() {
            return "payment.deuna";
        }

        @Override
        public String getLabelKey() {
            return "tab.deuna";
        }

        @Override
        public String getIconKey() {
            return "/com/unicenta/images/cheque.png";
        }
    }

    private void printState() {

        m_jRemaininglEuros.setText(Formats.CURRENCY.formatValue(
                m_dTotal - m_aPaymentInfo.getTotal()));
        m_jButtonRemove.setEnabled(!m_aPaymentInfo.isEmpty());
        m_jTabPayment.setSelectedIndex(0);
        ((JPaymentInterface) m_jTabPayment.getSelectedComponent())
                .activate(customerext,
                        m_dTotal - m_aPaymentInfo.getTotal()
                        , m_sTransactionID);
    }

    protected static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window)parent;
        } else {
            return getWindow(parent.getParent());
        }
    }

    @Override
    public void setStatus(boolean isPositive, boolean isComplete) {

        setStatusPanel(isPositive, isComplete);
    }

    public void setTransactionID(String tID){
        this.m_sTransactionID = tID;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        m_jLblTotalEuros1 = new javax.swing.JLabel();
        m_jTotalEuros = new javax.swing.JLabel();
        m_jLblRemainingEuros = new javax.swing.JLabel();
        m_jRemaininglEuros = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        m_jButtonRemove = new javax.swing.JButton();
        m_jButtonAdd = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        m_jTabPayment = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        cmdUnderground = new javax.swing.JToggleButton();
        m_jButtonCancel = new javax.swing.JButton();
        m_jButtonOK = new javax.swing.JButton();
        m_jButtonPrint = new javax.swing.JToggleButton();
        jlblPrinterStatus = new javax.swing.JLabel();
        tabData = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        txtType = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        cbxIdentificationType = new javax.swing.JComboBox<>();
        lblIdentification = new javax.swing.JLabel();
        txtIdentification = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblAddress = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        lblPhone = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("payment.title")); // NOI18N
        setPreferredSize(new java.awt.Dimension(750, 127));
        setResizable(false);

        jPanel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        m_jLblTotalEuros1.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        m_jLblTotalEuros1.setText(AppLocal.getIntString("label.totalcash")); // NOI18N
        m_jLblTotalEuros1.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jTotalEuros.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jTotalEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTotalEuros.setOpaque(true);
        m_jTotalEuros.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jTotalEuros.setRequestFocusEnabled(false);

        m_jLblRemainingEuros.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        m_jLblRemainingEuros.setText(AppLocal.getIntString("label.remainingcash")); // NOI18N
        m_jLblRemainingEuros.setPreferredSize(new java.awt.Dimension(120, 30));

        m_jRemaininglEuros.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        m_jRemaininglEuros.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jRemaininglEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jRemaininglEuros.setOpaque(true);
        m_jRemaininglEuros.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jRemaininglEuros.setRequestFocusEnabled(false);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        m_jButtonRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/btnminus.png"))); // NOI18N
        m_jButtonRemove.setToolTipText("Delete Part Payment");
        m_jButtonRemove.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonRemoveActionPerformed(evt);
            }
        });

        m_jButtonAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/btnplus.png"))); // NOI18N
        m_jButtonAdd.setToolTipText("Add Part Payment");
        m_jButtonAdd.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonAddActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(m_jLblTotalEuros1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(m_jTotalEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(m_jLblRemainingEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(m_jRemaininglEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(18, 18, 18)
                .add(m_jButtonAdd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_jButtonRemove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(0, 0, Short.MAX_VALUE)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, m_jButtonRemove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, m_jButtonAdd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(5, 5, 5)
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(m_jLblTotalEuros1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(m_jRemaininglEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(m_jLblRemainingEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(m_jTotalEuros, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);

        jPanel3.setNextFocusableComponent(m_jTabPayment);
        jPanel3.setLayout(new java.awt.BorderLayout());

        m_jTabPayment.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jTabPayment.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        m_jTabPayment.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTabPayment.setRequestFocusEnabled(false);
        m_jTabPayment.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                m_jTabPaymentStateChanged(evt);
            }
        });
        m_jTabPayment.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                m_jTabPaymentKeyPressed(evt);
            }
        });
        jPanel3.add(m_jTabPayment, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.BorderLayout());

        cmdUnderground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/pig.png"))); // NOI18N
        cmdUnderground.setPreferredSize(new java.awt.Dimension(110, 45));
        cmdUnderground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdUndergroundActionPerformed(evt);
            }
        });
        jPanel2.add(cmdUnderground);

        m_jButtonCancel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jButtonCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/cancel.png"))); // NOI18N
        m_jButtonCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonCancel.setPreferredSize(new java.awt.Dimension(110, 45));
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonCancel);

        m_jButtonOK.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jButtonOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/ok.png"))); // NOI18N
        m_jButtonOK.setText(AppLocal.getIntString("button.OK")); // NOI18N
        m_jButtonOK.setFocusPainted(false);
        m_jButtonOK.setFocusable(false);
        m_jButtonOK.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonOK.setMaximumSize(new java.awt.Dimension(100, 44));
        m_jButtonOK.setPreferredSize(new java.awt.Dimension(110, 45));
        m_jButtonOK.setRequestFocusEnabled(false);
        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonOK);

        jPanel5.add(jPanel2, java.awt.BorderLayout.LINE_END);

        m_jButtonPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/printer24_off.png"))); // NOI18N
        m_jButtonPrint.setSelected(true);
        m_jButtonPrint.setToolTipText("Print Receipt");
        m_jButtonPrint.setFocusPainted(false);
        m_jButtonPrint.setFocusable(false);
        m_jButtonPrint.setMargin(new java.awt.Insets(8, 16, 8, 16));
        m_jButtonPrint.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jButtonPrint.setRequestFocusEnabled(false);
        m_jButtonPrint.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/printer24.png"))); // NOI18N
        m_jButtonPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonPrintActionPerformed(evt);
            }
        });
        jPanel5.add(m_jButtonPrint, java.awt.BorderLayout.LINE_START);

        jlblPrinterStatus.setFont(new java.awt.Font("Arial", 0, 24)); // NOI18N
        jlblPrinterStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jlblPrinterStatus.setText(bundle.getString("label.printerstatusOn")); // NOI18N
        jPanel5.add(jlblPrinterStatus, java.awt.BorderLayout.CENTER);

        txtType.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        txtType.setText(bundle.getString("label.type")); // NOI18N

        txtPhone.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N

        cbxIdentificationType.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        cbxIdentificationType.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cbxIdentificationTypeFocusGained(evt);
            }
        });
        cbxIdentificationType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxIdentificationTypeActionPerformed(evt);
            }
        });

        lblIdentification.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblIdentification.setText(bundle.getString("label.taxid")); // NOI18N

        txtIdentification.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        txtIdentification.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtIdentificationFocusGained(evt);
            }
        });
        txtIdentification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdentificationActionPerformed(evt);
            }
        });

        lblName.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblName.setText(bundle.getString("label.namem")); // NOI18N

        txtName.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        txtName.setToolTipText("Apellido Nombre");
        txtName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtNameFocusGained(evt);
            }
        });
        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        lblEmail.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblEmail.setText(bundle.getString("label.email")); // NOI18N

        txtEmail.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        txtEmail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEmailActionPerformed(evt);
            }
        });

        lblAddress.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblAddress.setText(bundle.getString("label.locationaddress")); // NOI18N

        txtAddress.setFont(new java.awt.Font("Arial", 0, 19)); // NOI18N
        txtAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAddressActionPerformed(evt);
            }
        });

        lblPhone.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblPhone.setText(bundle.getString("label.phone")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblAddress)
                    .add(lblIdentification))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(txtName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                    .add(txtAddress)
                    .add(txtIdentification))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblEmail)
                    .add(txtType)
                    .add(lblPhone))
                .add(4, 4, 4)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, cbxIdentificationType, 0, 366, Short.MAX_VALUE)
                    .add(txtEmail)
                    .add(txtPhone))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txtIdentification, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(txtType)
                    .add(lblIdentification)
                    .add(cbxIdentificationType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(42, 42, 42)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(txtPhone, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(lblPhone)
                            .add(txtAddress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(lblName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(lblEmail)
                            .add(txtEmail, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lblAddress)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabData.addTab("Customer", jPanel1);

        jPanel5.add(tabData, java.awt.BorderLayout.PAGE_START);
        tabData.getAccessibleContext().setAccessibleName("");
        tabData.getAccessibleContext().setAccessibleDescription("");

        getContentPane().add(jPanel5, java.awt.BorderLayout.SOUTH);

        setSize(new java.awt.Dimension(900, 790));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonRemoveActionPerformed

        m_aPaymentInfo.removeLast();
        printState();

    }//GEN-LAST:event_m_jButtonRemoveActionPerformed

    private void m_jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonAddActionPerformed

        PaymentInfo returnPayment = (
                (JPaymentInterface) m_jTabPayment.getSelectedComponent())
                .executePayment();
        if (returnPayment != null) {
            m_aPaymentInfo.add(returnPayment);
            printState();
        }

    }//GEN-LAST:event_m_jButtonAddActionPerformed

    private void m_jTabPaymentStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_jTabPaymentStateChanged

        if (m_jTabPayment.getSelectedComponent() != null) {
            if (customerDefault.equals(txtIdentification.getText())) {
                final var tab = (JPaymentInterface) m_jTabPayment.getSelectedComponent();

                if (JPaymentCashPos.class == tab.getComponent().getClass()) {
                    ((JPaymentInterface) m_jTabPayment.getSelectedComponent())
                            .activate(customerext,
                                    m_dTotal - m_aPaymentInfo.getTotal(),
                                    m_sTransactionID);
                    m_jRemaininglEuros.setText(
                            Formats.CURRENCY.formatValue(
                                    m_dTotal - m_aPaymentInfo.getTotal()));
                } else {
                    JOptionPane.showMessageDialog(this,
                            "El Consumidor Final solo puede facturar en efectivo",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    setOKEnabled(false);
                }
            } else {
                ((JPaymentInterface) m_jTabPayment.getSelectedComponent())
                        .activate(customerext
                                , m_dTotal - m_aPaymentInfo.getTotal()
                                , m_sTransactionID);
                m_jRemaininglEuros.setText(
                        Formats.CURRENCY.formatValue(
                                m_dTotal - m_aPaymentInfo.getTotal()));
            }
        }

    }//GEN-LAST:event_m_jTabPaymentStateChanged

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOKActionPerformed
        if (cmdUnderground.isSelected()) {
            ticketType = "EV";
            executePayment();
        } else {
            ticketType = "FV";
            executePayment();
        }
    }//GEN-LAST:event_m_jButtonOKActionPerformed

    private void executePayment() {
        if (isBlank(txtIdentification.getText(), "Es necesario ingresar un valor en la identificación")) {
            return;
        }

        if (isBlank(txtName.getText(), "Es necesario ingresar un valor en la razón social")) {
            return;
        }

        if (isBlank(txtAddress.getText(), "Es necesario ingresar un valor en la dirección")) {
            return;
        }

        if (modelIdentificationType.getSelectedText().equals("Consumidor Final")) {
            if (!isValidAmountToFinalConsumer()) {
                JOptionPane.showMessageDialog(this,
                        "El monto a facturar no es válido para Consumidor Final",
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (!existCustomerByTaxIdAndTaxIdType(txtIdentification.getText(),
                modelIdentificationType.getSelectedKey().toString())) {
            saveCustomer();
        }

        try {
            customerext = dlCustomers
                    .loadCustomerExtTaxByIdAndTaxIdType(
                            txtIdentification.getText(),
                            modelIdentificationType.getSelectedKey().toString());

            if (!txtName.getText().equals(customerext.getName())
                    || !txtEmail.getText().equals(customerext.getCemail())
                    || !txtAddress.getText().equals(customerext.getAddress())
                    || !txtPhone.getText().equals(customerext.getPhone1())) {
                updateCustomer();
            }

        } catch (BasicException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al recuperar el cliente de la base de datos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                m_jButtonOK.setEnabled(false);
                setReturnPayment(((JPaymentInterface) m_jTabPayment.getSelectedComponent()).executePayment());
                return null;
            }

            @Override
            public void done() {
                m_jButtonOK.setEnabled(true);
                m_jButtonCancel.setEnabled(true);
                if (returnPayment != null) {
                    m_aPaymentInfo.add(returnPayment);
                    accepted = true;
                    dispose();
                }
            }
        };

        worker.execute();
    }
    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed

        dispose();

    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    private void m_jButtonPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonPrintActionPerformed
        if (!m_jButtonPrint.isSelected()) {
            jlblPrinterStatus.setText("Printer OFF");
        } else {
            jlblPrinterStatus.setText("Printer ON");
        }
    }//GEN-LAST:event_m_jButtonPrintActionPerformed

    private void m_jTabPaymentKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_jTabPaymentKeyPressed

        if( evt.getKeyCode() == KeyEvent.VK_F1 ) {

        } else if ( evt.getKeyCode() == KeyEvent.VK_F2 ) {

        }
    }//GEN-LAST:event_m_jTabPaymentKeyPressed

    private void cbxIdentificationTypeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cbxIdentificationTypeFocusGained
        cbxIdentificationType.setBackground(new java.awt.Color(255, 255, 255));
        if (!txtIdentification.getText().isEmpty()) {
            final var validator = new Validator(country);

            switch (txtIdentification.getText().length()) {
                case 10: {
                    final var error = validator.identification("C",
                        txtIdentification.getText()
                    );
                    if (error.getIsError()) {
                        System.err.println("Validation -> " + error.getMessage());
                        cbxIdentificationType.setBackground(new java.awt.Color(255, 204, 204));
                        modelIdentificationType.setSelectedKey("IE");
                    } else {
                        modelIdentificationType.setSelectedKey("C");
                    }
                    break;
                }
                case 13: {
                    final var error = validator.identification("R",
                        txtIdentification.getText()
                    );
                    if (error.getIsError()) {
                        cbxIdentificationType.setBackground(new java.awt.Color(255, 204, 204));
                        modelIdentificationType.setSelectedKey("IE");
                    } else {
                        modelIdentificationType.setSelectedKey("R");
                    }
                    break;
                }
                default:
                modelIdentificationType.setSelectedKey("IE");
                break;
            }
        }
    }//GEN-LAST:event_cbxIdentificationTypeFocusGained

    private void cbxIdentificationTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxIdentificationTypeActionPerformed
        if (modelIdentificationType.getSelectedKey() != null) {
            String identificationType = modelIdentificationType.getSelectedKey().toString();
            if (identificationType.equals("CF")) {
                requestFinalConsumer();
                txtIdentification.requestFocus();
            } else {
                var error = validateIdentification();
                if (error.getIsError()) {
                    JOptionPane.showMessageDialog(this,
                        error.getMessage(),
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);

                    txtIdentification.requestFocus();
                    return;
                }
                requestIdentification();
            }
        }
    }//GEN-LAST:event_cbxIdentificationTypeActionPerformed

    private void txtIdentificationFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtIdentificationFocusGained
        txtIdentification.selectAll();
    }//GEN-LAST:event_txtIdentificationFocusGained

    private void txtIdentificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdentificationActionPerformed
        if (!validateEmpty(txtIdentification, "Identificación")) {
            return;
        }

        if (existCustomerByTaxId(txtIdentification.getText())) {
            CustomerInfoBasic customer = getCustomerByTaxId(
                txtIdentification.getText());

            modelIdentificationType.setSelectedKey(customer.getType());
            txtName.setText(customer.getName());
            txtEmail.setText(customer.getEmail());
            txtAddress.setText(customer.getAddress());
            txtPhone.setText(customer.getPhone());

            try {
                this.customerext = dlSales.loadCustomerExt(customer.getId());
                m_jButtonOK.setEnabled(true);
                if (m_jTabPayment.getTabCount() > 0) {
                    m_jTabPayment.setSelectedIndex(0);
                }
                m_jButtonOK.requestFocus();
            } catch (BasicException ex) {
                log.error(ex.getMessage());
            }
        } else {
            var status = getEntity(txtIdentification.getText());
            if(!status) {
                txtName.setText("");
            }
            modelIdentificationType.setSelectedKey(null);            
            txtEmail.setText("");
            txtAddress.setText(cityWhenAddressIsEmpty);
            txtPhone.setText("");

            this.customerext = null;
            cbxIdentificationType.requestFocus();
        }

//        requestIdentification();
        m_jButtonOK.setEnabled(true);
        if (m_jTabPayment.getTabCount() > 0) {
            m_jTabPayment.setSelectedIndex(0);
        }
        //cbxIdentificationType.requestFocus();
        if (modelIdentificationType.getSelectedKey() != null) {
        var type = modelIdentificationType.getSelectedKey().toString();
            if ("CF".equals(type)) {
                requestFinalConsumer();
                txtPhone.setEditable(true);
                txtPhone.requestFocus();
                txtPhone.setEditable(false);
            }
        }
    }//GEN-LAST:event_txtIdentificationActionPerformed

    private Boolean getEntity(String identification) {
        
        if (identification.equals(dlSystem.getResourceAsText("Customer.Default"))) {
            return false;
        }
        
        CursorAnimation.startWaitCursor(getRootPane());
        try {
            var httpClient = new HttpClientSubscription(this.app);
            final var serviceName = "ReIdi";
            
            if (!httpClient.isActive(serviceName)) {
                CursorAnimation.stopWaitCursor(getRootPane());
                return false;
            }
            
            final var userAgent = AppLocal.APP_NAME + "/" + AppLocal.APP_VERSION;
            
            var service = httpClient.generator(serviceName)
                    .createService(EntityService.class, httpClient.getToken(), userAgent);
            var callSync = service.getEntity(identification);
                                              
            Response<EntityResponse> response = callSync.execute();
            if (response.isSuccessful()) {
                EntityResponse entity = response.body();
                txtName.setText(entity.getName());
                CursorAnimation.stopWaitCursor(getRootPane());
                return true;
            }                        
        } catch (IllegalArgumentException | HeadlessException | IOException ex) {
            log.error(JPaymentSelect.class.getName() + " " + ex.getMessage());
        } catch (BasicException ex) {
            log.error(JPaymentSelect.class.getName() + " " + ex.getMessage());
        }
        CursorAnimation.stopWaitCursor(getRootPane());
        return false;
    }
    
    private void txtNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNameFocusGained
        txtName.selectAll();
    }//GEN-LAST:event_txtNameFocusGained

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        if (!validateEmpty(txtName, "Razón Social")) {
            return;
        }
        txtEmail.requestFocus();
    }//GEN-LAST:event_txtNameActionPerformed

    private void txtEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEmailActionPerformed
        txtAddress.requestFocus();
        txtAddress.selectAll();
    }//GEN-LAST:event_txtEmailActionPerformed

    private void txtAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAddressActionPerformed
        if (!validateEmpty(txtAddress, "Dirección")) {
            return;
        }
        txtPhone.requestFocus();
    }//GEN-LAST:event_txtAddressActionPerformed

    private void cmdUndergroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdUndergroundActionPerformed
        if (m_jButtonPrint.isSelected()) {
            m_jButtonPrint.setSelected(false);
            jlblPrinterStatus.setText("Printer OFF");
        } else {
            m_jButtonPrint.setSelected(true);
            jlblPrinterStatus.setText("Printer ON");
        }
    }//GEN-LAST:event_cmdUndergroundActionPerformed

    /**
     * Validate that string characters not empty
     */
    private Boolean validateEmpty(javax.swing.JTextField field, String name) {
        String stringCharacters = field.getText();
        stringCharacters = stringCharacters.replaceAll("\\s+", "");
        if (stringCharacters.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El el campo de texto " + name + " no tiene que estar vacío",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private ErrorMessage validateIdentification() {        
        final var validator = new Validator(country);

        return validator.identification(getIdentificationType(), txtIdentification.getText());
    }
    
    private String getIdentificationType() {
        return modelIdentificationType.getSelectedKey().toString();
    }
    
    private void requestIdentification() {
        if (txtIdentification.getText().isEmpty()) {
            txtIdentification.requestFocus();
        } else {
            txtName.requestFocus();
        }
        txtIdentification.setEditable(true);
        txtName.setEditable(true);
        txtEmail.setEditable(true);
        txtAddress.setEditable(true);
        txtPhone.setEditable(true);
    }
    
    private void requestFinalConsumer() {
        txtIdentification.setText(customerDefault);
        txtName.setEditable(false);
        txtEmail.setEditable(false);
        txtAddress.setEditable(false);
        txtPhone.setEditable(false);
    }
    
    private Boolean existCustomerByTaxId(String identification) {
        try {
            int count = 0;
            count = dlCustomers
                    .countCustomerByTaxId(
                            identification
                    );
            if (count == 0) {
                return false;
            }
            if (count > 1) {
                JOptionPane.showMessageDialog(this,
                        "Existe más de un cliente con esa identificación",
                        "Advertencia",
                        JOptionPane.WARNING_MESSAGE);
            }
            return true;
        } catch (BasicException ex) {
            log.error(ex.getMessage());
            return false;
        }
    }
    
    private CustomerInfoBasic getCustomerById(String customerId) {
        CustomerInfoBasic customer;
        try {
            customer = dlCustomers.getCustomerById(customerId);
            if (customer == null) {
                return null;
            }

            return customer;
        } catch (BasicException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }
    
    /**
     * Search customer by identification
     */
    private CustomerInfoBasic getCustomerByTaxId(String identification) {
        try {
            CustomerInfoBasic customer = dlCustomers.getCustomerByTaxId(
                    identification
            );
            if (customer == null) {
                return null;
            }
            return customer;
        } catch (BasicException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }
    
    private Boolean isBlank(String text, String message) {
        final var validate = new Validator(country);

        final var isBlank = validate.blank(text, message);

        if (isBlank.getIsError()) {
            JOptionPane.showMessageDialog(this,
                    isBlank.getMessage(),
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return isBlank.getIsError();
        }

        return false;
    }
    
    /**
     * Validate the value of sale of the Final Consumer
     */
    private Boolean isValidAmountToFinalConsumer() {
        try {
            Double amount = dlCustomers
                    .maxValueInSaleWhenIsFinalConsumer(txtIdentification.getText());

            return m_dTotal < amount;
        } catch (BasicException ex) {
            System.out.println("Error al recuperar en monto válido para consumidor final");
            return false;
        }
    }
    
    private Boolean existCustomerByTaxIdAndTaxIdType(String identification, String identificationType) {
        try {
            int count = 0;
            count = dlCustomers
                    .countCustomerByTaxIdAndTaxIdType(
                            identification,
                            identificationType
                    );
            if (count == 0) {
                return false;
            }
            return true;
        } catch (BasicException ex) {
            log.error(ex.getMessage());
            return false;
        }
    }
    
    /**
     * Search customer by taxid, return if exist return true else false
     */
    private Boolean existCustomer(String customerTaxId) {
        int count = 0;
        try {
            count = dlCustomers.countCustomerByTaxId(customerTaxId);
            if (count == 0) {
                return false;
            }

            return true;
        } catch (BasicException ex) {
            log.error(ex.getMessage());
            return false;
        }
    }
    
    /**
     * Save customer
     */
    private void saveCustomer() {
        CustomerInfoBasic customer = new CustomerInfoBasic(txtIdentification.getText());
        customer.setType(getIdentificationType());
        customer.setName(WordUtils.capitalizeFully(txtName.getText()));
        customer.setEmail(txtEmail.getText());
        customer.setAddress(WordUtils.capitalizeFully(txtAddress.getText()));
        customer.setPhone(txtPhone.getText());

        try {
            dlCustomers.saveCustomer(customer);
        } catch (BasicException ex) {
            log.error(ex.getMessage());
        }
    }

    /**
     * Update customer
     */
    private void updateCustomer() {
        CustomerInfoBasic customer = new CustomerInfoBasic(customerext.getId());
        customer.setName(WordUtils.capitalizeFully(txtName.getText()));
        customer.setEmail(txtEmail.getText());
        customer.setAddress(WordUtils.capitalizeFully(txtAddress.getText()));
        customer.setPhone(txtPhone.getText());

        customerext.setName(customer.getName());
        customerext.setCemail(customer.getEmail());
        customerext.setAddress(customer.getAddress());
        customerext.setPhone1(customer.getPhone());

        try {
            dlCustomers.updateCustomer(customer);
        } catch (BasicException ex) {
            log.error(ex.getMessage());
        }
    }
    
    void setAddressWhenIsEmpty(String establishmentId) {
        DataLogicEstablishment dlEstablishment = (DataLogicEstablishment) app.getBean("dev.joguenco.pos.establishment.DataLogicEstablishment");

        try {
            if (!establishmentId.isEmpty()) {
                EstablishmentInfo establishmentInfo = (EstablishmentInfo) dlEstablishment
                        .getEstablishmentInfo()
                        .find(establishmentId);

                if (establishmentInfo == null) {
                    txtAddress.setText("No definida");
                } else {
                    cityWhenAddressIsEmpty = establishmentInfo.getCity();
                    txtAddress.setText(cityWhenAddressIsEmpty);
                }
            }

        } catch (BasicException ex) {
            txtAddress.setText("No definida");
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cbxIdentificationType;
    private javax.swing.JToggleButton cmdUnderground;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel jlblPrinterStatus;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblIdentification;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPhone;
    private javax.swing.JButton m_jButtonAdd;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    private javax.swing.JToggleButton m_jButtonPrint;
    private javax.swing.JButton m_jButtonRemove;
    private javax.swing.JLabel m_jLblRemainingEuros;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jRemaininglEuros;
    private javax.swing.JTabbedPane m_jTabPayment;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JTabbedPane tabData;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtIdentification;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JLabel txtType;
    // End of variables declaration//GEN-END:variables
}
