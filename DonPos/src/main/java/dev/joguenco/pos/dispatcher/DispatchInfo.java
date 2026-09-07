package dev.joguenco.pos.dispatcher;

import com.unicenta.pos.ticket.UserInfo;
import dev.joguenco.receipt.MasterMoldInfo;
import dev.resolvedor.util.PrintFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Cabecera de la guia de remision (tabla dispatches).
 *
 * Documenta un traslado: quien transporta, desde donde, entre que fechas, y que
 * facturas van en ese viaje.
 */
@Getter
@Setter
public class DispatchInfo extends MasterMoldInfo {

    /**
     * Motivo del traslado que se propone al abrir la pantalla.
     *
     * Esta pantalla despacha facturas de venta, asi que casi siempre es este.
     * No se quema: el SRI lo pide como texto libre en motivoTraslado y el
     * usuario lo puede cambiar si el traslado es por otra cosa.
     */
    public static final String TRANSFER_REASON = "Ventas de Productos";

    private String id;
    private String dispatcherId;
    private Date dateDispatch;
    private Date dateEndDispatch;
    private String addressStart;
    private String observation;
    private String transferReason;
    private Boolean status;

    // Datos de apoyo: sirven para armar el numero y la clave, no se guardan
    private UserInfo user;
    private String dispatcherLabel; // nombre y placa juntos, para la busqueda

    // El SRI los pide por separado en la guia, no como una sola etiqueta
    private String dispatcherName;
    private String dispatcherTaxId;
    private String dispatcherPlate;
    private Integer lineCount;      // cuantas facturas trae, para la busqueda

    private List<DispatchLineInfo> lines;

    public DispatchInfo() {
        id = UUID.randomUUID().toString();
        setCode("GUI");
        lines = new ArrayList<>();
        dateDispatch = new Date();
        dateEndDispatch = new Date();
        status = true;
        user = new UserInfo("", "");
    }

    /**
     * Clave de acceso de 49 digitos. Mismo armado que PurchaseInfo y
     * WithholdInfo, cambiando el codigo de documento: 06 = guia de remision.
     */
    public String buildAccessKey() {
        return buildAccessKey(getDateDispatch());
    }

    // -----------------------------------------------------------------------
    // Impresion
    //
    // La guia viaja en el camion, asi que lo que sale mal impreso aqui es lo
    // que el control de carretera va a leer. Nada de Date ni null crudos.
    // -----------------------------------------------------------------------
    /**
     * Numero de la guia con guiones: 001-001-000000002.
     */
    public String printSequential() {
        return PrintFormat.sequential(getSerieNumber());
    }

    /**
     * Fecha de inicio del traslado.
     */
    public String printDateDispatch() {
        return PrintFormat.date(dateDispatch);
    }

    /**
     * Fecha de fin del traslado. El SRI la exige, nunca va vacia.
     */
    public String printDateEndDispatch() {
        return PrintFormat.date(dateEndDispatch);
    }

    /**
     * Razon social o nombre del transportista.
     */
    public String printDispatcher() {
        return PrintFormat.text(dispatcherName).isEmpty()
                ? PrintFormat.text(dispatcherLabel)
                : PrintFormat.text(dispatcherName);
    }

    /**
     * Identificacion del transportista, que el SRI exige en la guia.
     */
    public String printDispatcherTaxId() {
        return PrintFormat.text(dispatcherTaxId);
    }

    /**
     * Placa del vehiculo.
     */
    public String printDispatcherPlate() {
        return PrintFormat.text(dispatcherPlate);
    }

    public String printAddressStart() {
        return PrintFormat.text(addressStart);
    }

    public String printTransferReason() {
        return PrintFormat.text(transferReason);
    }

    public String printObservation() {
        return PrintFormat.text(observation);
    }

    public String printUser() {
        return user == null ? "" : PrintFormat.text(user.getName());
    }

    /**
     * Cuantas facturas van en este viaje.
     */
    public String printLinesCount() {
        return Integer.toString(lines == null ? 0 : lines.size());
    }

    @Override
    public String toString() {
        return getCode() + " " + getSerieNumber() + " - " + lines.size();
    }
}
