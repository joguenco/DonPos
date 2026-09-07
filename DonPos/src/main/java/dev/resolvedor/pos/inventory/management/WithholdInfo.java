package dev.resolvedor.pos.inventory.management;

import com.unicenta.pos.ticket.UserInfo;
import dev.joguenco.receipt.MasterMoldInfo;
import dev.resolvedor.util.PrintFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * Cabecera del comprobante de retencion (tabla withholds).
 *
 * El proveedor no se guarda aqui: llega por purchases.supplier a traves de
 * purchaseId. Guardarlo dos veces permitiria que se contradigan.
 */
@Getter
@Setter
public class WithholdInfo extends MasterMoldInfo {

    private String id;
    private String purchaseId;
    private Date dateWithhold;
    private String observation;
    private Date fiscalPeriod;
    private Boolean status;

    // Datos de apoyo: se usan para armar el numero y la clave, no se guardan

    private UserInfo user;
    

    private List<WithholdLineInfo> lines;

    public WithholdInfo() {
        id = UUID.randomUUID().toString();
        setCode("RT");
        lines = new ArrayList<>();
        dateWithhold = new Date();
        fiscalPeriod = new Date();
        status = true;
        user = new UserInfo("", "");
    }

    /**
     * Clave de acceso de 49 digitos. Mismo armado que PurchaseInfo, cambiando
     * el codigo de documento: 07 = comprobante de retencion.
     */
    public String buildAccessKey() {
        return buildAccessKey(getDateWithhold());
    }

    public Double getTotalWithhold() {
        var total = lines.stream()
                .mapToDouble(WithholdLineInfo::getWithholdedValue)
                .sum();

        return BigDecimal.valueOf(total)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    // -----------------------------------------------------------------------
    // Impresion
    //
    // La plantilla no toca getters directos: los Date y los Double crudos se
    // imprimen feos, y un null en Velocity sale como "${withhold.getX()}".
    // -----------------------------------------------------------------------

    /**
     * Numero del comprobante con guiones: 001-001-000000002.
     */
    public String printSequential() {
        return PrintFormat.sequential(getSerieNumber());
    }

    /**
     * Fecha de emision de la retencion.
     */
    public String printDate() {
        return PrintFormat.date(dateWithhold);
    }

    /**
     * Periodo fiscal en MM/yyyy, que es como lo pide el SRI.
     */
    public String printFiscalPeriod() {
        return PrintFormat.fiscalPeriod(fiscalPeriod);
    }

    /**
     * Total retenido, con simbolo de moneda.
     */
    public String printTotalWithheld() {
        return PrintFormat.currency(getTotalWithhold());
    }

    public String printObservation() {
        return PrintFormat.text(observation);
    }

    public String printUser() {
        return user == null ? "" : PrintFormat.text(user.getName());
    }

    @Override
    public String toString() {
        return getCode() + " " + getSerie() + " - " + getTotalWithhold();
    }
}
