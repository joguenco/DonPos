package dev.joguenco.pos.dispatcher;

import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.RIGHT;
import com.unicenta.basic.BasicException;
import com.unicenta.data.gui.ComboBoxValModel;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.forms.AppView;
import com.unicenta.pos.forms.BeanFactoryApp;
import com.unicenta.pos.forms.BeanFactoryException;
import com.unicenta.pos.forms.DataLogicSystem;
import com.unicenta.pos.forms.JPanelView;
import com.unicenta.pos.printer.TicketParser;
import com.unicenta.pos.scripting.ScriptEngine;
import com.unicenta.pos.scripting.ScriptFactory;
import dev.joguenco.http.client.authorization.ExecuteAuthorization;
import dev.joguenco.pos.establishment.DataLogicEstablishment;
import dev.joguenco.pos.establishment.EstablishmentInfo;
import dev.joguenco.pos.service.DataLogicService;
import dev.joguenco.pos.taxpayer.DataLogicTaxpayer;
import dev.joguenco.pos.taxpayer.TaxpayerInfo;
import dev.joguenco.pos.ticketsnum.DataLogicTicketsNum;
import dev.joguenco.pos.ticketsnum.TicketsNumInfo;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import lombok.extern.slf4j.Slf4j;

/**
 * Pantalla para armar una guia de remision.
 *
 * Sigue el molde de PurchaseEditor: cabecera arriba, grilla de lineas abajo y
 * botonera al pie. Las facturas se eligen con un dialogo aparte, que solo
 * muestra las del dia y bloquea las ya despachadas.
 */
@Slf4j
public class DispatchEditor extends JPanel implements JPanelView, BeanFactoryApp {

    private AppView app;

    private DataLogicDispatch dlDispatch;
    private DataLogicTaxpayer dlTaxPayer;
    private DataLogicEstablishment dlEstablishment;
    private DataLogicSystem dlSystem;
    private DataLogicService dlServices;

    private ComboBoxValModel modelDispatcher;
    private ComboBoxValModel modelAddressStart;

    /**
     * Interpreta la plantilla ya resuelta y la manda a la impresora.
     */
    private TicketParser ticketParser;

    private DispatchInfo dispatch = new DispatchInfo();
    private final LinesTableModel linesModel = new LinesTableModel();

    public DispatchEditor() {
        initComponents();
        initBehaviour();
    }

    /**
     * Lo que el disenador no genera: modelo de la grilla y listeners.
     *
     * Va aparte de initComponents() porque NetBeans reescribe ese metodo cada
     * vez que se toca el .form y se llevaria estas lineas.
     */
    private void initBehaviour() {
        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelHeader.setBorder(javax.swing.BorderFactory.createTitledBorder(
                AppLocal.getIntString("Menu.Dispatch")));

        jTableLines.setModel(linesModel);
        jTableLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTableLines.setRowHeight(26);

        DispatchTables.alinear(jTableLines, LEFT, LEFT, LEFT,
                LEFT, LEFT, RIGHT);

        jTableLines.getColumnModel().getColumn(0).setPreferredWidth(90);
        jTableLines.getColumnModel().getColumn(1).setPreferredWidth(170);
        jTableLines.getColumnModel().getColumn(2).setPreferredWidth(200);
        jTableLines.getColumnModel().getColumn(3).setPreferredWidth(120);
        jTableLines.getColumnModel().getColumn(4).setPreferredWidth(220);
        jTableLines.getColumnModel().getColumn(5).setPreferredWidth(90);

        cmdAddInvoice.addActionListener(evt -> cmdAddInvoiceActionPerformed());
        cmdSearch.addActionListener(evt -> cmdSearchActionPerformed());
        cmdRemove.addActionListener(evt -> cmdRemoveActionPerformed());
        cmdSave.addActionListener(evt -> cmdSaveActionPerformed());
        cmdNew.addActionListener(evt -> stateToInsert());
    }

    // -----------------------------------------------------------------------
    // JPanelView
    // -----------------------------------------------------------------------
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Dispatch");
    }

    @Override
    public void activate() throws BasicException {
        loadDispatchers();
        loadAddressStart();
        stateToInsert();
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

        dlDispatch = (DataLogicDispatch) app.getBean("dev.joguenco.pos.dispatcher.DataLogicDispatch");
        dlTaxPayer = (DataLogicTaxpayer) app.getBean("dev.joguenco.pos.taxpayer.DataLogicTaxpayer");
        dlEstablishment = (DataLogicEstablishment) app.getBean("dev.joguenco.pos.establishment.DataLogicEstablishment");
        dlSystem = (DataLogicSystem) app.getBean("com.unicenta.pos.forms.DataLogicSystem");
        dlServices = (DataLogicService) app.getBean("dev.joguenco.pos.service.DataLogicService");

        ticketParser = new TicketParser(app.getDeviceTicket(), dlSystem);
    }

    @Override
    public Object getBean() {
        return this;
    }

    // -----------------------------------------------------------------------
    // Interfaz
    // -----------------------------------------------------------------------
    // -----------------------------------------------------------------------
    // Carga
    // -----------------------------------------------------------------------
    private void loadDispatchers() {
        try {
            modelDispatcher = new ComboBoxValModel(dlDispatch.getDispatcherList().list());
            cboDispatcher.setModel(modelDispatcher);
            modelDispatcher.setSelectedKey(null);
        } catch (Exception e) {
            log.error(DispatchEditor.class.getName() + " " + e.getMessage());
        }
    }

    private void loadAddressStart() {
        try {
            modelAddressStart = new ComboBoxValModel(dlDispatch.getAddressStartList().list());
            cboAddressStart.setModel(modelAddressStart);
            modelAddressStart.setSelectedKey(null);
        } catch (Exception e) {
            log.error(DispatchEditor.class.getName() + " " + e.getMessage());
        }
    }

    /**
     * Guia nueva: pantalla vacia y lista para escribir.
     *
     * Es el estado en que abre la pantalla, porque venir aqui a crear una guia
     * es lo normal; consultar el historial es la excepcion.
     */
    private void stateToInsert() {
        limpiar();
        setReadOnly(false);
        refreshCount();
    }

    private void limpiar() {
        dispatch = new DispatchInfo();
        dispatch.setUser(app.getAppUserView().getUser().getUserInfo());

        if (modelDispatcher != null) {
            modelDispatcher.setSelectedKey(null);
        }
        if (modelAddressStart != null) {
            modelAddressStart.setSelectedKey(null);
        }

        var hoy = new Date();
        txtDateDispatch.setText(Formats.SIMPLEDATE.formatValue(hoy));
        txtDateEndDispatch.setText(Formats.SIMPLEDATE.formatValue(hoy));
        txtObservation.setText(null);

        // Se propone el motivo de siempre, listo para cambiar si hace falta
        txtTransferReason.setText(DispatchInfo.TRANSFER_REASON);

        linesModel.clear();
    }

    /**
     * Carga una guia ya emitida. Queda en solo lectura: el numero y la clave de
     * acceso ya se consumieron, y si el documento se envio al SRI cambiarlo
     * aqui dejaria la base diciendo una cosa y el SRI otra.
     */
    private void stateToView(DispatchInfo guia) {
        try {
            dispatch = guia;

            modelDispatcher.setSelectedKey(guia.getDispatcherId());
            modelAddressStart.setSelectedKey(guia.getAddressStart());
            txtDateDispatch.setText(Formats.SIMPLEDATE.formatValue(guia.getDateDispatch()));
            txtDateEndDispatch.setText(Formats.SIMPLEDATE.formatValue(guia.getDateEndDispatch()));
            txtObservation.setText(guia.getObservation());
            txtTransferReason.setText(guia.getTransferReason());

            linesModel.setLines(dlDispatch.getLinesByDispatch(guia.getId()));

            setReadOnly(true);
            mostrarResumen(guia.getSerieNumber());
        } catch (Exception e) {
            log.error(DispatchEditor.class.getName() + " " + e.getMessage());
            showError("message.cannotloaddata", e);
        }
    }

    /**
     * Apaga o enciende todo lo que se puede tocar.
     *
     * Nueva Guia se esconde mientras se esta escribiendo: ahi no tiene nada que
     * hacer y solo confunde con Guardar. Aparece cuando la pantalla queda en
     * consulta, que es cuando de verdad sirve, como salida del modo lectura.
     *
     * Buscar Guias nunca se apaga: se puede consultar el historial en cualquier
     * momento.
     */
    private void setReadOnly(boolean readOnly) {
        cboDispatcher.setEnabled(!readOnly);
        cboAddressStart.setEnabled(!readOnly);
        txtDateDispatch.setEditable(!readOnly);
        txtDateEndDispatch.setEditable(!readOnly);
        txtObservation.setEditable(!readOnly);
        txtTransferReason.setEditable(!readOnly);
        cmdAddInvoice.setEnabled(!readOnly);
        cmdRemove.setEnabled(!readOnly);
        cmdSave.setEnabled(!readOnly);

        cmdNew.setVisible(readOnly);
    }

    // -----------------------------------------------------------------------
    // Eventos
    // -----------------------------------------------------------------------
    private void cmdAddInvoiceActionPerformed() {
        var yaAgregadas = linesModel.getLines().stream()
                .map(DispatchLineInfo::getReferenceNumber)
                .collect(Collectors.toList());

        var dialog = new DispatchInvoiceDialog(app, new javax.swing.JFrame(), true, yaAgregadas);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        if (dialog.getReturnStatus() == DispatchInvoiceDialog.RET_OK) {
            // Se reemplaza, no se acumula: el dialogo devuelve la guia
            // completa, porque ahi tambien se pueden desmarcar facturas.
            linesModel.setLines(dialog.getSelectedInvoices());
            refreshCount();
        }
    }

    private void cmdSearchActionPerformed() {
        var dialog = new DispatchSearchDialog(app, new javax.swing.JFrame(), true);
        dialog.setVisible(true);

        if (dialog.getReturnStatus() == DispatchSearchDialog.RET_OK) {
            stateToView(dialog.getSelectedDispatch());
        }
    }

    private void cmdRemoveActionPerformed() {
        var index = jTableLines.getSelectedRow();
        if (index < 0) {
            return;
        }

        linesModel.removeLine(index);
        refreshCount();
    }

    private void cmdSaveActionPerformed() {
        if (!validateForm()) {
            return;
        }

        try {
            var user = dispatch.getUser();

            var dlTicketsNum = (DataLogicTicketsNum) app.getBean(
                    "dev.joguenco.pos.ticketsnum.DataLogicTicketsNum");

            var ticketNum = (TicketsNumInfo) dlTicketsNum
                    .getSerial()
                    .find(user.getId(), dispatch.getCode(), "primary");

            if (ticketNum == null) {
                showMessage("message.dispatch.noserie");
                return;
            }

            dispatch.setSerie(ticketNum.getSerie());
            dispatch.setFormatNumberDigits(dlSystem.getResourceAsText("FormatTicket.NumberDigits"));
            dispatch.setTaxPayerInfo((TaxpayerInfo) dlTaxPayer.getTaxPayerInfo().find("1"));
            dispatch.setEnvironment(dlSystem.getResourceAsText("Electronic.Environment"));

            // Los datos del local van en la cabecera impresa, igual que en la
            // factura: los tres primeros digitos de la serie son el codigo del
            // establecimiento que emite.
            dispatch.setEstablishment(findEstablishment(dispatch.getSerie()));

            dispatch.setDispatcherId((String) modelDispatcher.getSelectedKey());

            // Los datos del transportista los tiene el combo: DispatchInfo
            // guarda solo el id, y en el papel un UUID no sirve de nada. Van
            // por separado porque el SRI pide identificacion y placa como
            // campos propios, no como una sola etiqueta.
            var transportista = (DataLogicDispatch.DispatcherComboInfo) modelDispatcher.getSelectedItem();

            if (transportista != null) {
                dispatch.setDispatcherLabel(transportista.toString());
                dispatch.setDispatcherName(transportista.getName());
                dispatch.setDispatcherTaxId(transportista.getTaxid());
                dispatch.setDispatcherPlate(transportista.getPlate());
            }
            dispatch.setAddressStart((String) modelAddressStart.getSelectedKey());
            dispatch.setDateDispatch(parseDate(txtDateDispatch.getText()));

            // El SRI exige fechaFinTransporte, asi que no puede quedar vacia.
            // Si no se entiende lo escrito se usa la de salida, que es el caso
            // normal: sale y llega el mismo dia.
            var llegada = parseDate(txtDateEndDispatch.getText());
            dispatch.setDateEndDispatch(
                    llegada == null ? dispatch.getDateDispatch() : llegada);
            dispatch.setObservation(txtObservation.getText());
            dispatch.setTransferReason(txtTransferReason.getText());
            dispatch.setLines(linesModel.getLines());

            var serieNumber = dlDispatch.saveDispatch(dispatch);

            printDeliveryNote();

            // Igual que la factura y la liquidacion: guardada la guia, se manda a
            // autorizar en un hilo aparte. Uso el numero que devolvio saveDispatch,
            // que es el que quedo grabado de verdad.
            log.info("Start authorization in a thread "
                    + dispatch.getCode() + " " + serieNumber);
            new ExecuteAuthorization(dlServices.getServiceInfoByName("Authorize"),
                    dispatch.getCode(), serieNumber).start();

            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.dispatch.saved") + "\n"
                    + AppLocal.getIntString("Menu.Dispatch") + ": " + serieNumber,
                    AppLocal.getIntString("Menu.Dispatch"),
                    JOptionPane.INFORMATION_MESSAGE);

            // Se queda mostrando lo que acaba de guardar, en solo lectura.
            // Para hacer otra hay que pedirla con Agregar, igual que al entrar.
            setReadOnly(true);
            mostrarResumen(serieNumber);
        } catch (Exception e) {
            log.error(DispatchEditor.class.getName() + " " + e.getMessage());

            // Dos cajas despachando la misma factura al mismo tiempo: la que
            // llega segunda choca contra uk_dispatches_reference. La
            // transaccion ya deshizo su parte, aqui solo hay que explicarlo.
            if (esFacturaRepetida(e)) {
                showMessage("message.dispatch.alreadydispatched");
                return;
            }

            showError("message.cannotsavedata", e);
        }
    }

    /**
     * Imprime la guia de remision.
     *
     * Acompana a la mercaderia durante el traslado, asi que se imprime apenas
     * se guarda y viaja en el camion.
     *
     * Los errores se avisan pero no cortan nada: la guia ya quedo grabada y no
     * poder imprimirla no la invalida.
     */
    private void printDeliveryNote() {
        var plantilla = dlSystem.getResourceAsXML("Printer.Note.Delivery");

        if (plantilla == null) {
            showMessage("message.cannotprintticket");
            return;
        }

        try {
            loadProducts();

            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);

            // Cada nombre de aqui es el que la plantilla usa con $. Todo lo
            // demas (emisor, local, transportista) cuelga de dispatch, para que
            // la plantilla nunca tenga que preguntar por un objeto que puede
            // venir nulo.
            script.put("dispatch", dispatch);
            script.put("lines", dispatch.getLines());

            ticketParser.printTicket(script.eval(plantilla).toString());
        } catch (Exception e) {
            log.error(DispatchEditor.class.getName() + " " + e.getMessage());
            showMessage("message.cannotprintticket");
        }
    }

    /**
     * Llena cada factura de la guia con los productos que van adentro.
     *
     * Se hace recien al imprimir, no al armar la guia: la pantalla solo
     * necesita saber que facturas van, y traer los productos de todas mientras
     * el usuario arma el viaje seria cargar datos que quizas descarte.
     *
     * Si una factura falla, esa se queda sin detalle pero la guia igual sale.
     * Un producto que no se pudo leer no invalida el traslado.
     */
    private void loadProducts() {
        for (var linea : dispatch.getLines()) {
            try {
                linea.setProducts(dlDispatch.getProductsOfInvoice(
                        linea.getReferenceCode(), linea.getReferenceNumber()));
            } catch (Exception e) {
                log.error(DispatchEditor.class.getName() + " " + e.getMessage());
            }
        }
    }

    /**
     * El establecimiento al que pertenece una serie.
     *
     * Los tres primeros digitos de la serie son su codigo, y con eso se traen
     * nombre comercial, direccion, telefono y correo del local que emite. Es el
     * mismo camino que usa la factura.
     *
     * Devuelve null si no se puede resolver: la impresion se lo salta y la guia
     * sale sin esa cabecera, pero sale.
     */
    private EstablishmentInfo findEstablishment(String serie) {
        if (serie == null || serie.length() < 3) {
            return null;
        }

        try {
            return (EstablishmentInfo) dlEstablishment
                    .getEstablishmentInfo()
                    .find(serie.substring(0, 3));
        } catch (Exception e) {
            log.error(DispatchEditor.class.getName() + " " + e.getMessage());
            return null;
        }
    }

    /**
     * Reconoce el choque del indice unico entre el monton de errores SQL.
     */
    private boolean esFacturaRepetida(Throwable e) {
        for (Throwable causa = e; causa != null; causa = causa.getCause()) {
            var texto = causa.getMessage();
            if (texto != null
                    && (texto.contains("uk_dispatches_reference")
                    || texto.contains("Duplicate entry"))) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Apoyo
    // -----------------------------------------------------------------------
    private boolean validateForm() {
        if (modelDispatcher == null || modelDispatcher.getSelectedKey() == null) {
            showMessage("message.dispatch.nodispatcher");
            return false;
        }

        if (modelAddressStart == null || modelAddressStart.getSelectedKey() == null) {
            showMessage("message.dispatch.noaddressstart");
            return false;
        }

        var salida = parseDate(txtDateDispatch.getText());

        if (salida == null) {
            showMessage("message.dispatch.baddate");
            return false;
        }

        // La guia se emite el mismo dia del despacho. Una fecha pasada dejaria
        // el documento diciendo que la mercaderia salio antes de que la guia
        // existiera, y el SRI la rechaza por fecha de emision.
        if (esAnteriorAHoy(salida)) {
            showMessage("message.dispatch.pastdate");
            return false;
        }

        if (txtTransferReason.getText() == null
                || txtTransferReason.getText().trim().isEmpty()) {
            showMessage("message.dispatch.notransferreason");
            return false;
        }

        if (linesModel.getRowCount() == 0) {
            showMessage("message.dispatch.noinvoices");
            return false;
        }

        return true;
    }

    /**
     * Compara solo el dia: la hora no importa y estorba al comparar.
     */
    private boolean esAnteriorAHoy(Date fecha) {
        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .isBefore(LocalDate.now());
    }

    private void refreshCount() {
        lblCount.setText(AppLocal.getIntString("label.dispatch.invoices")
                + ": " + linesModel.getRowCount());
    }

    /**
     * Pie de una guia ya guardada: numero y cuantas facturas lleva.
     */
    private void mostrarResumen(String serieNumber) {
        lblCount.setText(AppLocal.getIntString("Menu.Dispatch") + ": " + serieNumber
                + "    " + AppLocal.getIntString("label.dispatch.invoices") + ": "
                + linesModel.getRowCount());
    }

    /**
     * La fecha escrita, o null si no se entiende.
     *
     * Antes devolvia hoy cuando fallaba, y eso escondia los errores de tipeo:
     * el usuario escribia mal y se guardaba otra fecha sin avisarle.
     */
    private Date parseDate(String text) {
        try {
            return (Date) Formats.SIMPLEDATE.parseValue(text.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void showError(String key, Exception e) {
        JOptionPane.showMessageDialog(this,
                AppLocal.getIntString(key) + "\n" + e.getMessage(),
                AppLocal.getIntString("Menu.Dispatch"),
                JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String key) {
        JOptionPane.showMessageDialog(this,
                AppLocal.getIntString(key),
                AppLocal.getIntString("Menu.Dispatch"),
                JOptionPane.WARNING_MESSAGE);
    }

    // -----------------------------------------------------------------------
    // Modelo de la grilla
    // -----------------------------------------------------------------------
    private class LinesTableModel extends AbstractTableModel {

        private final List<DispatchLineInfo> rows = new ArrayList<>();

        private final String[] columns = {
            AppLocal.getIntString("label.date"),
            AppLocal.getIntString("label.Number"),
            AppLocal.getIntString("label.customer"),
            AppLocal.getIntString("label.suppliertaxid"),
            AppLocal.getIntString("label.address"),
            AppLocal.getIntString("label.totalcash")
        };

        public List<DispatchLineInfo> getLines() {
            return rows;
        }

        /**
         * Reemplaza el contenido con lo que trae el dialogo de facturas.
         */
        public void setLines(List<DispatchLineInfo> lineas) {
            rows.clear();
            rows.addAll(lineas);
            fireTableDataChanged();
        }

        public void removeLine(int index) {
            rows.remove(index);
            fireTableRowsDeleted(index, index);
        }

        public void clear() {
            var total = rows.size();
            if (total > 0) {
                rows.clear();
                fireTableRowsDeleted(0, total - 1);
            }
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

        /**
         * Nada se escribe a mano: las lineas entran y salen con los botones.
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int column) {
            var line = rows.get(row);

            switch (column) {
                case 0:
                    return line.getDate() == null ? ""
                            : Formats.SIMPLEDATE.formatValue(line.getDate());
                case 1:
                    return line.getReferenceCode() + " " + line.getReferenceNumber();
                case 2:
                    return line.getCustomerName();
                case 3:
                    return line.getCustomerTaxId();
                case 4:
                    return line.getCustomerAddress();
                case 5:
                    return Formats.CURRENCY.formatValue(line.getTotal());
                default:
                    return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        panelDispatcher = new javax.swing.JPanel();
        lblDispatcher = new javax.swing.JLabel();
        cboDispatcher = new javax.swing.JComboBox();
        panelAddressStart = new javax.swing.JPanel();
        lblAddressStart = new javax.swing.JLabel();
        cboAddressStart = new javax.swing.JComboBox();
        panelDates = new javax.swing.JPanel();
        lblDateDispatch = new javax.swing.JLabel();
        txtDateDispatch = new javax.swing.JTextField();
        lblDateEndDispatch = new javax.swing.JLabel();
        txtDateEndDispatch = new javax.swing.JTextField();
        panelReason = new javax.swing.JPanel();
        lblTransferReason = new javax.swing.JLabel();
        txtTransferReason = new javax.swing.JTextField();
        panelObservation = new javax.swing.JPanel();
        lblObservation = new javax.swing.JLabel();
        txtObservation = new javax.swing.JTextField();
        panelLines = new javax.swing.JPanel();
        panelLineButtons = new javax.swing.JPanel();
        cmdAddInvoice = new javax.swing.JButton();
        cmdRemove = new javax.swing.JButton();
        cmdSearch = new javax.swing.JButton();
        cmdNew = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableLines = new javax.swing.JTable();
        panelFooter = new javax.swing.JPanel();
        panelCount = new javax.swing.JPanel();
        lblCount = new javax.swing.JLabel();
        panelButtons = new javax.swing.JPanel();
        cmdSave = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        panelHeader.setLayout(new java.awt.GridLayout(5, 1));

        panelDispatcher.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        lblDispatcher.setText(AppLocal.getIntString("Menu.Dispatcher")); // NOI18N
        lblDispatcher.setPreferredSize(new java.awt.Dimension(170, 38));
        panelDispatcher.add(lblDispatcher);

        cboDispatcher.setPreferredSize(new java.awt.Dimension(320, 38));
        panelDispatcher.add(cboDispatcher);

        panelHeader.add(panelDispatcher);

        panelAddressStart.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        lblAddressStart.setText(AppLocal.getIntString("label.dispatch.addressstart")); // NOI18N
        lblAddressStart.setPreferredSize(new java.awt.Dimension(170, 38));
        panelAddressStart.add(lblAddressStart);

        cboAddressStart.setPreferredSize(new java.awt.Dimension(320, 38));
        panelAddressStart.add(cboAddressStart);

        panelHeader.add(panelAddressStart);

        panelDates.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        lblDateDispatch.setText(AppLocal.getIntString("label.dispatch.datestart")); // NOI18N
        lblDateDispatch.setPreferredSize(new java.awt.Dimension(170, 38));
        panelDates.add(lblDateDispatch);

        txtDateDispatch.setPreferredSize(new java.awt.Dimension(135, 38));
        panelDates.add(txtDateDispatch);

        lblDateEndDispatch.setText(AppLocal.getIntString("label.dispatch.dateend")); // NOI18N
        lblDateEndDispatch.setPreferredSize(new java.awt.Dimension(135, 30));
        panelDates.add(lblDateEndDispatch);

        txtDateEndDispatch.setPreferredSize(new java.awt.Dimension(135, 38));
        panelDates.add(txtDateEndDispatch);

        panelHeader.add(panelDates);

        panelReason.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        lblTransferReason.setText(AppLocal.getIntString("label.dispatch.transferreason")); // NOI18N
        lblTransferReason.setPreferredSize(new java.awt.Dimension(170, 38));
        panelReason.add(lblTransferReason);

        txtTransferReason.setPreferredSize(new java.awt.Dimension(320, 38));
        panelReason.add(txtTransferReason);

        panelHeader.add(panelReason);

        panelObservation.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        lblObservation.setText(AppLocal.getIntString("label.observation")); // NOI18N
        lblObservation.setPreferredSize(new java.awt.Dimension(170, 38));
        panelObservation.add(lblObservation);

        txtObservation.setPreferredSize(new java.awt.Dimension(320, 38));
        panelObservation.add(txtObservation);

        panelHeader.add(panelObservation);

        add(panelHeader, java.awt.BorderLayout.NORTH);

        panelLines.setLayout(new java.awt.BorderLayout());

        panelLineButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        cmdAddInvoice.setText(AppLocal.getIntString("label.dispatch.addinvoice")); // NOI18N
        panelLineButtons.add(cmdAddInvoice);

        cmdRemove.setText(AppLocal.getIntString("Button.Remove")); // NOI18N
        panelLineButtons.add(cmdRemove);

        cmdSearch.setText(AppLocal.getIntString("label.dispatch.search")); // NOI18N
        panelLineButtons.add(cmdSearch);

        cmdNew.setText(AppLocal.getIntString("label.dispatch.new")); // NOI18N
        panelLineButtons.add(cmdNew);

        panelLines.add(panelLineButtons, java.awt.BorderLayout.NORTH);

        jScrollPane1.setViewportView(jTableLines);

        panelLines.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(panelLines, java.awt.BorderLayout.CENTER);

        panelFooter.setLayout(new java.awt.BorderLayout());

        panelCount.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        lblCount.setText(" ");
        panelCount.add(lblCount);

        panelFooter.add(panelCount, java.awt.BorderLayout.WEST);

        panelButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 4));

        cmdSave.setText(AppLocal.getIntString("Button.Save")); // NOI18N
        panelButtons.add(cmdSave);

        panelFooter.add(panelButtons, java.awt.BorderLayout.EAST);

        add(panelFooter, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboAddressStart;
    private javax.swing.JComboBox cboDispatcher;
    private javax.swing.JButton cmdAddInvoice;
    private javax.swing.JButton cmdNew;
    private javax.swing.JButton cmdRemove;
    private javax.swing.JButton cmdSave;
    private javax.swing.JButton cmdSearch;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableLines;
    private javax.swing.JLabel lblAddressStart;
    private javax.swing.JLabel lblCount;
    private javax.swing.JLabel lblDateDispatch;
    private javax.swing.JLabel lblDateEndDispatch;
    private javax.swing.JLabel lblDispatcher;
    private javax.swing.JLabel lblObservation;
    private javax.swing.JLabel lblTransferReason;
    private javax.swing.JPanel panelAddressStart;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelCount;
    private javax.swing.JPanel panelDates;
    private javax.swing.JPanel panelDispatcher;
    private javax.swing.JPanel panelFooter;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelLineButtons;
    private javax.swing.JPanel panelLines;
    private javax.swing.JPanel panelObservation;
    private javax.swing.JPanel panelReason;
    private javax.swing.JTextField txtDateDispatch;
    private javax.swing.JTextField txtDateEndDispatch;
    private javax.swing.JTextField txtObservation;
    private javax.swing.JTextField txtTransferReason;
    // End of variables declaration//GEN-END:variables
}
