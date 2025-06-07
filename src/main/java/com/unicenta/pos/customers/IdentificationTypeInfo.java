/*
 * Copyright (C) 2023 <Jorge Luis from resolvedor.dev>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.unicenta.pos.customers;

import com.unicenta.basic.BasicException;
import com.unicenta.data.loader.DataRead;
import com.unicenta.data.loader.IKeyed;
import java.io.Serializable;

/**
 *
 * @author <Jorge Luis from resolvedor.dev>
 */
public class IdentificationTypeInfo implements Serializable, IKeyed {

    protected String code;
    protected String name;

    public IdentificationTypeInfo(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void readValues(DataRead dr) throws BasicException {
        code = dr.getString(1);
        name = dr.getString(2);
    }

    @Override
    public Object getKey() {
        return this.code;
    }

    @Override
    public String toString() {
        return getName();
    }
}
