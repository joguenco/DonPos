package dev.resolvedor.pos.inventory.lot;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataParams;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.Datas;
import com.unicenta.data.loader.PreparedSentence;
import com.unicenta.data.loader.QBFBuilder;
import com.unicenta.data.loader.SentenceList;
import com.unicenta.data.loader.SerializerReadInteger;
import com.unicenta.data.loader.SerializerWriteBasic;
import com.unicenta.data.loader.SerializerWriteParams;
import com.unicenta.data.loader.SerializerWriteString;
import com.unicenta.data.loader.Session;
import com.unicenta.data.loader.StaticSentence;
import com.unicenta.data.loader.TableDefinition;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.BeanFactoryDataSingle;
import java.util.List;

/**
 *
 * @author Jorge Luis
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
public class DataLogicLot extends BeanFactoryDataSingle {

    private Session s;
    private TableDefinition tdLot;

    @Override
    public void init(Session s) {
        this.s = s;

        tdLot = new TableDefinition(s,
                "lots",
                new String[]{"id", "name", "expiration_date", "status"},
                new Datas[]{Datas.STRING, Datas.STRING, Datas.TIMESTAMP, Datas.BOOLEAN},
                new Formats[]{Formats.STRING, Formats.STRING, Formats.DATE, Formats.BOOLEAN},
                new int[]{0});
    }

    public final TableDefinition getTableLot() {
        return tdLot;
    }

    public final PreparedSentence getLotInfo() {
        return new PreparedSentence(s,
                "select name, expiration_date, status "
                + "from lots "
                + "where id = ?",
                SerializerWriteString.INSTANCE,
                (DataRead dr) -> {
                    var lotInfo = new LotInfo();
                    lotInfo.readValues(dr);

                    return lotInfo;
                });
    }

    public final List<ProductLotInfo> getProductLotList(String productId) throws BasicException {

        return new PreparedSentence(s,
                "SELECT p.id product, l.id lot, p.name product_name, l.name lot_name, "
                + "l.expiration_date , if(pl.status = true, 'Active', 'Inactive') as status, s.units stock "
                + "from products p "
                + "inner join products_lots pl "
                + "on p.id = pl.product "
                + "inner join lots l "
                + "on l.id = pl.lot "
                + "left join stockcurrent s "
                + "on s.lot = pl.lot "
                + "and s.product = pl.product "
                + "where p.id = ?",
                SerializerWriteString.INSTANCE,
                ProductLotInfo.getSerializerRead()).list(productId);
    }

    public final List<ProductLotInfo> getLotProductList(String lotId) throws BasicException {

        return new PreparedSentence(s,
                "SELECT p.id product, l.id lot, p.name product_name, l.name lot_name, "
                + "l.expiration_date , if(pl.status = true, 'Active', 'Inactive') as status, s.units stock "
                + "from products p "
                + "inner join products_lots pl "
                + "on p.id = pl.product "
                + "inner join lots l "
                + "on l.id = pl.lot "
                + "left join stockcurrent s "
                + "on s.lot = pl.lot "
                + "and s.product = pl.product "
                + "where pl.lot = ?",
                SerializerWriteString.INSTANCE,
                ProductLotInfo.getSerializerRead()).list(lotId);
    }

    public SentenceList getProductList() {
        return new StaticSentence(s,
                new QBFBuilder("select id, name from products "
                        + "order by name",
                        new String[]{"id", "name"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING
        }),
                (DataRead dr) -> {
                    var product = new ProductInfo(
                            dr.getString(1),
                            dr.getString(2)
                    );

                    return product;
                });
    }

    public SentenceList getLotList(String productId) {
        return new StaticSentence(s,
                new QBFBuilder("SELECT l.id, "
                        + "concat(l.name, if(l.expiration_date is null, '',concat(' | ', l.expiration_date)), ' | (', if(s.units is null, 0, s.units), ')') name "
                        + "from products p "
                        + "inner join products_lots pl on p.id = pl.product "
                        + "inner join lots l on l.id = pl.lot "
                        + "left join stockcurrent s on s.lot = pl.lot "
                        + "and s.product = pl.product "
                        + "where p.id = '" + productId + "'",
                        new String[]{"id", "name"}),
                new SerializerWriteBasic(new Datas[]{
            Datas.OBJECT, Datas.STRING,
            Datas.OBJECT, Datas.STRING
        }),
                (DataRead dr) -> {
                    var product = new ProductInfo(
                            dr.getString(1),
                            dr.getString(2)
                    );

                    return product;
                });
    }

    public int saveLotProduct(String lotId, String productId) throws BasicException {

        return new PreparedSentence(s,
                "INSERT INTO products_lots(lot, product) VALUES(?, ?)",
                SerializerWriteParams.INSTANCE
        ).exec(new DataParams() {
            @Override
            public void writeValues() throws BasicException {
                setString(1, lotId);
                setString(2, productId);
            }
        });
    }

    public int deleteLotProduct(String lotId, String productId) throws BasicException {
        return new PreparedSentence(s,
                "DELETE from products_lots "
                + "where lot = ? "
                + "and product = ?",
                SerializerWriteParams.INSTANCE
        ).exec(new DataParams() {
            @Override
            public void writeValues() throws BasicException {
                setString(1, lotId);
                setString(2, productId);
            }
        });
    }

    public final void updateStatusLotProduct(String lotId, String productId) throws BasicException {

        PreparedSentence p = new PreparedSentence(s, "SELECT status from products_lots "
                + "where lot = ? "
                + "and product = ?",
                new SerializerWriteBasic(Datas.STRING, Datas.STRING),
                SerializerReadInteger.INSTANCE);

        var status = (Integer) p.find(lotId, productId) == 1 ? 0 : 1;

        new PreparedSentence(s,
                "UPDATE products_lots "
                + "set status = ? "
                + "where lot = ? "
                + "and product = ?",
                SerializerWriteParams.INSTANCE
        ).exec(new DataParams() {
            @Override
            public void writeValues() throws BasicException {
                setInt(1, status);
                setString(2, lotId);
                setString(3, productId);
            }
        });
    }
}
