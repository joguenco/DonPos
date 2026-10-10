package dev.resolvedor.pos.inventory.management;

import com.github.f4b6a3.uuid.UuidCreator;
import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataParams;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.Datas;
import com.unicenta.data.loader.PreparedSentence;
import com.unicenta.data.loader.QBFBuilder;
import com.unicenta.data.loader.SentenceExec;
import com.unicenta.data.loader.SentenceList;
import com.unicenta.data.loader.SerializerWriteBasic;
import com.unicenta.data.loader.SerializerWriteBasicExt;
import com.unicenta.data.loader.SerializerWriteParams;
import com.unicenta.data.loader.SerializerWriteString;
import com.unicenta.data.loader.Session;
import com.unicenta.data.loader.StaticSentence;
import com.unicenta.data.loader.TableDefinition;
import com.unicenta.data.loader.Transaction;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.BeanFactoryDataSingle;
import com.unicenta.pos.forms.DataLogicSales;
import com.unicenta.pos.inventory.InventoryLine;
import com.unicenta.pos.inventory.InventoryRecord;
import com.unicenta.pos.inventory.MovementReason;
import com.unicenta.pos.sales.TaxesLogic;
import com.unicenta.pos.ticket.ProductInfoExt;
import com.unicenta.pos.ticket.TaxInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Jorge Luis
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
public class DataLogicPurchase extends BeanFactoryDataSingle {

    private Session s;
    private TableDefinition tdPurchases;

    protected Datas[] stockdiaryDatas;

    @Override
    public void init(Session s) {
        this.s = s;

        tdPurchases = new TableDefinition(s,
                "purchases",
                new String[]{"id", "number", "created_at", "reason", "supplier", "purchase_tax_support",
                    "purchase_document", "purchase_reference", "purchase_date", "purchase_authorization",
                    "observation", "location", "status"},
                new Datas[]{Datas.STRING, Datas.INT, Datas.TIMESTAMP, Datas.INT, Datas.STRING, Datas.STRING,
                    Datas.STRING, Datas.STRING, Datas.TIMESTAMP, Datas.STRING,
                    Datas.STRING, Datas.STRING, Datas.BOOLEAN},
                new Formats[]{Formats.STRING, Formats.INT, Formats.TIMESTAMP, Formats.INT, Formats.STRING, Formats.STRING,
                    Formats.STRING, Formats.STRING, Formats.TIMESTAMP, Formats.STRING,
                    Formats.STRING, Formats.STRING, Formats.BOOLEAN},
                new int[]{0});

        stockdiaryDatas = new Datas[]{
            Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING,
            Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE,
            Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING};
    }

    public final TableDefinition getTablePurchase() {
        return tdPurchases;
    }

    public final PreparedSentence getPurchaseInfo() {
        return new PreparedSentence(s,
                "select id, number, created_at, reason, supplier, purchase_tax_support, "
                + "purchase_document, purchase_reference, purchase_date, purchase_authorization, "
                + "location, observation, status "
                + "from purchases "
                + "where id = ?",
                SerializerWriteString.INSTANCE,
                (DataRead dr) -> {
                    var purchaseInfo = new PurchaseInfo();
                    purchaseInfo.readValues(dr);

                    return purchaseInfo;
                });
    }

    public final Integer getNextTicketIndex(String peopleId, String code) throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "ticketsnum_purchase", peopleId, code).find();
    }

    /**
     * Graba la compra y, si viene, su retencion en una sola transaccion.
     *
     * Antes eran dos guardados sueltos y podia quedar la compra sin retencion,
     * con el numero de la serie RT ya consumido. Ahora o quedan las dos o no
     * queda ninguna.
     *
     * Una compra que ya trae numero es una que ya esta en la base: se abrio
     * desde el buscador solo para agregarle la retencion que le faltaba, asi
     * que no se vuelve a insertar.
     */
    public final Integer savePurchase(final PurchaseInfo purchase, final InventoryRecord rec,
            final WithholdInfo withhold) throws BasicException {

        Transaction t;
        t = new Transaction(s) {
            @Override
            public Object transact() throws BasicException {

                if (purchase.getNumber() == null) {
                    savePurchaseOnly(purchase, rec);
                }

                if (withhold != null) {
                    saveWithholdOnly(withhold);
                }

                return purchase.getNumber();
            }
        };

        return Integer.valueOf(t.execute().toString());
    }

    /**
     * La compra en si: recibo, cabecera, lineas y movimientos de stock.
     *
     * Corre siempre dentro de la transaccion de savePurchase; el numero se pide
     * aqui adentro y no antes, o un fallo posterior lo dejaria quemado.
     */
    private void savePurchaseOnly(final PurchaseInfo purchase, final InventoryRecord rec)
            throws BasicException {

                purchase.setNumber((Integer) getPurchaseSequence().find());

                new PreparedSentence(s,
                        "INSERT INTO receipts (ID, MONEY, DATENEW, PERSON) VALUES (?, ?, ?, ?)",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, purchase.getId());
                                setString(2, purchase.getMoney());
                                setTimestamp(3, purchase.getCreatedAt());
                                setString(4, purchase.getUser().getId());
                            }
                        });

                if ("03".equals(purchase.getPurchaseDocument())) {
                    purchase.setCode("LQ");
                    var sequence = getNextTicketIndex(purchase.getUser().getId(), purchase.getCode());
                    purchase.setPurchaseReference(purchase.getSerie()
                            .concat(String.format(purchase.getFormatNumberDigits(), sequence))
                    );
                    purchase.setPurchaseAuthorization(purchase.buildAccessKey());
                }

                new PreparedSentence(s,
                        "INSERT INTO purchases "
                        + "(id, number, reason, supplier, purchase_tax_support, "
                        + "purchase_document, purchase_reference, purchase_date, purchase_authorization, "
                        + "location, observation) "
                        + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        SerializerWriteParams.INSTANCE)
                        .exec(new DataParams() {

                            @Override
                            public void writeValues() throws BasicException {
                                setString(1, purchase.getId());
                                setInt(2, purchase.getNumber());
                                setInt(3, purchase.getReason());
                                setString(4, purchase.getSupplier().getID());
                                setString(5, purchase.getPurchaseTaxSupport());
                                setString(6, purchase.getPurchaseDocument());
                                setString(7, purchase.getPurchaseReference());
                                setTimestamp(8, purchase.getPurchaseDate());
                                setString(9, purchase.getPurchaseAuthorization());
                                setString(10, purchase.getLocation());
                                setString(11, purchase.getObservation());
                            }
                        });

                SentenceExec purchaselineinsert = new PreparedSentence(s,
                        "INSERT INTO purchaselines (PURCHASE, LINE, "
                        + "PRODUCT, UNITS, PRICE, TAXID, LOT, TAX_SUPPORT) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        SerializerWriteParams.INSTANCE);

                for (int i = 0; i < purchase.getInvLines().size(); i++) {
                    final int line = i;

                    InventoryLine inv = rec.getLines().get(i);

                    purchaselineinsert.exec(new DataParams() {
                        @Override
                        public void writeValues() throws BasicException {
                            setString(1, purchase.getId());
                            setInt(2, line);
                            setString(3, inv.getProductID());
                            setDouble(4, inv.getMultiply());
                            setDouble(5, inv.getPrice());
                            setString(6, inv.getTax().getId());
                            setString(7, inv.getLot());
                            setString(8, inv.getTaxSupport());
                        }
                    });

                    Object params = new Object[]{
                        UuidCreator.getTimeOrderedEpoch().toString(),
                        purchase.getCreatedAt(),
                        rec.getReason().getKey(),
                        rec.getLocation().getID(),
                        inv.getProductID(),
                        inv.getProductAttSetInstId(),
                        rec.getReason().samesignum(inv.getMultiply()),
                        inv.getPrice(),
                        rec.getUser(),
                        rec.getSupplier().getID(),
                        rec.getSupplierDoc(),
                        inv.getLot()
                    };

                    new PreparedSentence(s,
                            "UPDATE products SET pricebuy = ? "
                            + "WHERE id = ?",
                            new SerializerWriteBasicExt(stockdiaryDatas, new int[]{7, 4})).exec(params);

                    if (inv.isService() != true) {

                        int updateresult = ((Object[]) params)[5] == null
                                ? new PreparedSentence(s,
                                        "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                                        + "WHERE LOCATION = ? AND PRODUCT = ? AND LOT = ? "
                                        + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                                        new SerializerWriteBasicExt(stockdiaryDatas, new int[]{6, 3, 4, 11})).exec(params)
                                : new PreparedSentence(s,
                                        "UPDATE stockcurrent SET UNITS = (UNITS + ?) "
                                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                                        + "AND ATTRIBUTESETINSTANCE_ID = ? AND LOT = ?",
                                        new SerializerWriteBasicExt(stockdiaryDatas, new int[]{6, 3, 4, 5, 11})).exec(params);

                        if (updateresult == 0) {
                            new PreparedSentence(s,
                                    "INSERT INTO stockcurrent (LOCATION, PRODUCT, "
                                    + "ATTRIBUTESETINSTANCE_ID, UNITS, LOT) "
                                    + "VALUES (?, ?, ?, ?, ?)",
                                    new SerializerWriteBasicExt(stockdiaryDatas, new int[]{3, 4, 5, 6, 11})).exec(params);
                        }
                        new PreparedSentence(s,
                                "INSERT INTO stockdiary (ID, DATENEW, REASON, LOCATION, PRODUCT, "
                                + "ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, AppUser, "
                                + "SUPPLIER, SUPPLIERDOC, LOT) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                new SerializerWriteBasicExt(stockdiaryDatas,
                                        new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11})).exec(params);

                    }

                }
    }

    /**
     * La retencion: numero de la serie RT, clave de acceso, cabecera y detalle.
     *
     * Antes vivia en DataLogicWithhold.saveWithhold con su propia transaccion.
     * Se movio aqui para que comparta la de la compra.
     */
    private void saveWithholdOnly(final WithholdInfo withhold) throws BasicException {

        var sequence = getNextTicketIndex(withhold.getUser().getId(), withhold.getCode());

        withhold.setSerieNumber(withhold.getSerie()
                .concat(String.format(withhold.getFormatNumberDigits(), sequence))
        );
        withhold.setAccessKey(withhold.buildAccessKey());

        new PreparedSentence(s,
                "INSERT INTO withholds "
                + "(id, code, serie_number, purchase_id, date_withhold, "
                + "observation, fiscal_period, access_key, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                SerializerWriteParams.INSTANCE)
                .exec(new DataParams() {
                    @Override
                    public void writeValues() throws BasicException {
                        setString(1, withhold.getId());
                        setString(2, withhold.getCode());
                        setString(3, withhold.getSerieNumber());
                        setString(4, withhold.getPurchaseId());
                        setTimestamp(5, withhold.getDateWithhold());
                        setString(6, withhold.getObservation());
                        setTimestamp(7, withhold.getFiscalPeriod());
                        setString(8, withhold.getAccessKey());
                        setBoolean(9, withhold.getStatus());
                    }
                });

        SentenceExec detailInsert = new PreparedSentence(s,
                "INSERT INTO withholds_detail "
                + "(withhold_id, line, withhold_taxes_id, percentage, "
                + "base_value, withholded_value, tax_rate, tax_support) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                SerializerWriteParams.INSTANCE);

        for (int i = 0; i < withhold.getLines().size(); i++) {
            final int line = i;
            final WithholdLineInfo detail = withhold.getLines().get(i);

            detailInsert.exec(new DataParams() {
                @Override
                public void writeValues() throws BasicException {
                    setString(1, withhold.getId());
                    setInt(2, line);
                    setString(3, detail.getWithholdTaxesId());
                    setDouble(4, detail.getPercentage());
                    setDouble(5, detail.getBaseValue());
                    setDouble(6, detail.getWithholdedValue());
                    setDouble(7, detail.getTaxRate());
                    setString(8, detail.getTaxSupport());
                }
            });
        }
    }

    public PreparedSentence getPurchaseSequence() {
        return new PreparedSentence(s,
                "SELECT ifnull(max(number) + 1, 1) as sequence from purchases",
                null,
                (DataRead dr) -> dr.getInt(1));
    }

    public SentenceList getTaxSupportList() {
        return new StaticSentence(s,
                new QBFBuilder("SELECT id, CONCAT(id, ' - ', name) name from tax_supports "
                        + "where status = true "
                        + "order by id asc",
                        new String[]{"id", "name"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING
        }),
                (DataRead dr) -> {
                    var taxSupport = new TaxSupportInfo(
                            dr.getString(1),
                            dr.getString(2)
                    );

                    return taxSupport;
                });
    }

    public SentenceList getDocumentTypeList() {
        return new StaticSentence(s,
                new QBFBuilder("SELECT id, CONCAT(id, ' - ', name) name from document_types "
                        + "where status = true "
                        + "order by id asc",
                        new String[]{"id", "name"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING
        }),
                (DataRead dr) -> {
                    var taxSupport = new TaxSupportInfo(
                            dr.getString(1),
                            dr.getString(2)
                    );

                    return taxSupport;
                });
    }

    public final SentenceList getPurchaseListByData(Date startDate, Date endDate, String data) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        var startDateFormatted = format.format(startDate);
        var endDateFormatted = format.format(endDate);
        var sql = "SELECT "
                + "p.id, "
                + "p.number purchase, "
                + "p.created_at saved, "
                + "s.id supplier_id, "
                + "s.name supplier, "
                + "p.purchase_date date, "
                + "d.name document, "
                + "p.purchase_reference reference, "
                + "p.observation "
                + "from purchases p "
                + "join suppliers s on s.id = p.supplier  "
                + "join document_types d on d.id = p.purchase_document "
                + "where DATE_FORMAT(p.created_at, '%Y-%m-%d') BETWEEN '" + startDateFormatted + "' and '" + endDateFormatted + "' "
                + "and (s.name like '%" + data + "%' or p.purchase_reference like '%" + data + "%')";

        return new StaticSentence(s,
                sql,
                null,
                PurchaseInfo.getSerializerRead());
    }

    public final PurchaseInfo loadPurchase(String purchaseId, DataLogicSales dlSales) throws BasicException {

        var purchase = (PurchaseInfo) getPurchaseInfo().find(purchaseId);
        SentenceList sentTax;
        TaxesLogic taxeslogic;

        sentTax = dlSales.getTaxList();
        java.util.List<TaxInfo> taxlist;

        taxlist = sentTax.list();
        taxeslogic = new TaxesLogic(taxlist);

        purchase.setInvLines(
                new PreparedSentence(s, "SELECT "
                        + "l.product, p.name, l.units, l.price, l.taxid, l.lot, tx.category, "
                        + "l.tax_support "
                        + "FROM purchaselines l "
                        + "join products p on p.id = l.product "
                        + "join taxes tx on tx.id = l.taxid "
                        + "where l.purchase = ? "
                        + "order by l.line asc ",
                        SerializerWriteString.INSTANCE,
                        (DataRead dr) -> {
                            var product = new ProductInfoExt();
                            product.setID(dr.getString(1));
                            product.setName(dr.getString(2));

                            var tax = taxeslogic.getTaxInfo(dr.getString(7), null);
                            var line = new InventoryLine(
                                    product,
                                    dr.getDouble(3),
                                    Math.round(dr.getDouble(3) * dr.getDouble(4)),
                                    dr.getString(6),
                                    tax
                            );
                            line.setTaxSupport(dr.getString(8));

                            return line;
                        })
                        .list(purchaseId));

        return purchase;
    }

    public final void deleteTicket(final PurchaseInfo purchase, DataLogicSales dlSales) throws BasicException {

        Transaction t;
        t = new Transaction(s) {
            @Override
            public Object transact() throws BasicException {

                for (int i = 0; i < purchase.getLinesCount(); i++) {
                    if (purchase.getLine(i).getProductID() != null) {
                        dlSales.getStockDiaryInsert().exec(new Object[]{
                            UuidCreator.getTimeOrderedEpoch().toString(),
                            new Date(),
                            MovementReason.OUT_MOVEMENT.getKey(),
                            purchase.getLocation(),
                            purchase.getLine(i).getProductID(),
                            purchase.getLine(i).getProductAttSetInstId(),
                            MovementReason.OUT_MOVEMENT.samesignum(purchase.getLine(i).getMultiply()),
                            purchase.getLine(i).getPrice(),
                            purchase.getUser().getName(),
                            purchase.getLine(i).getLot()
                        });
                    }
                }

                new StaticSentence(s,
                        "DELETE FROM purchases WHERE ID = ?",
                        SerializerWriteString.INSTANCE).exec(purchase.getId());
                new StaticSentence(s,
                        "DELETE FROM receipts WHERE ID = ?",
                        SerializerWriteString.INSTANCE).exec(purchase.getId());
                return null;
            }
        };
        t.execute();
    }
}
