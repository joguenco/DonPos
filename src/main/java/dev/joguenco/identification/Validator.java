/*
 * Copyright (C) 2023 <Jorge Luis from mestizos.dev>
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

import dev.joguenco.error.ErrorMessage;

/**
 *
 * @author <Jorge Luis from mestizos.dev>
 */
public class Validator {

    private String country;

    public Validator(String country) {
        this.country = country;
    }

    public ErrorMessage identification(String identificationType, String identification) {
        if ("EC".equals(this.country)) {
            return identificationEc(identificationType, identification);
        }

        return new ErrorMessage();
    }

    private ErrorMessage identificationEc(String identificationType, String identification) {
        if (identificationType.equals("C")) {
            final var ci = new Ci(identification);
            if (!ci.validate()) {
                return new ErrorMessage(ci.getError());
            }
        } else if (identificationType.equals("R")) {
            final var ruc = new Ruc(identification);
            if (!ruc.validate()) {
                return new ErrorMessage(ruc.getError());
            }
        } else if (identificationType.equals("CF")) {
            if (!identification.equals("9999999999999")) {
                return new ErrorMessage("Error al validar el Consumidor Final");
            }
        }

        return new ErrorMessage();
    }

    public ErrorMessage blank(String text, String message) {
        if ("".equals(text)) {
            return new ErrorMessage(message);
        }

        return new ErrorMessage();
    }
}
