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
package dev.joguenco.types;

import com.unicenta.data.loader.IKeyed;

/**
 *
 * @author <Jorge Luis from resolvedor.dev>
 */
public class TransctionType implements IKeyed {

    private String code;
    private String description;

    public TransctionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public Object getKey() {
        return code;
    }

    @Override
    public String toString() {
        return code + " - " + description;
    }
}
