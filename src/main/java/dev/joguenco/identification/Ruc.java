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
package dev.joguenco.identification;

import lombok.val;

/**
 *
 * @author <Jorge Luis from resolvedor.dev>
 */
public class Ruc extends Ci {

    private String ruc;

    public Ruc(String ruc) {
        super(ruc);
        this.ruc = ruc;
    }

    @Override
    public Boolean validate() {
        if (!isNumeric(ruc)) {
            return false;
        }
        if (!basic(13)) {
            return false;
        }
        if (!provinces()) {
            return false;
        }
        if (!establishment()) {
            return false;
        }
        return true;
    }

    @Override
    Boolean isNumeric(String input) {
        if (super.isNumeric(input)) {
            return true;
        }
        setError("El RUC debe contener solo valores numéricos");

        return false;
    }

    @Override
    Boolean basic(Integer digits) {
        if (super.basic(digits)) {
            return true;
        }
        setError("El número de dígitos es incorrecto para el RUC");

        return false;
    }

    Boolean establishment() {
        val finalNumber = Integer.parseInt(ruc.substring(10, 13));

        if (finalNumber == 1) {
            return true;
        }

        setError("Los 3 últimos díjitos del RUC deben ser 001");
        return false;
    }
}
