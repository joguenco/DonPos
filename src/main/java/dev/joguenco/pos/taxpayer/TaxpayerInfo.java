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

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.SerializableRead;

/**
 *
 * @author jorgeluis
 */
public class TaxpayerInfo implements SerializableRead {

    private String identification;
    private String legalName;
    private String forcedAccounting;
    private String specialTaxpayer;
    private String retentionAgent;
    private String other;

    public TaxpayerInfo() {
        identification = "";
        legalName = "";
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getForcedAccounting() {
        return forcedAccounting;
    }

    public void setForcedAccounting(String forcedAccounting) {
        this.forcedAccounting = forcedAccounting;
    }

    public String getSpecialTaxpayer() {
        return specialTaxpayer;
    }

    public void setSpecialTaxpayer(String specialTaxpayer) {
        this.specialTaxpayer = specialTaxpayer;
    }

    public String getRetentionAgent() {
        return retentionAgent;
    }

    public void setRetentionAgent(String retentionAgent) {
        this.retentionAgent = retentionAgent;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    @Override
    public void readValues(DataRead dr) throws BasicException {
        this.identification = dr.getString(1);
        this.legalName = dr.getString(2);
        this.forcedAccounting = dr.getString(3);
        this.specialTaxpayer = dr.getString(4);
        this.retentionAgent = dr.getString(5);
        this.other = dr.getString(6);
    }
}
