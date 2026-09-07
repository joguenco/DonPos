package dev.resolvedor.pos.inventory.management;

import com.unicenta.data.gui.ComboBoxValModel;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.forms.AppView;
import com.unicenta.pos.forms.DataLogicSystem;
import com.unicenta.pos.inventory.InventoryLine;
import dev.joguenco.pos.taxpayer.DataLogicTaxpayer;
import dev.joguenco.pos.taxpayer.TaxpayerInfo;
import dev.joguenco.pos.ticketsnum.TicketsNumInfo;
import dev.joguenco.pos.ticketsnumpurchase.DataLogicTicketsNumPurchase;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

/**
 * Dialogo para emitir el comprobante de retencion de una compra.
 *
 * Se abre desde PurchaseEditor sobre una compra YA GUARDADA: withholds.purchase_id
 * es una clave foranea a purchases.id, asi que la compra tiene que existir antes.
 *
 * La parte visual vive en WithholdDialog.form y se edita con el disenador de
 * NetBeans. No tocar a mano el bloque initComponents ni las marcas //GEN-.
 */
public class WithholdDialog extends javax.swing.JDialog {

    public static final int RET_CANCEL = 0;
    public static final int RET_OK = 1;

    private int returnStatus = RET_CANCEL;

    private final AppView app;
    private DataLogicWithhold dlWithhold;
    private DataLogicTaxpayer dlTaxPayer;
    private DataLogicSystem dlSystem;

    private WithholdInfo withhold = new WithholdInfo();

    /** La compra ya tenia retencion: el dialogo abre solo para verla. */
    private boolean readOnly = false;

    /** Las lineas de la compra, para poder sumarlas por sustento. */
    private final List<InventoryLine> lines;

    /** Sustento de la cabecera: lo heredan las lineas que no traen el suyo. */
    private final String headerTaxSupport;

    // Dejaron de ser final: cambian cada vez que se mueve el filtro.
    private Double subtotal = 0.0;
    private Double iva = 0.0;

    private ComboBoxValModel modelWithholdTax;
    private ComboBoxValModel modelTaxSupport;

    /** Sustento unico de la compra, cuando el combo no se muestra. */
    private String fixedTaxSupport;
    private final LinesTableModel linesModel = new LinesTableModel();

    public WithholdDialog(AppView app, java.awt.Frame parent, boolean modal,
            PurchaseInfo purchase, String supplierName, List<InventoryLine> lines,
            java.util.List<Object> taxSupports, WithholdInfo pending) {
        super(parent, modal);

        this.app = app;
        this.lines = lines;
        this.headerTaxSupport = purchase.getPurchaseTaxSupport();

        initComponents();

        setTitle(AppLocal.getIntString("label.withhold"));
        jTableLines.setModel(linesModel);
        jTableLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        dlWithhold = (DataLogicWithhold) app.getBean(
                "dev.resolvedor.pos.inventory.management.DataLogicWithhold");
        dlTaxPayer = (DataLogicTaxpayer) app.getBean(
                "dev.joguenco.pos.taxpayer.DataLogicTaxpayer");
        dlSystem = (DataLogicSystem) app.getBean(
                "com.unicenta.pos.forms.DataLogicSystem");

        loadWithholdTaxes();
        loadTaxSupports(taxSupports, purchase.getPurchaseTaxSupport());
        loadPurchase(purchase, supplierName);

        if (pending != null) {
            loadPending(pending);
        } else {
            loadExistingWithhold();
        }
    }

    public int getReturnStatus() {
        return returnStatus;
    }

    public WithholdInfo getWithhold() {
        return withhold;
    }

    /**
     * Vuelve a cargar la retencion que se armo antes de guardar la compra.
     *
     * Sigue editable: mientras no se guarde la compra, nada se escribio en la
     * base y el usuario puede corregir lo que quiera.
     */
    private void loadPending(WithholdInfo pending) {
        withhold = pending;

        txtDate.setText(new SimpleDateFormat("dd-MM-yyyy").format(withhold.getDateWithhold()));
        txtFiscalPeriod.setText(new SimpleDateFormat("MM/yyyy").format(withhold.getFiscalPeriod()));
        txtObservation.setText(withhold.getObservation());

        for (WithholdLineInfo line : withhold.getLines()) {
            linesModel.addLine(line);
        }

        refreshTotal();
    }

    /**
     * Si la compra ya tiene retencion, la muestra y bloquea todo.
     *
     * Este dialogo dejo de servir para corregir: una retencion se crea junto
     * con su compra y despues solo se consulta. Corregirla, si hiciera falta,
     * se hace anulando en el SRI y emitiendo otra.
     */
    private void loadExistingWithhold() {
        try {
            var existing = dlWithhold.getByPurchase(withhold.getPurchaseId());

            if (existing == null) {
                return;
            }

            existing.setUser(withhold.getUser());
            existing.setLines(dlWithhold.getLinesByWithhold(existing.getId()));
            withhold = existing;

            txtDate.setText(new SimpleDateFormat("dd-MM-yyyy").format(withhold.getDateWithhold()));
            txtFiscalPeriod.setText(new SimpleDateFormat("MM/yyyy").format(withhold.getFiscalPeriod()));
            txtObservation.setText(withhold.getObservation());

            for (WithholdLineInfo line : withhold.getLines()) {
                linesModel.addLine(line);
            }
            refreshTotal();

            setTitle(AppLocal.getIntString("label.withhold") + "  " + withhold.getSerieNumber());
            setReadOnly(true);
        } catch (Exception e) {
            showError("message.cannotloaddata", e);
        }
    }

    /**
     * Apaga el formulario entero. El boton de crear no se pone gris sino que
     * desaparece: en modo consulta no hay nada que crear, y un boton apagado
     * invita a preguntarse por que no funciona.
     */
    private void setReadOnly(boolean value) {
        readOnly = value;

        txtDate.setEditable(!value);
        txtFiscalPeriod.setEditable(!value);
        txtObservation.setEditable(!value);
        txtBase.setEditable(!value);
        txtPercentage.setEditable(!value);
        txtValue.setEditable(!value);

        cboWithholdTax.setEnabled(!value);
        cboTaxSupport.setEnabled(!value);
        cmdAdd.setEnabled(!value);
        cmdRemove.setEnabled(!value);
        cmdSave.setVisible(!value);
    }

    // -----------------------------------------------------------------------
    // Carga
    // -----------------------------------------------------------------------
    private void loadWithholdTaxes() {
        try {
            modelWithholdTax = new ComboBoxValModel(dlWithhold.getWithholdTaxList().list());
            cboWithholdTax.setModel(modelWithholdTax);
            modelWithholdTax.setSelectedKey(null);
        } catch (Exception e) {
            showError("message.cannotloaddata", e);
        }
    }

    /**
     * Sustentos disponibles: solo los que esa compra realmente usa.
     *
     * Vienen de la pantalla de Compra, no de la base: la compra todavia no
     * esta guardada, sus productos estan en memoria.
     *
     * El combo solo aparece cuando hay algo que decidir. Con un solo sustento
     * queda elegido y se esconde; sin ninguno (compras viejas, guardadas antes
     * de que se pidiera el sustento por producto) se cae al de la cabecera, que
     * es lo mismo que hace la vista al armar el XML.
     */
    /**
     * Vuelve a sumar la compra tomando solo las lineas del sustento elegido.
     *
     * El SRI retiene por sustento, no por compra: si la factura trae transporte
     * (04) y mercaderia (01), la base de la retencion de transporte es solo el
     * transporte. Antes el usuario tenia que sacar ese numero con calculadora.
     *
     * Sin sustento elegido suma todo, que es como venia funcionando.
     */
    private void recalculateTotals() {
        subtotal = 0.0;
        iva = 0.0;

        // Sin sustento elegido no hay nada que mostrar: los totales serian los
        // de toda la compra y el usuario terminaria reteniendo sobre una base
        // que no le corresponde. Se dejan en blanco y se traba el tipo.
        if (!isTaxSupportChosen()) {
            cboWithholdTax.setEnabled(false);
            txtSubtotal.setText("");
            txtIva.setText("");
            clearEntry();

            return;
        }

        cboWithholdTax.setEnabled(!readOnly);

        var selected = selectedTaxSupport();

        for (InventoryLine line : lines) {
            if (selected == null || selected.equals(taxSupportOf(line))) {
                subtotal += line.getSubValue();
                iva += line.getTaxPurchase();
            }
        }

        txtSubtotal.setText(Formats.CURRENCY.formatValue(subtotal));
        txtIva.setText(Formats.CURRENCY.formatValue(iva));

        // Si ya habia un tipo elegido, su base quedo vieja: se rehace sola para
        // no obligar a volver a elegirlo. Las lineas ya agregadas no se tocan,
        // cada una guarda el sustento con el que se creo.
        fillBaseFromSelectedTax();
    }

    /**
     * Si ya hay un sustento con el que trabajar.
     *
     * Con un solo sustento el combo esta escondido: no hay nada que elegir y el
     * formulario arranca listo. Con varios hay que esperar a que el usuario
     * decida.
     */
    private boolean isTaxSupportChosen() {
        return !cboTaxSupport.isVisible() || selectedTaxSupport() != null;
    }

    /**
     * Vacia el renglon que se esta armando: tipo, base, porcentaje e importe.
     */
    private void clearEntry() {
        if (modelWithholdTax != null) {
            modelWithholdTax.setSelectedKey(null);
        }

        txtPercentage.setText("");
        txtBase.setText("");
        txtValue.setText("");
    }

    /**
     * Rellena base y porcentaje segun el tipo elegido.
     *
     * La base depende de tax_type: RENTA se calcula sobre el subtotal, IVA
     * sobre el valor del IVA. Y como esos dos ya vienen filtrados por sustento,
     * la base sale filtrada sin hacer nada mas.
     */
    private void fillBaseFromSelectedTax() {
        var tax = getSelectedTax();

        if (tax == null) {
            txtPercentage.setText("");
            txtBase.setText("");
            txtValue.setText("");

            return;
        }

        txtPercentage.setText(Formats.DOUBLE.formatValue(tax.getPercentage()));
        txtBase.setText(Formats.CURRENCY.formatValue(
                "IVA".equals(tax.getTaxType()) ? iva : subtotal));

        recalculateValue();
    }

    /**
     * El sustento por el que hay que filtrar, o null para no filtrar nada.
     *
     * Cuando el combo esta escondido hay un solo sustento en toda la compra, y
     * el filtro deja pasar igual todas las lineas.
     */
    private String selectedTaxSupport() {
        if (!cboTaxSupport.isVisible()) {
            return fixedTaxSupport;
        }

        var key = modelTaxSupport == null ? null : modelTaxSupport.getSelectedKey();

        return key == null ? null : key.toString();
    }

    /**
     * El sustento de una linea.
     *
     * Si el producto no trae el suyo hereda el de la cabecera, que es lo mismo
     * que hace la vista v_ele_withholds_support al armar el XML. Si la pantalla
     * y la vista no coincidieran, el usuario veria un total en el dialogo y
     * otro distinto en el comprobante autorizado.
     */
    private String taxSupportOf(InventoryLine line) {
        var value = line.getTaxSupport();

        return value == null || value.trim().isEmpty() ? headerTaxSupport : value;
    }

    private void loadTaxSupports(java.util.List<Object> taxSupports, String headerTaxSupport) {
        modelTaxSupport = new ComboBoxValModel(taxSupports);
        cboTaxSupport.setModel(modelTaxSupport);

        if (taxSupports.size() > 1) {
            modelTaxSupport.setSelectedKey(null);
            return;
        }

        var unico = taxSupports.isEmpty()
                ? headerTaxSupport
                : ((com.unicenta.data.loader.IKeyed) taxSupports.get(0)).getKey();

        modelTaxSupport.setSelectedKey(unico);

        // No hay nada que elegir: se esconde en vez de quedar gris, para que
        // no invite a preguntarse por que no responde.
        lblTaxSupportTitle.setVisible(false);
        cboTaxSupport.setVisible(false);

        if (unico == null) {
            fixedTaxSupport = null;
        } else {
            fixedTaxSupport = unico.toString();
        }
    }

    private void loadPurchase(PurchaseInfo purchase, String supplierName) {
        withhold.setPurchaseId(purchase.getId());
        withhold.setUser(app.getAppUserView().getUser().getUserInfo());

        txtSupplier.setText(supplierName);
        // En una liquidacion (03) el numero lo asigna el contador al guardar, asi
        // que aqui todavia no existe: se muestra solo el codigo del documento.
        var referencia = purchase.getPurchaseReference();
        txtDocument.setText(
                (purchase.getPurchaseDocument() == null ? "" : purchase.getPurchaseDocument())
                + (referencia == null || referencia.trim().isEmpty()
                        ? "" : "  " + referencia));
        recalculateTotals();

        var today = new Date();
        withhold.setDateWithhold(today);
        withhold.setFiscalPeriod(purchase.getPurchaseDate() == null
                ? today : purchase.getPurchaseDate());

        txtDate.setText(new SimpleDateFormat("dd-MM-yyyy").format(withhold.getDateWithhold()));
        txtFiscalPeriod.setText(new SimpleDateFormat("MM/yyyy").format(withhold.getFiscalPeriod()));
    }

    // -----------------------------------------------------------------------
    // Apoyo
    // -----------------------------------------------------------------------
    private void recalculateValue() {
        var tax = getSelectedTax();
        if (tax == null) {
            return;
        }

        var base = parseDouble(txtBase.getText());
        txtValue.setText(Formats.CURRENCY.formatValue(
                round(base * tax.getPercentage() / 100)));
    }

    private boolean validateForm() {
        if (linesModel.getRowCount() == 0) {
            showMessage("message.withhold.nolines");
            return false;
        }

        for (WithholdLineInfo line : linesModel.getLines()) {
            if (line.getBaseValue() == null || line.getBaseValue() <= 0) {
                showMessage("message.withhold.invalidbase");
                return false;
            }
        }

        return true;
    }

    private WithholdTaxInfo getSelectedTax() {
        return modelWithholdTax == null
                ? null
                : (WithholdTaxInfo) modelWithholdTax.getSelectedItem();
    }

    private void refreshTotal() {
        var total = linesModel.getLines().stream()
                .mapToDouble(WithholdLineInfo::getWithholdedValue)
                .sum();

        lblTotalWithheld.setText(Formats.CURRENCY.formatValue(round(total)));
    }

    private Double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        try {
            return ((Number) Formats.CURRENCY.parseValue(text)).doubleValue();
        } catch (Exception e) {
            try {
                return Double.parseDouble(text.trim().replace(",", "."));
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
    }

    private Date parseDate(String text, String pattern, Date fallback) {
        try {
            return new SimpleDateFormat(pattern).parse(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private Double round(Double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void showMessage(String key) {
        JOptionPane.showMessageDialog(this,
                AppLocal.getIntString(key),
                AppLocal.getIntString("label.withhold"),
                JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String key, Exception e) {
        JOptionPane.showMessageDialog(this,
                AppLocal.getIntString(key) + "\n" + e.getMessage(),
                AppLocal.getIntString("label.withhold"),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelTop = new javax.swing.JPanel();
        panelPurchase = new javax.swing.JPanel();
        lblSupplierTitle = new javax.swing.JLabel();
        txtSupplier = new javax.swing.JTextField();
        lblDocumentTitle = new javax.swing.JLabel();
        txtDocument = new javax.swing.JTextField();
        lblSubtotalTitle = new javax.swing.JLabel();
        txtSubtotal = new javax.swing.JTextField();
        lblIvaTitle = new javax.swing.JLabel();
        txtIva = new javax.swing.JTextField();
        panelDates = new javax.swing.JPanel();
        lblDateTitle = new javax.swing.JLabel();
        txtDate = new javax.swing.JTextField();
        lblFiscalPeriodTitle = new javax.swing.JLabel();
        txtFiscalPeriod = new javax.swing.JTextField();
        lblObservationTitle = new javax.swing.JLabel();
        txtObservation = new javax.swing.JTextField();
        panelType = new javax.swing.JPanel();
        lblWithholdTypeTitle = new javax.swing.JLabel();
        cboWithholdTax = new javax.swing.JComboBox();
        lblTaxSupportTitle = new javax.swing.JLabel();
        cboTaxSupport = new javax.swing.JComboBox();
        panelValues = new javax.swing.JPanel();
        lblBaseTitle = new javax.swing.JLabel();
        txtBase = new javax.swing.JTextField();
        lblPercentageTitle = new javax.swing.JLabel();
        txtPercentage = new javax.swing.JTextField();
        lblValueTitle = new javax.swing.JLabel();
        txtValue = new javax.swing.JTextField();
        cmdAdd = new javax.swing.JButton();
        cmdRemove = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableLines = new javax.swing.JTable();
        panelBottom = new javax.swing.JPanel();
        panelTotal = new javax.swing.JPanel();
        lblTotalWithheldTitle = new javax.swing.JLabel();
        lblTotalWithheld = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
        cmdSave = new javax.swing.JButton();
        cmdClose = new javax.swing.JButton();

        panelTop.setLayout(new java.awt.GridLayout(4, 1));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N

        panelPurchase.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblSupplierTitle.setText(bundle.getString("label.supplier")); // NOI18N
        panelPurchase.add(lblSupplierTitle);

        txtSupplier.setColumns(22);
        txtSupplier.setEditable(false);
        panelPurchase.add(txtSupplier);

        lblDocumentTitle.setText(bundle.getString("label.Number")); // NOI18N
        panelPurchase.add(lblDocumentTitle);

        txtDocument.setColumns(18);
        txtDocument.setEditable(false);
        panelPurchase.add(txtDocument);

        lblSubtotalTitle.setText(bundle.getString("label.subtotalcash")); // NOI18N
        panelPurchase.add(lblSubtotalTitle);

        txtSubtotal.setColumns(9);
        txtSubtotal.setEditable(false);
        panelPurchase.add(txtSubtotal);

        lblIvaTitle.setText(bundle.getString("label.taxes")); // NOI18N
        panelPurchase.add(lblIvaTitle);

        txtIva.setColumns(9);
        txtIva.setEditable(false);
        panelPurchase.add(txtIva);

        panelTop.add(panelPurchase);

        panelDates.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblDateTitle.setText(bundle.getString("label.date")); // NOI18N
        panelDates.add(lblDateTitle);

        txtDate.setColumns(10);
        panelDates.add(txtDate);

        lblFiscalPeriodTitle.setText(bundle.getString("label.fiscal.period")); // NOI18N
        panelDates.add(lblFiscalPeriodTitle);

        txtFiscalPeriod.setColumns(8);
        panelDates.add(txtFiscalPeriod);

        lblObservationTitle.setText(bundle.getString("label.observation")); // NOI18N
        panelDates.add(lblObservationTitle);

        txtObservation.setColumns(24);
        panelDates.add(txtObservation);

        panelTop.add(panelDates);

        panelType.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblWithholdTypeTitle.setText(bundle.getString("label.withhold.type")); // NOI18N
        panelType.add(lblWithholdTypeTitle);

        cboWithholdTax.setPreferredSize(new java.awt.Dimension(380, 26));
        cboWithholdTax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboWithholdTaxActionPerformed(evt);
            }
        });
        panelType.add(cboWithholdTax);

        lblTaxSupportTitle.setText(bundle.getString("label.taxSupport")); // NOI18N
        panelType.add(lblTaxSupportTitle);

        cboTaxSupport.setPreferredSize(new java.awt.Dimension(260, 26));
        cboTaxSupport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTaxSupportActionPerformed(evt);
            }
        });
        panelType.add(cboTaxSupport);

        panelTop.add(panelType);

        panelValues.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblBaseTitle.setText(bundle.getString("label.base")); // NOI18N
        panelValues.add(lblBaseTitle);

        txtBase.setColumns(9);
        txtBase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBaseActionPerformed(evt);
            }
        });
        txtBase.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtBaseFocusLost(evt);
            }
        });
        panelValues.add(txtBase);

        lblPercentageTitle.setText("%");
        panelValues.add(lblPercentageTitle);

        txtPercentage.setColumns(5);
        txtPercentage.setEditable(false);
        panelValues.add(txtPercentage);

        lblValueTitle.setText(bundle.getString("label.value")); // NOI18N
        panelValues.add(lblValueTitle);

        txtValue.setColumns(9);
        panelValues.add(txtValue);

        cmdAdd.setText(bundle.getString("Button.Add")); // NOI18N
        cmdAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdAddActionPerformed(evt);
            }
        });
        panelValues.add(cmdAdd);

        cmdRemove.setText(bundle.getString("Button.Remove")); // NOI18N
        cmdRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdRemoveActionPerformed(evt);
            }
        });
        panelValues.add(cmdRemove);

        panelTop.add(panelValues);

        getContentPane().add(panelTop, java.awt.BorderLayout.NORTH);

        jTableLines.setPreferredSize(new java.awt.Dimension(740, 200));
        jScrollPane1.setViewportView(jTableLines);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        panelBottom.setLayout(new java.awt.BorderLayout());

        panelTotal.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblTotalWithheldTitle.setText(bundle.getString("label.total.withheld")); // NOI18N
        panelTotal.add(lblTotalWithheldTitle);

        lblTotalWithheld.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lblTotalWithheld.setText("0.00");
        panelTotal.add(lblTotalWithheld);

        panelBottom.add(panelTotal, java.awt.BorderLayout.WEST);

        panelButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        cmdSave.setText(bundle.getString("label.withhold.create")); // NOI18N
        cmdSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdSaveActionPerformed(evt);
            }
        });
        panelButtons.add(cmdSave);

        cmdClose.setText(bundle.getString("Button.Close")); // NOI18N
        cmdClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdCloseActionPerformed(evt);
            }
        });
        panelButtons.add(cmdClose);

        panelBottom.add(panelButtons, java.awt.BorderLayout.EAST);

        getContentPane().add(panelBottom, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Al elegir el tipo se rellenan porcentaje y base.
     */
    private void cboWithholdTaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboWithholdTaxActionPerformed
        fillBaseFromSelectedTax();
    }//GEN-LAST:event_cboWithholdTaxActionPerformed

    /**
     * Al cambiar el sustento se vuelven a sumar los totales de la compra.
     */
    private void cboTaxSupportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTaxSupportActionPerformed
        recalculateTotals();
    }//GEN-LAST:event_cboTaxSupportActionPerformed

    private void txtBaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBaseActionPerformed
        recalculateValue();
    }//GEN-LAST:event_txtBaseActionPerformed

    private void txtBaseFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtBaseFocusLost
        recalculateValue();
    }//GEN-LAST:event_txtBaseFocusLost

    private void cmdAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdAddActionPerformed
        var tax = getSelectedTax();
        if (tax == null) {
            showMessage("message.withhold.selecttype");
            return;
        }

        if (linesModel.containsTax(tax.getId())) {
            showMessage("message.withhold.duplicatetype");
            return;
        }

        // El SRI agrupa las retenciones por sustento, asi que la linea tiene
        // que decir a cual pertenece. Con el combo escondido se usa el unico
        // que tiene la compra.
        var sustento = cboTaxSupport.isVisible()
                ? (modelTaxSupport == null || modelTaxSupport.getSelectedKey() == null
                        ? null : modelTaxSupport.getSelectedKey().toString())
                : fixedTaxSupport;

        if (sustento == null) {
            showMessage("message.withhold.selecttaxsupport");
            return;
        }

        var line = new WithholdLineInfo();
        line.setWithholdTax(tax);
        line.setTaxSupport(sustento);
        line.setBaseValue(parseDouble(txtBase.getText()));
        line.setWithholdedValue(parseDouble(txtValue.getText()));

        linesModel.addLine(line);
        refreshTotal();

        modelWithholdTax.setSelectedKey(null);
        txtBase.setText("");
        txtPercentage.setText("");
        txtValue.setText("");
    }//GEN-LAST:event_cmdAddActionPerformed

    private void cmdRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveActionPerformed
        var index = jTableLines.getSelectedRow();
        if (index < 0) {
            return;
        }

        linesModel.removeLine(index);
        refreshTotal();
    }//GEN-LAST:event_cmdRemoveActionPerformed

    /**
     * Arma la retencion y la devuelve; no escribe nada en la base.
     *
     * La escritura la hace savePurchase, para que la compra y su retencion
     * queden en la misma transaccion.
     */
    private void cmdSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSaveActionPerformed
        if (readOnly) {
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            var user = withhold.getUser();

            var dlTicketsNum = (DataLogicTicketsNumPurchase) app.getBean(
                    "dev.joguenco.pos.ticketsnumpurchase.DataLogicTicketsNumPurchase");

            var ticketNum = (TicketsNumInfo) dlTicketsNum
                    .getSerial()
                    .find(user.getId(), withhold.getCode(), "primary");

            if (ticketNum == null) {
                showMessage("message.withhold.noserie");
                return;
            }

            // El numero de la serie no se pide aqui: se pide al guardar, dentro
            // de la transaccion. Aqui solo se deja lo necesario para armarlo.
            withhold.setSerie(ticketNum.getSerie());
            withhold.setFormatNumberDigits(dlSystem.getResourceAsText("FormatTicket.NumberDigits"));
            withhold.setTaxPayerInfo((TaxpayerInfo) dlTaxPayer.getTaxPayerInfo().find("1"));
            withhold.setEnvironment(dlSystem.getResourceAsText("Electronic.Environment"));

            withhold.setDateWithhold(parseDate(txtDate.getText(), "dd-MM-yyyy",
                    withhold.getDateWithhold()));
            withhold.setFiscalPeriod(parseDate(txtFiscalPeriod.getText(), "MM/yyyy",
                    withhold.getFiscalPeriod()));
            withhold.setObservation(txtObservation.getText());
            withhold.setLines(linesModel.getLines());

            returnStatus = RET_OK;
            dispose();
        } catch (Exception e) {
            showError("message.cannotloaddata", e);
        }
    }//GEN-LAST:event_cmdSaveActionPerformed

    private void cmdCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdCloseActionPerformed
        returnStatus = RET_CANCEL;
        dispose();
    }//GEN-LAST:event_cmdCloseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboTaxSupport;
    private javax.swing.JComboBox cboWithholdTax;
    private javax.swing.JButton cmdAdd;
    private javax.swing.JButton cmdClose;
    private javax.swing.JButton cmdRemove;
    private javax.swing.JButton cmdSave;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableLines;
    private javax.swing.JLabel lblBaseTitle;
    private javax.swing.JLabel lblDateTitle;
    private javax.swing.JTextField txtDocument;
    private javax.swing.JLabel lblDocumentTitle;
    private javax.swing.JLabel lblFiscalPeriodTitle;
    private javax.swing.JTextField txtIva;
    private javax.swing.JLabel lblIvaTitle;
    private javax.swing.JLabel lblObservationTitle;
    private javax.swing.JLabel lblPercentageTitle;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JLabel lblSubtotalTitle;
    private javax.swing.JTextField txtSupplier;
    private javax.swing.JLabel lblSupplierTitle;
    private javax.swing.JLabel lblTaxSupportTitle;
    private javax.swing.JLabel lblTotalWithheld;
    private javax.swing.JLabel lblTotalWithheldTitle;
    private javax.swing.JLabel lblValueTitle;
    private javax.swing.JLabel lblWithholdTypeTitle;
    private javax.swing.JPanel panelBottom;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelDates;
    private javax.swing.JPanel panelPurchase;
    private javax.swing.JPanel panelTop;
    private javax.swing.JPanel panelTotal;
    private javax.swing.JPanel panelType;
    private javax.swing.JPanel panelValues;
    private javax.swing.JTextField txtBase;
    private javax.swing.JTextField txtDate;
    private javax.swing.JTextField txtFiscalPeriod;
    private javax.swing.JTextField txtObservation;
    private javax.swing.JTextField txtPercentage;
    private javax.swing.JTextField txtValue;
    // End of variables declaration//GEN-END:variables

    // -----------------------------------------------------------------------
    // Modelo de la grilla
    // -----------------------------------------------------------------------
    private class LinesTableModel extends AbstractTableModel {

        private final List<WithholdLineInfo> rows = new ArrayList<>();

        private final String[] columns = {
            AppLocal.getIntString("label.withhold.type"),
            AppLocal.getIntString("label.code"),
            AppLocal.getIntString("label.base"),
            "%",
            AppLocal.getIntString("label.value")
        };

        public List<WithholdLineInfo> getLines() {
            return rows;
        }

        public void addLine(WithholdLineInfo line) {
            rows.add(line);
            fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
        }

        public void removeLine(int index) {
            rows.remove(index);
            fireTableRowsDeleted(index, index);
        }

        public boolean containsTax(String withholdTaxesId) {
            return rows.stream()
                    .anyMatch(l -> withholdTaxesId.equals(l.getWithholdTaxesId()));
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            var line = rows.get(row);

            switch (column) {
                case 0:
                    return line.getWithholdTaxName();
                case 1:
                    return line.getWithholdTaxCode();
                case 2:
                    return Formats.CURRENCY.formatValue(line.getBaseValue());
                case 3:
                    return Formats.DOUBLE.formatValue(line.getPercentage());
                case 4:
                    return Formats.CURRENCY.formatValue(line.getWithholdedValue());
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
