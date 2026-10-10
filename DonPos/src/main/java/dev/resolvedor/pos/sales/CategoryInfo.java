package dev.resolvedor.pos.sales;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.IKeyed;
import com.unicenta.data.loader.SerializableRead;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Jorge Luis
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
@Getter
@Setter
public class CategoryInfo implements SerializableRead, IKeyed {

    private String id;
    private String name;

    @Override
    public Object getKey() {
        return id;
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        id = dr.getString(1);
        name = dr.getString(2);
    }

    @Override
    public String toString() {
        return name;
    }
}
