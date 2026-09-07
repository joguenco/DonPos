package dev.joguenco.receipt;

import dev.joguenco.pos.establishment.EstablishmentInfo;
import dev.joguenco.pos.taxpayer.TaxpayerInfo;
import dev.resolvedor.util.Module11;
import dev.resolvedor.util.PrintFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Jorge Luis
 * @web https://resolvedor.dev
 * @mail jorgeluis@resolvedor.dev
 */
@Getter
@Setter
public class MasterMoldInfo {

    private TaxpayerInfo taxPayerInfo;
    private String accessKey;
    private String code;
    private String serie;
    private String serieNumber;
    private String environment; // Test -> 1; Production -> 2
    private String formatNumberDigits;
    private EstablishmentInfo establishment;

    public String buildAccessKey(Date date) {
        String codeDocument;
        final var m11 = new Module11();
        try {
            if (null == getCode()) {
                accessKey = "";
                return accessKey;
            } else {
                switch (getCode()) {
                    case "FV":
                        codeDocument = "01"; // Invoice
                        break;
                    case "LQ":
                        codeDocument = "03"; // Purchase Liquidation
                        break;
                    case "DV":
                        codeDocument = "04"; // Credit Note
                        break;
                    case "ND":
                        codeDocument = "05"; // Debit Note
                        break;
                    case "GUI":
                        codeDocument = "06"; // Delivery note
                        break;
                    case "RT":
                        codeDocument = "07"; // Withholding receipt
                        break;
                    default:
                        accessKey = "";
                        return accessKey;
                }
            }

            accessKey = new SimpleDateFormat("ddMMyyyy").format(date);
            accessKey = accessKey + codeDocument;
            accessKey = accessKey + getTaxPayerInfo().getIdentification();
            accessKey = accessKey + getEnvironment();
            accessKey = accessKey + getSerieNumber().replace("-", "");
            accessKey = accessKey + "12345678" + "1";
            accessKey = accessKey + m11.module11(accessKey);

            return accessKey;
        } catch (Exception e) {
            accessKey = "";
            return accessKey;
        }
    }
    
    public String printComercialName() {
        return establishment == null ? "" : PrintFormat.text(establishment.getComercialName());
    }

    public String printEstablishmentAddress() {
        return establishment == null ? "" : PrintFormat.text(establishment.getAddress());
    }

    public String printEstablishmentPhone() {
        return establishment == null ? "" : PrintFormat.text(establishment.getPhone());
    }

    public String printEstablishmentEmail() {
        return establishment == null ? "" : PrintFormat.text(establishment.getEmail());
    }   
    
    public String printEnvironment() {
        return PrintFormat.environment(getEnvironment());
    }

    // --- Emisor ------------------------------------------------------------
    // Los datos del emisor son los mismos en todos los documentos: quien firma
    // la factura es quien firma la liquidacion, la retencion y la guia. Por eso
    // viven aqui y no repetidos en cada clase.

    public String printLegalName() {
        return taxPayerInfo == null ? "" : taxPayerInfo.printLegalName();
    }

    public String printIdentification() {
        return taxPayerInfo == null ? "" : taxPayerInfo.printIdentification();
    }

    public String printForcedAccounting() {
        return taxPayerInfo == null ? "" : taxPayerInfo.printForcedAccounting();
    }

    public String printSpecialTaxpayer() {
        return taxPayerInfo == null ? "" : taxPayerInfo.printSpecialTaxpayer();
    }

    public String printRetentionAgent() {
        return taxPayerInfo == null ? "" : taxPayerInfo.printRetentionAgent();
    }

    public String printOther() {
        return taxPayerInfo == null ? "" : taxPayerInfo.printOther();
    }

    // --- Clave de acceso ---------------------------------------------------
    // Los 49 digitos no entran en un rollo de 40 columnas, asi que se parten en
    // dos lineas. Si la clave no esta completa las dos salen vacias y la
    // plantilla se salta el bloque entero.

    public String printAccessKeyLine1() {
        return PrintFormat.accessKeyLine1(getAccessKey());
    }

    public String printAccessKeyLine2() {
        return PrintFormat.accessKeyLine2(getAccessKey());
    }
}
