//    Mestizo Pos - Touch Friendly Point Of Sale
//    https://resolvedor.dev
//
//    Mestizo Pos is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Mestizo Pos is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Mestizo Pos.  If not, see <http://www.gnu.org/licenses/>.
package dev.joguenco.pos.taxpayer;

import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.Datas;
import com.unicenta.data.loader.PreparedSentence;
import com.unicenta.data.loader.SerializerWriteString;
import com.unicenta.data.loader.Session;
import com.unicenta.data.loader.TableDefinition;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.BeanFactoryDataSingle;

/**
 *
 * @author jorgeluis
 */
public class DataLogicTaxpayer extends BeanFactoryDataSingle {

    private Session s;
    private TableDefinition tdTaxPayer;

    @Override
    public void init(Session s) {
        this.s = s;

        this.tdTaxPayer = new TableDefinition(s,
                "taxpayer",
                new String[]{"id", "identification", "legal_name", "forced_accounting",
                    "special_taxpayer", "retention_agent", "other"},
                new String[]{"id", "identification", "legal_name", "forced_accounting",
                    "special_taxpayer", "retention_agent", "other"},
                new Datas[]{Datas.INT, Datas.STRING, Datas.STRING, Datas.STRING,
                    Datas.STRING, Datas.STRING, Datas.STRING},
                new Formats[]{Formats.INT, Formats.STRING, Formats.STRING, Formats.STRING,
                    Formats.STRING, Formats.STRING, Formats.STRING},
                new int[]{0});
    }

    public final TableDefinition getTableTaxPayer() {
        return this.tdTaxPayer;
    }

    public final PreparedSentence getTaxPayerInfo() {
        return new PreparedSentence(s,
                "select identification, legal_name, forced_accounting, special_taxpayer, "
                + "retention_agent, other from taxpayer "
                + "where id = ?",
                SerializerWriteString.INSTANCE,
                (DataRead dr) -> {
                    TaxpayerInfo taxPayer = new TaxpayerInfo();

                    taxPayer.readValues(dr);

                    return taxPayer;
                });
    }
}
