package dev.resolvedor.pos.inventory.management;

import com.unicenta.basic.BasicException;
import com.unicenta.beans.DateUtils;
import com.unicenta.data.gui.ComboBoxValModel;
import com.unicenta.data.loader.LocalRes;
import com.unicenta.data.loader.SentenceList;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.AppConfig;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.forms.AppView;
import com.unicenta.pos.printer.TicketParser;
import com.unicenta.pos.scripting.ScriptEngine;
import com.unicenta.pos.scripting.ScriptFactory;
import com.unicenta.pos.forms.BeanFactoryApp;
import com.unicenta.pos.forms.BeanFactoryException;
import com.unicenta.pos.forms.DataLogicSales;
import com.unicenta.pos.forms.DataLogicSystem;
import com.unicenta.pos.forms.JPanelView;
import com.unicenta.pos.inventory.InventoryLine;
import com.unicenta.pos.inventory.InventoryRecord;
import com.unicenta.pos.inventory.LocationInfo;
import com.unicenta.pos.inventory.MovementReason;
import com.unicenta.pos.suppliers.DataLogicSuppliers;
import com.unicenta.pos.suppliers.SupplierInfo;
import com.unicenta.pos.ticket.ProductInfoExt;
import com.unicenta.pos.ticket.TaxInfo;
import com.unicenta.pos.ticket.UserInfo;
import dev.joguenco.pos.establishment.DataLogicEstablishment;
import dev.joguenco.pos.establishment.EstablishmentInfo;
import dev.joguenco.pos.taxpayer.DataLogicTaxpayer;
import dev.joguenco.pos.taxpayer.TaxpayerInfo;
import dev.joguenco.pos.ticketsnum.TicketsNumInfo;
import dev.joguenco.pos.ticketsnumpurchase.DataLogicTicketsNumPurchase;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.io.File;
import java.util.Date;
import java.util.stream.Collectors;
/**
 * pattern
 */
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

// Clases para filtrar lo que se escribe en un campo de texto
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 *
 * @author Jorge Luis
 * @web https://resolvedor.dev
 * @mail jorgeluis@resolvedor.dev
 */
@Slf4j
public class PurchaseEditor extends JPanel implements JPanelView, BeanFactoryApp {

    private AppView app;

    private DataLogicPurchase dlPurchase;
    private DataLogicSuppliers dlSupplier;
    private DataLogicSales dlSales;
    private DataLogicTaxpayer dlTaxPayer;
    private DataLogicEstablishment dlEstablishment;
    private DataLogicWithhold dlWithhold;
    protected DataLogicSystem dlSystem;

    private ComboBoxValModel modelReason;
    private ComboBoxValModel modelSupplier;
    private ComboBoxValModel modelTaxSupport;
    private ComboBoxValModel modelDocumentType;
    private ComboBoxValModel modelLocation;

    private SentenceList sentSupplier;
    private SentenceList sentTaxSupport;

    private PurchaseInfo purchase;

    /** Interpreta la plantilla ya resuelta y la manda a la impresora. */
    private TicketParser ticketParser;

    /** Retencion armada en el dialogo, todavia sin grabar. */
    private WithholdInfo pendingWithhold;

    /** La compra que se abrio del buscador ya tenia retencion. */
    private boolean loadedHasWithhold = false;

    /** Ya se aviso que la retencion quedo desfasada; no repetirlo. */
    private boolean retencionAvisada = false;
    private final JPurchaseLines purchaseLines;
    private String country;

    /**
     * plantilla para como debe verse el texto
     */
    private static final Pattern SERIE_PATTERN
            = Pattern.compile("^\\d{3}-\\d{3}-\\d{9}$");

    private static final Pattern AUTHORIZATION_PATTERN
            = Pattern.compile("^(\\d{10}|\\d{49})$");

    public PurchaseEditor() {
        initComponents();

        /**
         * COUTRY
         */
        final var config = new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
        config.load();
        country = config.getProperty("user.country");

        purchase = new PurchaseInfo();
        purchaseLines = new JPurchaseLines();
        panelLines.add(purchaseLines, BorderLayout.CENTER);

        AutoCompleteDecorator.decorate(cboSupplier);

        /**
         * En Ecuador el numero de factura lleva guiones (001-001-000000001) y
         * la autorizacion es solo digitos
         */
        if (isEcuador()) {
            ((AbstractDocument) txtSerie.getDocument())
                    .setDocumentFilter(new SerieFilter());

            ((AbstractDocument) txtAuthorization.getDocument())
                    .setDocumentFilter(new DigitsFilter(49));
        }

        enableForm(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmdSave = new javax.swing.JButton();
        cmdInsert = new javax.swing.JButton();
        cmdSearch = new javax.swing.JButton();
        cmdDelete = new javax.swing.JButton();
        cmdWithhold = new javax.swing.JButton();
        panelHead = new javax.swing.JPanel();
        lblNumber = new javax.swing.JLabel();
        txtNumber = new javax.swing.JTextField();
        lblReason = new javax.swing.JLabel();
        cboReason = new javax.swing.JComboBox<>();
        lblCreatedAt = new javax.swing.JLabel();
        txtCreatedAt = new javax.swing.JTextField();
        lblSupplier = new javax.swing.JLabel();
        cboSupplier = new javax.swing.JComboBox<>();
        lblDescription = new javax.swing.JLabel();
        txtObservation = new javax.swing.JTextField();
        panelTax = new javax.swing.JPanel();
        lblTaxSupport = new javax.swing.JLabel();
        cboTaxSupport = new javax.swing.JComboBox<>();
        lblDocument = new javax.swing.JLabel();
        cboDocumentType = new javax.swing.JComboBox<>();
        lblSerie = new javax.swing.JLabel();
        txtSerie = new javax.swing.JTextField();
        lblDatePurchase = new javax.swing.JLabel();
        txtDatePurchase = new javax.swing.JTextField();
        lblAuthorization = new javax.swing.JLabel();
        txtAuthorization = new javax.swing.JTextField();
        panelProduct = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cboLocation = new javax.swing.JComboBox<>();
        panelLines = new javax.swing.JPanel();
        panelAction = new javax.swing.JPanel();
        cmdDeleteProduct = new javax.swing.JButton();
        cmdFindProduct = new javax.swing.JButton();
        cmdDeleteAll = new javax.swing.JButton();
        panelTotal = new javax.swing.JPanel();
        lblSubtotalTitle = new javax.swing.JLabel();
        lblSubtotal = new javax.swing.JLabel();
        lblTaxTitle = new javax.swing.JLabel();
        lblTax = new javax.swing.JLabel();
        lblTotalTitle = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();

        cmdSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/ok.png"))); // NOI18N
        cmdSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSaveActionPerformed(evt);
            }
        });

        cmdInsert.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/editnew.png"))); // NOI18N
        cmdInsert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdInsertActionPerformed(evt);
            }
        });

        cmdSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/search24.png"))); // NOI18N
        cmdSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSearchActionPerformed(evt);
            }
        });

        cmdDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/editdelete.png"))); // NOI18N
        cmdDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDeleteActionPerformed(evt);
            }
        });

        cmdWithhold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/cheque.png"))); // NOI18N
        cmdWithhold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdWithholdActionPerformed(evt);
            }
        });

        panelHead.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblNumber.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        lblNumber.setText(bundle.getString("label.Number")); // NOI18N
        lblNumber.setMaximumSize(new java.awt.Dimension(120, 26));
        lblNumber.setMinimumSize(new java.awt.Dimension(120, 26));
        lblNumber.setPreferredSize(new java.awt.Dimension(99, 26));

        txtNumber.setMaximumSize(new java.awt.Dimension(240, 36));
        txtNumber.setPreferredSize(new java.awt.Dimension(240, 36));

        lblReason.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblReason.setText(bundle.getString("label.stockreason")); // NOI18N
        lblReason.setMaximumSize(new java.awt.Dimension(120, 26));
        lblReason.setMinimumSize(new java.awt.Dimension(120, 26));
        lblReason.setPreferredSize(new java.awt.Dimension(99, 26));

        lblCreatedAt.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCreatedAt.setLabelFor(txtCreatedAt);
        lblCreatedAt.setText(bundle.getString("label.date")); // NOI18N
        lblCreatedAt.setMaximumSize(new java.awt.Dimension(120, 26));
        lblCreatedAt.setMinimumSize(new java.awt.Dimension(120, 26));
        lblCreatedAt.setPreferredSize(new java.awt.Dimension(99, 26));

        txtCreatedAt.setPreferredSize(new java.awt.Dimension(240, 36));

        lblSupplier.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSupplier.setText(bundle.getString("label.supplier")); // NOI18N
        lblSupplier.setMaximumSize(new java.awt.Dimension(120, 26));
        lblSupplier.setMinimumSize(new java.awt.Dimension(120, 26));
        lblSupplier.setPreferredSize(new java.awt.Dimension(99, 26));

        lblDescription.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDescription.setText(bundle.getString("label.item2")); // NOI18N
        lblDescription.setMaximumSize(new java.awt.Dimension(120, 26));
        lblDescription.setMinimumSize(new java.awt.Dimension(120, 26));
        lblDescription.setPreferredSize(new java.awt.Dimension(99, 26));

        txtObservation.setPreferredSize(new java.awt.Dimension(600, 36));

        javax.swing.GroupLayout panelHeadLayout = new javax.swing.GroupLayout(panelHead);
        panelHead.setLayout(panelHeadLayout);
        panelHeadLayout.setHorizontalGroup(
            panelHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeadLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelHeadLayout.createSequentialGroup()
                        .addComponent(lblDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtObservation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelHeadLayout.createSequentialGroup()
                        .addGroup(panelHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cboReason, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelHeadLayout.createSequentialGroup()
                                .addComponent(lblCreatedAt, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCreatedAt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelHeadLayout.createSequentialGroup()
                                .addComponent(lblSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        panelHeadLayout.setVerticalGroup(
            panelHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeadLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCreatedAt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCreatedAt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboReason, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblReason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelHeadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtObservation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelTax.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("label.purchase"))); // NOI18N

        lblTaxSupport.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTaxSupport.setText(bundle.getString("label.taxSupport")); // NOI18N
        lblTaxSupport.setPreferredSize(new java.awt.Dimension(99, 26));

        cboTaxSupport.setPreferredSize(new java.awt.Dimension(180, 36));

        lblDocument.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDocument.setText(bundle.getString("label.supplierdocment")); // NOI18N
        lblDocument.setPreferredSize(new java.awt.Dimension(99, 26));

        cboDocumentType.setPreferredSize(new java.awt.Dimension(180, 36));

        lblSerie.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSerie.setText(bundle.getString("label.Number")); // NOI18N
        lblSerie.setPreferredSize(new java.awt.Dimension(99, 26));

        txtSerie.setPreferredSize(new java.awt.Dimension(240, 36));

        lblDatePurchase.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDatePurchase.setText(bundle.getString("label.date")); // NOI18N
        lblDatePurchase.setPreferredSize(new java.awt.Dimension(99, 26));

        txtDatePurchase.setPreferredSize(new java.awt.Dimension(180, 36));
        txtDatePurchase.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtDatePurchaseFocusGained(evt);
            }
        });

        lblAuthorization.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblAuthorization.setText(bundle.getString("label.authorization")); // NOI18N
        lblAuthorization.setPreferredSize(new java.awt.Dimension(99, 26));

        txtAuthorization.setPreferredSize(new java.awt.Dimension(530, 36));

        javax.swing.GroupLayout panelTaxLayout = new javax.swing.GroupLayout(panelTax);
        panelTax.setLayout(panelTaxLayout);
        panelTaxLayout.setHorizontalGroup(
            panelTaxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTaxLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTaxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelTaxLayout.createSequentialGroup()
                        .addComponent(lblTaxSupport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboTaxSupport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDocument, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cboDocumentType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSerie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSerie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelTaxLayout.createSequentialGroup()
                        .addComponent(lblDatePurchase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDatePurchase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblAuthorization, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAuthorization, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelTaxLayout.setVerticalGroup(
            panelTaxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTaxLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTaxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTaxSupport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTaxSupport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDocument, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboDocumentType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSerie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSerie, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelTaxLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDatePurchase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDatePurchase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAuthorization, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAuthorization, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelProduct.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(bundle.getString("label.location")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(99, 26));

        javax.swing.GroupLayout panelProductLayout = new javax.swing.GroupLayout(panelProduct);
        panelProduct.setLayout(panelProductLayout);
        panelProductLayout.setHorizontalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cboLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelProductLayout.setVerticalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelLines.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panelLines.setLayout(new java.awt.BorderLayout());

        panelAction.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        cmdDeleteProduct.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/editdelete.png"))); // NOI18N
        cmdDeleteProduct.setPreferredSize(new java.awt.Dimension(45, 45));
        cmdDeleteProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDeleteProductActionPerformed(evt);
            }
        });

        cmdFindProduct.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/search32.png"))); // NOI18N
        cmdFindProduct.setPreferredSize(new java.awt.Dimension(45, 45));
        cmdFindProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdFindProductActionPerformed(evt);
            }
        });

        cmdDeleteAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/sale_delete.png"))); // NOI18N
        cmdDeleteAll.setPreferredSize(new java.awt.Dimension(45, 45));
        cmdDeleteAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdDeleteAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelActionLayout = new javax.swing.GroupLayout(panelAction);
        panelAction.setLayout(panelActionLayout);
        panelActionLayout.setHorizontalGroup(
            panelActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmdDeleteProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdFindProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmdDeleteAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelActionLayout.setVerticalGroup(
            panelActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cmdDeleteProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmdFindProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmdDeleteAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelTotal.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblSubtotalTitle.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        lblSubtotalTitle.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSubtotalTitle.setText(bundle.getString("label.subtotalcash")); // NOI18N

        lblSubtotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblSubtotal.setLabelFor(lblSubtotal);
        lblSubtotal.setText("0.0");

        lblTaxTitle.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        lblTaxTitle.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTaxTitle.setText(bundle.getString("label.taxcash")); // NOI18N

        lblTax.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTax.setLabelFor(lblSubtotal);
        lblTax.setText("0.0");

        lblTotalTitle.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        lblTotalTitle.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotalTitle.setText(bundle.getString("label.totalcash")); // NOI18N

        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotal.setLabelFor(lblSubtotal);
        lblTotal.setText("0.0");

        javax.swing.GroupLayout panelTotalLayout = new javax.swing.GroupLayout(panelTotal);
        panelTotal.setLayout(panelTotalLayout);
        panelTotalLayout.setHorizontalGroup(
            panelTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTotalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSubtotalTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(lblSubtotal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(lblTaxTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(lblTax, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(lblTotalTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                    .addComponent(lblTotal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelTotalLayout.setVerticalGroup(
            panelTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTotalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSubtotalTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtotal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTaxTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTax)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTotal)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelProduct, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelTax, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelHead, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(cmdSave, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdInsert, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdWithhold, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmdDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, 0)
                        .addComponent(panelAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelLines, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelHead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelLines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelAction, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmdSave, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmdInsert, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmdSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmdDelete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmdWithhold, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(22, 22, 22))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmdSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSaveActionPerformed
        save();
    }//GEN-LAST:event_cmdSaveActionPerformed

    private void save() {
        if (!validateData()) {
            return;
        }

        MovementReason reason = (MovementReason) modelReason.getSelectedItem();

        try {
            Date d = (Date) Formats.TIMESTAMP.parseValue(txtCreatedAt.getText());
            purchase.setMoney(app.getActiveCashIndex());
            purchase.setCreatedAt(new Date());
            purchase.setReason((Integer) modelReason.getSelectedKey());
            // El objeto completo, no uno armado con el id: el combo ya trae
            // nombre, RUC, telefono y correo, y son los que salen impresos en
            // la liquidacion y en la retencion. Con solo el id se imprimian
            // vacios, porque el resto de campos quedaban en null.
            purchase.setSupplier((SupplierInfo) modelSupplier.getSelectedItem());
            purchase.setLocation((String) modelLocation.getSelectedKey());
            // Purchase tax support
            purchase.setPurchaseTaxSupport(modelTaxSupport.getSelectedKey().toString());
            purchase.setPurchaseDocument(modelDocumentType.getSelectedKey().toString());
            // El codigo "03" no le dice nada al proveedor: se guarda tambien el
            // texto del combo para poder imprimirlo en palabras.
            purchase.setPurchaseDocumentName(modelDocumentType.getSelectedItem() == null
                    ? null : modelDocumentType.getSelectedItem().toString());
            purchase.setPurchaseReference(txtSerie.getText());
            purchase.setPurchaseDate(parsePurchaseDate());
            purchase.setPurchaseAuthorization(txtAuthorization.getText());

            purchase.setObservation(txtObservation.getText());

            purchase.setTaxPayerInfo((TaxpayerInfo) dlTaxPayer.getTaxPayerInfo().find("1"));
            purchase.setEnvironment(dlSystem.getResourceAsText("Electronic.Environment"));

            if ("03".equals(modelDocumentType.getSelectedKey())) {
                purchase.setCode("LQ");
                var seriePurchase = getSeriePurchase(purchase.getUser(), purchase.getCode());
                purchase.setSerie(seriePurchase.getSerie());
                purchase.setFormatNumberDigits(dlSystem.getResourceAsText("FormatTicket.NumberDigits"));
            }

            // Los datos del local (nombre comercial, direccion, telefono) van
            // en la cabecera impresa, igual que en la factura. Salen del
            // establecimiento, que se identifica con los tres primeros digitos
            // de la serie.
            purchase.setEstablishment(findEstablishment(purchase.getSerie()));

            // La compra y su retencion van juntas: savePurchase las mete en
            // la misma transaccion. Si la compra ya tiene numero es porque se
            // abrio del buscador, y entonces solo se graba la retencion.
            var result = dlPurchase.savePurchase(purchase, new InventoryRecord(
                    new Date(),
                    reason,
                    (LocationInfo) modelLocation.getSelectedItem(),
                    app.getAppUserView().getUser().getName(),
                    (SupplierInfo) modelSupplier.getSelectedItem(),
                    purchaseLines.getLines(),
                    txtSerie.getText()),
                    pendingWithhold
            );

            // Se imprime lo que emitimos nosotros. La factura no: la trae el
            // proveedor. Si la compra es liquidacion CON retencion, salen las dos.
            if ("03".equals(purchase.getPurchaseDocument())) {
                printLiquidation();
            }

            if (pendingWithhold != null) {
                pendingWithhold.setEstablishment(
                        findEstablishment(pendingWithhold.getSerie()));
                printWithhold();
            }

            var mensaje = AppLocal.getIntString("label.purchase") + " = " + result;
            if (pendingWithhold != null) {
                mensaje += "\n" + AppLocal.getIntString("label.withhold") + " = "
                        + pendingWithhold.getSerieNumber();
            }

            JOptionPane.showMessageDialog(
                    this,
                    mensaje,
                    LocalRes.getIntString("sgn.success"),
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (BasicException ex) {
            log.error(PurchaseEditor.class.getName() + " " + ex.getMessage());
        }

        stateToInsert();
    }

    /**
     * Avisa una sola vez si quitar un producto dejo huerfana alguna linea de
     * la retencion.
     *
     * Solo avisa por ese caso, que es el que rompe: la linea apunta a un
     * sustento que la compra ya no tiene y su bloque docSustento no se arma,
     * asi que esa retencion se pierde del XML sin que nadie lo note.
     *
     * NO avisa por agregar un producto de un sustento nuevo: retener o no
     * sobre el es una decision valida del usuario, y avisarle ahi seria
     * gritar por algo que no esta mal.
     */
    private void avisarSiLaRetencionQuedoDesfasada() {
        if (pendingWithhold == null) {
            retencionAvisada = false;
            return;
        }

        var desfasada = !sustentosHuerfanos().isEmpty();

        if (desfasada && !retencionAvisada) {
            retencionAvisada = true;
            JOptionPane.showMessageDialog(
                    this,
                    AppLocal.getIntString("message.withhold.outofdate"),
                    AppLocal.getIntString("label.withhold"),
                    JOptionPane.WARNING_MESSAGE
            );
        } else if (!desfasada) {
            retencionAvisada = false;
        }
    }

    /**
     * Los sustentos que la retencion armada usa y la compra ya no tiene.
     *
     * Pasa cuando se quita un producto despues de armar la retencion: la linea
     * de retencion queda apuntando a un sustento huerfano, y al emitir el XML
     * su bloque docSustento no se arma y esa retencion se pierde sin aviso.
     */
    private java.util.List<String> sustentosHuerfanos() {
        if (pendingWithhold == null || pendingWithhold.getLines() == null) {
            return java.util.Collections.emptyList();
        }

        var enLaCompra = purchaseLines.getLines().stream()
                .map(InventoryLine::getTaxSupport)
                .filter(c -> c != null)
                .collect(Collectors.toSet());

        return pendingWithhold.getLines().stream()
                .map(WithholdLineInfo::getTaxSupport)
                .filter(c -> c != null && !enLaCompra.contains(c))
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean validateData() {

        var huerfanos = sustentosHuerfanos();
        if (!huerfanos.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    AppLocal.getIntString("message.withhold.orphansupport")
                    + " " + String.join(", ", huerfanos),
                    AppLocal.getIntString("label.withhold"),
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if (modelSupplier.getSelectedKey() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    LocalRes.getIntString("exception.noSupplier"),
                    AppLocal.getIntString("label.supplier"),
                    JOptionPane.OK_OPTION
            );
            cboSupplier.requestFocus();
            return false;
        }

        if (modelTaxSupport.getSelectedKey() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    LocalRes.getIntString("exception.noTaxSupport"),
                    AppLocal.getIntString("label.taxSupport"),
                    JOptionPane.OK_OPTION
            );
            cboTaxSupport.requestFocus();
            return false;
        }

        if (modelDocumentType.getSelectedKey() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    LocalRes.getIntString("exception.noDocumentType"),
                    AppLocal.getIntString("label.supplierdocment"),
                    JOptionPane.OK_OPTION
            );
            cboDocumentType.requestFocus();
            return false;
        }

        if (txtDatePurchase.getText() == null || txtDatePurchase.getText().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    LocalRes.getIntString("exception.noPurchaseDate"),
                    AppLocal.getIntString("label.date"),
                    JOptionPane.OK_OPTION
            );
            txtDatePurchase.requestFocus();
            return false;
        } else {
            try {
                Date d = (Date) Formats.DATE.parseValue(txtDatePurchase.getText());
            } catch (BasicException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        LocalRes.getIntString("exception.noPurchaseDate"),
                        AppLocal.getIntString("label.date"),
                        JOptionPane.OK_OPTION
                );
                txtDatePurchase.requestFocus();
                return false;
            }
        }

        if (txtSerie.getText() == null || txtSerie.getText().isEmpty()) {
            if (!"03".equals(modelDocumentType.getSelectedKey())) {
                JOptionPane.showMessageDialog(
                        this,
                        LocalRes.getIntString("exception.noDocumentNumber"),
                        AppLocal.getIntString("label.Number"),
                        JOptionPane.OK_OPTION
                );
                txtSerie.requestFocus();
                return false;
            }
        }

        if (isEcuador() && !SERIE_PATTERN.matcher(txtSerie.getText().trim()).matches()) {
            if (!"03".equals(modelDocumentType.getSelectedKey())) {
                JOptionPane.showMessageDialog(
                        this,
                        LocalRes.getIntString("exception.invalidDocumentNumber"),
                        AppLocal.getIntString("label.Number"),
                        JOptionPane.OK_OPTION
                );
                txtSerie.requestFocus();
                return false;
            }
        }

        if (txtAuthorization.getText() == null || txtAuthorization.getText().isEmpty()) {
            if (!"03".equals(modelDocumentType.getSelectedKey())) {
                JOptionPane.showMessageDialog(
                        this,
                        LocalRes.getIntString("exception.noAuthorization"),
                        AppLocal.getIntString("label.authorization"),
                        JOptionPane.OK_OPTION
                );
                txtAuthorization.requestFocus();
                return false;
            }
        }

        if (isEcuador() && !AUTHORIZATION_PATTERN.matcher(txtAuthorization.getText().trim()).matches()) {
            if (!"03".equals(modelDocumentType.getSelectedKey())) {
                JOptionPane.showMessageDialog(
                        this,
                        LocalRes.getIntString("exception.invalidAuthorization"),
                        AppLocal.getIntString("label.authorization"),
                        JOptionPane.OK_OPTION
                );
                txtAuthorization.requestFocus();
                return false;
            }
        }

        if (purchaseLines.getCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    AppLocal.getIntString("message.productnotselected"),
                    AppLocal.getIntString("Menu.Products"),
                    JOptionPane.OK_OPTION
            );
            return false;
        }

        return true;
    }

    private TicketsNumInfo getSeriePurchase(UserInfo user, String code) throws BasicException {
        var dataLogicTicketsNum
                = (DataLogicTicketsNumPurchase) app
                        .getBean("dev.joguenco.pos.ticketsnumpurchase.DataLogicTicketsNumPurchase");

        // Get serie and code actives
        var ticketNum = (TicketsNumInfo) dataLogicTicketsNum
                .getSerial()
                .find(user.getId(), code, "primary");

        return ticketNum;
    }

    /**
     * Comprueba si el sistema esta configurado para Ecuador
     */
    private boolean isEcuador() {
        return "EC".equals(this.country);
    }


    private void cmdInsertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdInsertActionPerformed
        stateToInsert();
        cboReason.requestFocus();
    }//GEN-LAST:event_cmdInsertActionPerformed

    private void cmdFindProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFindProductActionPerformed
        productFinder();
    }//GEN-LAST:event_cmdFindProductActionPerformed

    private void productFinder() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PurchaseProductDialog dialog = new PurchaseProductDialog(app, new javax.swing.JFrame(), true);

                // Se le pasa el sustento de la cabecera como valor de partida:
                // casi siempre todos los items llevan el mismo.
                dialog.setTaxSupport(modelTaxSupport.getSelectedKey() == null
                        ? null : modelTaxSupport.getSelectedKey().toString());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        dialog.setVisible(false);
                    }
                });
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                if (dialog.getReturnStatus() == PurchaseProductDialog.RET_OK) {
                    ProductInfoExt product = dialog.getProduct();
                    incProduct(product, dialog.getTax(), dialog.getTaxSupport());
                }
            }
        });
    }

    private void cmdDeleteAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDeleteAllActionPerformed
        int res = JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.wannadelete"),
                AppLocal.getIntString("title.editor"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.YES_OPTION) {

            int i = 0;
            while (i < purchaseLines.getCount()) {
                purchaseLines.deleteLine(i);
                this.purchase.getInvLines().remove(i);
            }
        }
        printInvLines();
    }//GEN-LAST:event_cmdDeleteAllActionPerformed

    private void cmdDeleteProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDeleteProductActionPerformed
        int i = purchaseLines.getSelectedRow();

        if (i < 0) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            removeInvLine(i);

        }
        printInvLines();
    }//GEN-LAST:event_cmdDeleteProductActionPerformed

    private void txtDatePurchaseFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDatePurchaseFocusGained
        if (txtDatePurchase.getText() == null || txtDatePurchase.getText().isEmpty()) {
            txtDatePurchase.setText(Formats.DATE.formatValue(DateUtils.getToday()));
        }
    }//GEN-LAST:event_txtDatePurchaseFocusGained

    private void cmdSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSearchActionPerformed
        PurchaseFinderDialog dialog = new PurchaseFinderDialog(app, new javax.swing.JFrame());
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dialog.setVisible(false);
            }
        });
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        if (dialog.getReturnStatus() == PurchaseProductDialog.RET_OK) {
            loadPurchase(dialog.getSelectedPurchase());
        }
    }//GEN-LAST:event_cmdSearchActionPerformed

    private void cmdDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdDeleteActionPerformed
        var status = JOptionPane.showConfirmDialog(
                this,
                AppLocal.getIntString("message.deletelineyes"),
                AppLocal.getIntString("label.purchase"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (status == JOptionPane.YES_OPTION) {
            if (purchase != null) {
                try {
                    dlPurchase.deleteTicket(purchase, dlSales);
                    stateToInsert();
                    cboReason.requestFocus();
                } catch (BasicException ex) {
                    log.error(PurchaseEditor.class.getName() + " cmdDeleteActionPerformed " + ex);
                }
            }
        }
    }//GEN-LAST:event_cmdDeleteActionPerformed

    private void cmdWithholdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdWithholdActionPerformed
        openWithholdDialog();
    }//GEN-LAST:event_cmdWithholdActionPerformed

    /**
     * Los sustentos distintos que usan los productos de la compra.
     *
     * Se cruzan con el catalogo para traer el nombre, porque la linea solo
     * guarda el codigo y el combo tiene que mostrar algo legible.
     */
    private java.util.List<Object> sustentosDeLaCompra() {
        var codigos = purchaseLines.getLines().stream()
                .map(InventoryLine::getTaxSupport)
                .filter(c -> c != null && !c.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        var sustentos = new java.util.ArrayList<Object>();
        try {
            for (Object item : sentTaxSupport.list()) {
                var clave = ((com.unicenta.data.loader.IKeyed) item).getKey();
                if (clave != null && codigos.contains(clave.toString())) {
                    sustentos.add(item);
                }
            }
        } catch (Exception e) {
            log.error(PurchaseEditor.class.getName() + " " + e.getMessage());
        }

        return sustentos;
    }

    /**
     * El establecimiento al que pertenece una serie.
     *
     * Los tres primeros digitos de la serie son el codigo del establecimiento,
     * asi que con eso alcanza para traer nombre comercial, direccion, telefono
     * y correo del local que emite. Es el mismo camino que usa la factura.
     *
     * Devuelve null si no se puede resolver: la impresion se lo salta y el
     * documento sale sin esa cabecera, pero sale.
     */
    private EstablishmentInfo findEstablishment(String serie) {
        if (serie == null || serie.length() < 3) {
            return null;
        }

        try {
            return (EstablishmentInfo) dlEstablishment
                    .getEstablishmentInfo()
                    .find(serie.substring(0, 3));
        } catch (BasicException e) {
            log.error(PurchaseEditor.class.getName() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * Imprime la liquidacion de compra.
     *
     * La factura del proveedor no se imprime: la emite el y ya te la entrega.
     * La liquidacion la emitimos nosotros, asi que hay que darle una copia.
     */
    private void printLiquidation() {
        print("Printer.Liquidation", script -> {
            // Todo lo demas (emisor, local, proveedor) cuelga de purchase, para
            // que la plantilla nunca pregunte por un objeto que puede ser nulo.
            script.put("purchase", purchase);
            script.put("lines", purchase.getInvLines());
        });
    }

    /**
     * Imprime el comprobante de retencion.
     *
     * El proveedor lo necesita como credito tributario, asi que se imprime
     * siempre que la compra genere una retencion, sea sobre factura o sobre
     * liquidacion.
     */
    private void printWithhold() {
        print("Printer.Withhold", script -> {
            script.put("withhold", pendingWithhold);
            script.put("purchase", purchase);
            script.put("lines", pendingWithhold.getLines());
        });
    }

    /**
     * Resuelve una plantilla y la manda a la impresora.
     *
     * Los errores se avisan pero no cortan nada: la compra ya quedo grabada, y
     * no poder imprimirla no la invalida. Frenar aqui dejaria al usuario
     * pensando que no se guardo.
     */
    private void print(String plantilla, java.util.function.Consumer<ScriptEngine> datos) {
        var recurso = dlSystem.getResourceAsXML(plantilla);

        if (recurso == null) {
            showPrintError();
            return;
        }

        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);

            // Cada nombre que se pone aqui es el que la plantilla usa con $
            datos.accept(script);

            ticketParser.printTicket(script.eval(recurso).toString());
        } catch (Exception e) {
            log.error(PurchaseEditor.class.getName() + " " + e.getMessage());
            showPrintError();
        }
    }

    private void showPrintError() {
        JOptionPane.showMessageDialog(
                this,
                AppLocal.getIntString("message.cannotprintticket"),
                AppLocal.getIntString("label.purchase"),
                JOptionPane.WARNING_MESSAGE
        );
    }

    /**
     * La fecha del documento del proveedor, o null si todavia no se escribio.
     */
    private Date parsePurchaseDate() {
        try {
            return (Date) Formats.DATE.parseValue(txtDatePurchase.getText());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Abre el dialogo de retencion. No exige que la compra este guardada: lo
     * que arme el usuario se queda en memoria y se graba junto con la compra.
     */
    private void openWithholdDialog() {
        // El dialogo lee estos tres del objeto compra, y antes se llenaban recien
        // al guardar. Ahora que abre primero, hay que pasarle lo que hay en el
        // formulario o mostraria nulos y tomaria el periodo fiscal equivocado.
        purchase.setPurchaseDocument(modelDocumentType.getSelectedKey() == null
                ? null : modelDocumentType.getSelectedKey().toString());
        purchase.setPurchaseReference(txtSerie.getText());
        purchase.setPurchaseDate(parsePurchaseDate());

        // Antes se le mandaban dos totales ya sumados. Ahora el dialogo suma solo
        // las lineas del sustento que se elija, asi que necesita las lineas.
        final var lines = purchaseLines.getLines();

        // El nombre del proveedor es solo para mostrarlo en el dialogo. El combo
        // lo tiene; el PurchaseInfo del buscador a veces trae solo el id, y en
        // una compra nueva puede no tener proveedor todavia.
        var selected = modelSupplier.getSelectedItem();
        final String supplierName;
        if (selected != null) {
            supplierName = selected.toString();
        } else if (purchase != null && purchase.getSupplier() != null) {
            supplierName = purchase.getSupplier().getName();
        } else {
            supplierName = "";
        }

        // Los sustentos salen de los productos que hay en pantalla, no de la
        // base: esta compra todavia no se guardo.
        final var sustentos = sustentosDeLaCompra();

        java.awt.EventQueue.invokeLater(() -> {
            var dialog = new WithholdDialog(app, new javax.swing.JFrame(), true,
                    purchase, supplierName, lines, sustentos, pendingWithhold);
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            if (dialog.getReturnStatus() == WithholdDialog.RET_OK) {
                pendingWithhold = dialog.getWithhold();
                updateWithholdButton();
            }
        });
    }

    /**
     * Decide si se puede armar una retencion sobre lo que hay en pantalla.
     *
     * Hacen falta dos cosas: un documento que admita retencion y al menos un
     * producto cargado.
     *
     * Se retiene sobre una factura (01) o una liquidacion de compra (03). Una
     * nota de venta (02) no da derecho a credito tributario y una nota de
     * credito (04) es un documento de ajuste; ninguna de las dos lleva
     * retencion, asi que el boton se apaga.
     *
     * Si ya se armo una en esta pantalla el boton sigue vivo: mientras la compra
     * no se guarde no hay nada escrito en la base, asi que se puede corregir.
     *
     * En una compra ya guardada la regla se invierte: el boton se prende solo
     * si esa compra TIENE retencion, porque ahi sirve para verla. Sin ella no
     * hay nada que mostrar.
     */
    private void updateWithholdButton() {
        var documento = modelDocumentType == null
                ? null : modelDocumentType.getSelectedKey();

        if (!"01".equals(documento) && !"03".equals(documento)) {
            cmdWithhold.setEnabled(false);
            return;
        }

        // Sin productos no hay sobre que retener: la base imponible y el IVA
        // salen de las lineas de la compra, y con la grilla vacia serian cero.
        if (purchaseLines == null || purchaseLines.getLines().isEmpty()) {
            cmdWithhold.setEnabled(false);
            return;
        }

        // Compra nueva: se arma la retencion junto con ella.
        if (purchase == null || purchase.getNumber() == null) {
            cmdWithhold.setEnabled(true);
            return;
        }

        // Compra del buscador: el boton es solo para consultar. Si esa compra
        // no tiene retencion no hay nada que ver, y crearsela aparte tampoco
        // corresponde: la retencion se emite junto con su documento.
        cmdWithhold.setEnabled(loadedHasWithhold);
    }

    private void loadPurchase(PurchaseInfo purchase) {
        this.purchase = purchase;

        txtNumber.setText(purchase.getNumber() == null ? null : purchase.getNumber().toString());
        txtCreatedAt.setText(purchase.getCreatedAt() == null ? null : Formats.TIMESTAMP.formatValue(purchase.getCreatedAt()));
        txtObservation.setText(purchase.getObservation());
        txtSerie.setText(purchase.getPurchaseReference());
        txtDatePurchase.setText(purchase.getPurchaseDate() == null ? null : Formats.DATE.formatValue(purchase.getPurchaseDate()));
        txtAuthorization.setText(purchase.getPurchaseAuthorization());

        modelReason.setSelectedKey(purchase.getReason());
        modelSupplier.setSelectedKey(purchase.getSupplier().getID());
        modelDocumentType.setSelectedKey(purchase.getPurchaseDocument());
        modelTaxSupport.setSelectedKey(purchase.getPurchaseTaxSupport());

        purchaseLines.clear();
        for (InventoryLine line : purchase.getInvLines()) {
            purchaseLines.addLine(line);
        }

        try {
            loadedHasWithhold = dlWithhold.countByPurchase(purchase.getId()) > 0;
        } catch (Exception e) {
            log.error(PurchaseEditor.class.getName() + " " + e.getMessage());
            loadedHasWithhold = false;
        }

        printInvLines();
        enableForm(false);
        updateWithholdButton();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cboDocumentType;
    private javax.swing.JComboBox<String> cboLocation;
    private javax.swing.JComboBox<String> cboReason;
    private javax.swing.JComboBox<String> cboSupplier;
    private javax.swing.JComboBox<String> cboTaxSupport;
    private javax.swing.JButton cmdDelete;
    private javax.swing.JButton cmdDeleteAll;
    private javax.swing.JButton cmdDeleteProduct;
    private javax.swing.JButton cmdFindProduct;
    private javax.swing.JButton cmdInsert;
    private javax.swing.JButton cmdSave;
    private javax.swing.JButton cmdSearch;
    private javax.swing.JButton cmdWithhold;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblAuthorization;
    private javax.swing.JLabel lblCreatedAt;
    private javax.swing.JLabel lblDatePurchase;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblDocument;
    private javax.swing.JLabel lblNumber;
    private javax.swing.JLabel lblReason;
    private javax.swing.JLabel lblSerie;
    private javax.swing.JLabel lblSubtotal;
    private javax.swing.JLabel lblSubtotalTitle;
    private javax.swing.JLabel lblSupplier;
    private javax.swing.JLabel lblTax;
    private javax.swing.JLabel lblTaxSupport;
    private javax.swing.JLabel lblTaxTitle;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalTitle;
    private javax.swing.JPanel panelAction;
    private javax.swing.JPanel panelHead;
    private javax.swing.JPanel panelLines;
    private javax.swing.JPanel panelProduct;
    private javax.swing.JPanel panelTax;
    private javax.swing.JPanel panelTotal;
    private javax.swing.JTextField txtAuthorization;
    private javax.swing.JTextField txtCreatedAt;
    private javax.swing.JTextField txtDatePurchase;
    private javax.swing.JTextField txtNumber;
    private javax.swing.JTextField txtObservation;
    private javax.swing.JTextField txtSerie;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getTitle() {
        return AppLocal.getIntString("label.purchase");
    }

    @Override
    public void activate() throws BasicException {
        try {
            modelReason = new ComboBoxValModel();
            modelReason.add(MovementReason.IN_PURCHASE);    //Supplier Purchase
            //modelReason.add(MovementReason.OUT_REFUND);   //Supplier Return
            cboReason.setModel(modelReason);

            sentSupplier = dlSupplier.getSupplierList();
            modelSupplier = new ComboBoxValModel(sentSupplier.list());
            cboSupplier.setModel(modelSupplier);

            sentTaxSupport = dlPurchase.getTaxSupportList();
            modelTaxSupport = new ComboBoxValModel(sentTaxSupport.list());
            cboTaxSupport.setModel(modelTaxSupport);

            modelDocumentType = new ComboBoxValModel(
                    dlPurchase.getDocumentTypeList().list()
            );
            cboDocumentType.setModel(modelDocumentType);

            // Cambiar el tipo de documento prende o apaga el boton de retencion
            cboDocumentType.addActionListener(evt -> updateWithholdButton());

            // Load locations
            modelLocation = new ComboBoxValModel(dlSales.getLocationsList().list());
            cboLocation.setModel(modelLocation);

            stateToInsert();
        } catch (BasicException ex) {
            log.error(PurchaseEditor.class.getName() + " " + ex.getMessage());
        }
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

        dlPurchase = (DataLogicPurchase) app.getBean("dev.resolvedor.pos.inventory.management.DataLogicPurchase");
        dlSupplier = (DataLogicSuppliers) app.getBean("com.unicenta.pos.suppliers.DataLogicSuppliers");
        dlSales = (DataLogicSales) app.getBean("com.unicenta.pos.forms.DataLogicSales");
        dlTaxPayer = (DataLogicTaxpayer) app.getBean("dev.joguenco.pos.taxpayer.DataLogicTaxpayer");
        dlEstablishment = (DataLogicEstablishment) app.getBean("dev.joguenco.pos.establishment.DataLogicEstablishment");
        dlSystem = (DataLogicSystem) app.getBean("com.unicenta.pos.forms.DataLogicSystem");
        dlWithhold = (DataLogicWithhold) app.getBean("dev.resolvedor.pos.inventory.management.DataLogicWithhold");
        ticketParser = new TicketParser(app.getDeviceTicket(), dlSystem);
    }

    @Override
    public Object getBean() {
        return this;
    }

    public void stateToInsert() {
        pendingWithhold = null;
        loadedHasWithhold = false;
        retencionAvisada = false;
        enableForm(true);

        txtNumber.setText(null);
        txtCreatedAt.setText(null);
        txtObservation.setText(null);
        txtSerie.setText(null);
        txtDatePurchase.setText(null);
        txtAuthorization.setText(null);

        modelLocation.setSelectedKey(app.getInventoryLocation());
        modelReason.setSelectedItem(MovementReason.IN_PURCHASE);
        modelSupplier.setSelectedKey(null);
        modelDocumentType.setSelectedKey(null);
        modelTaxSupport.setSelectedKey(null);

        txtNumber.setEnabled(true);
        txtCreatedAt.setEnabled(true);
        txtObservation.setEnabled(true);
        txtSerie.setEnabled(true);
        txtDatePurchase.setEnabled(true);
        txtAuthorization.setEnabled(true);
        cboReason.setEnabled(true);
        cboSupplier.setEnabled(true);

        purchase = new PurchaseInfo();
        purchase.setUser(app.getAppUserView().getUser().getUserInfo());
        purchaseLines.clear();
        updateWithholdButton();

        cboReason.requestFocus();
    }

    private void incProduct(ProductInfoExt product, TaxInfo tax, String taxSupport) {
        addLine(product, product.getQuantity(), product.getTotalCost(), product.getLot(),
                tax, taxSupport);
        printInvLines();
    }

    /**
     * Agrega la linea a la grilla y a la compra.
     *
     * Son dos objetos distintos a proposito: la grilla dibuja uno y la compra
     * guarda el otro. El sustento se le pone a los dos, o al grabar se perderia.
     */
    private void addLine(ProductInfoExt oProduct, double dpor, double totalCost, String lot,
            TaxInfo tax, String taxSupport) {

        var enGrilla = new InventoryLine(oProduct, dpor, totalCost, lot, tax);
        enGrilla.setTaxSupport(taxSupport);
        purchaseLines.addLine(enGrilla);

        var enCompra = new InventoryLine(oProduct, dpor, totalCost, lot, tax);
        enCompra.setTaxSupport(taxSupport);
        this.purchase.getInvLines().add(enCompra);
    }

    private void removeInvLine(int index) {

        if (index < 0) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            purchaseLines.deleteLine(index);
            this.purchase.getInvLines().remove(index);
        }
    }

    /**
     * Refresca los totales. Se llama despues de cada cambio en las lineas, asi
     * que es tambien el lugar natural para revisar el boton de retencion.
     */
    private void printInvLines() {
        lblSubtotal.setText(purchase.printSubTotal());
        lblTax.setText(purchase.printTax());
        lblTotal.setText(purchase.printTotal());

        updateWithholdButton();
        avisarSiLaRetencionQuedoDesfasada();
    }

    private void enableForm(boolean status) {
        txtNumber.setEnabled(status);
        txtCreatedAt.setEnabled(status);
        txtObservation.setEnabled(status);
        txtSerie.setEnabled(status);
        txtDatePurchase.setEnabled(status);
        txtAuthorization.setEnabled(status);

        cboLocation.setEnabled(status);
        cboReason.setEnabled(status);
        cboSupplier.setEnabled(status);
        cboTaxSupport.setEnabled(status);
        cboDocumentType.setEnabled(status);

        cmdDeleteProduct.setEnabled(status);
        cmdFindProduct.setEnabled(status);
        cmdDeleteAll.setEnabled(status);
        cmdSave.setEnabled(status);

        if (status) {
            cmdDelete.setEnabled(false);
        } else {
            cmdDelete.setEnabled(true);
        }
    }

    /**
     * Da formato al numero de factura mientras se escribe: solo acepta digitos
     * y coloca los guiones en su sitio -> 001-001-000000001
     */
    private static class SerieFilter extends DocumentFilter {

        private static final int TOTAL_DIGITS = 15;

        @Override
        public void insertString(FilterBypass fb, int offset,
                String text, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, text, attr);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length)
                throws BadLocationException {
            replace(fb, offset, length, "", null);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length,
                String text, AttributeSet attr) throws BadLocationException {

            Document doc = fb.getDocument();
            String current = doc.getText(0, doc.getLength());

            // Como quedaria el texto si dejaramos pasar el cambio
            String updated = current.substring(0, offset)
                    + (text == null ? "" : text)
                    + current.substring(offset + length);

            // Nos quedamos solo con los numeros y volvemos a poner los guiones
            String digits = updated.replaceAll("\\D", "");
            if (digits.length() > TOTAL_DIGITS) {
                digits = digits.substring(0, TOTAL_DIGITS);
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i == 3 || i == 6) {
                    sb.append('-');
                }
                sb.append(digits.charAt(i));
            }

            fb.replace(0, doc.getLength(), sb.toString(), attr);
        }
    }

    /**
     * Solo deja escribir digitos, hasta un maximo de caracteres.
     */
    private static class DigitsFilter extends DocumentFilter {

        private final int maximum;

        DigitsFilter(int maximum) {
            this.maximum = maximum;
        }

        @Override
        public void insertString(FilterBypass fb, int offset,
                String text, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, text, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length,
                String text, AttributeSet attr) throws BadLocationException {

            String cleaned = (text == null) ? "" : text.replaceAll("\\D", "");

            int freeSpace = maximum - (fb.getDocument().getLength() - length);
            if (cleaned.length() > freeSpace) {
                if (freeSpace <= 0) {
                    return;
                }
                cleaned = cleaned.substring(0, freeSpace);
            }

            fb.replace(offset, length, cleaned, attr);
        }
    }
}
