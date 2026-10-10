package dev.resolvedor.pos.inventory.management;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.IKeyed;
import com.unicenta.data.loader.SerializableRead;
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
public class TaxSupportInfo implements SerializableRead, IKeyed {

    private String id;
    private String name;

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(1);
        name = dr.getString(2);
    }

    @Override
    public Object getKey() {
        return getId();
    }

    @Override
    public String toString() {
        return getName();
    }
}
