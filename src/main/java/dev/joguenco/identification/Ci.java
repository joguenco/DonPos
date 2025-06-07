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

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

/**
 *
 * @author <Jorge Luis from resolvedor.dev>
 */
@RequiredArgsConstructor
public class Ci {

    @NonNull
    private String ci;
    @Getter
    @Setter
    private String error = "";

    public Boolean validate() {
        if (!isNumeric(ci)) {
            return false;
        }
        if (!basic(10)) {
            return false;
        }
        if (!provinces()) {
            return false;
        }
        if (!module10()) {
            return false;
        }
        return true;
    }

    Boolean basic(Integer digits) {
        val length = ci.length();

        if (length == digits) {
            return true;
        } else {
            error = "El número de dígitos es incorrecto para la cédula";
        }

        return false;
    }

    Boolean isNumeric(String input) {
        try {
            Long.valueOf(input);
            return true;
        } catch (NumberFormatException e) {
            error = "La cédula debe contener solo valores numéricos";
            return false;
        }
    }

    Boolean provinces() {
        val province = Integer.parseInt(ci.substring(0, 2));

        if (province == 30 || (province >= 1 && province <= 24)) {
            return true;
        }
        error = "La provincia no corresponde, debe empezar la cédula entre 1 y 24 o 30";

        return false;
    }

    /*
        Algorithn module 10
     * Los coeficientes usados para verificar el décimo díjito de la cédula,
     * mediante el algoritmo Módulo 10 son: 2. 1. 2. 1. 2. 1. 2. 1. 2
     *
     * Paso 1: Multiplicar cada díjito de los díjitos iniciales por su respectivo
     * coeficiente.
     *
     * Ejemplo
     * digitosIniciales posicion 1 x 2
     * digitosIniciales posicion 2 x 1
     * digitosIniciales posicion 3 x 2
     * digitosIniciales posicion 4 x 1
     * digitosIniciales posicion 5 x 2
     * digitosIniciales posicion 6 x 1
     * digitosIniciales posicion 7 x 2
     * digitosIniciales posicion 8 x 1
     * digitosIniciales posicion 9 x 2
     *
     * Paso 2: Sí alguno de los resultados de cada multiplicación es mayor a o igual a 10,
     * se suma entre ambos díjitos de dicho resultado. Ex. 12->1+2->3
     *
     * Paso 3: Se suman los resultados y se obtiene total
     *
     * Paso 4: Divido total para 10, se guarda residuo. Se resta 10 menos el residuo.
     * El valor obtenido debe concordar con el díjito verificador
     *
     * Nota: Cuando el residuo es cero(0) el díjito verificador debe ser 0.
     */
    private Boolean module10() {
        val digits = ci.substring(0, 9).toCharArray();
        val checker = Integer.parseInt(ci.substring(9, 10));
        final int[] multipliers = {2, 1, 2, 1, 2, 1, 2, 1, 2};
        var total = 0;
        var residue = 0;
        var result = 0;

        for (int i = 0; i < digits.length; i++) {
            var multiplier = multipliers[i];
            var digit = Integer.parseInt(String.valueOf(digits[i]));
            var value = multiplier * digit;

            if (value >= 10) {
                value = Integer.parseInt(String.valueOf(value).substring(0, 1))
                        + Integer.parseInt(String.valueOf(value).substring(1, 2));
            }

            total = total + value;
        }

        residue = total % 10;

        if (residue == 0) {
            result = 0;
        } else {
            result = 10 - residue;
        }

        if (result == checker) {
            return true;
        }

        error = "La cédula no cumple la validación del algoritmo módulo 10";
        return false;
    }
}
