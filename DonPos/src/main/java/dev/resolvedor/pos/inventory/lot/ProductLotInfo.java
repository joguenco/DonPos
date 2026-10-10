package dev.resolvedor.pos.inventory.lot;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.SerializerRead;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author Jorge Luis
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
@Getter
@AllArgsConstructor
public class ProductLotInfo {

    private String product;
    private String lot;
    private String productName;
    private String lotName;
    private Date expirationDate;
    private String status;
    private Double stock;

    public static SerializerRead getSerializerRead() {
        return new SerializerRead() {

            @Override
            public Object readValues(DataRead dr) throws BasicException {

                String product = dr.getString(1);
                String lot = dr.getString(2);
                String productName = dr.getString(3);
                String lotName = dr.getString(4);
                Date expiration_date = dr.getTimestamp(5);
                String status = dr.getString(6);
                Double stock = dr.getDouble(7);

                return new ProductLotInfo(product, lot, productName, lotName, expiration_date, status, stock);
            }
        };
    }
}
