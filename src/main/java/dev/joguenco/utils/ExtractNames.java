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
package dev.joguenco.utils;

/**
 *
 * @author <Jorge Luis from resolvedor.dev>
 */
public class ExtractNames {

    public String extractLastName(String name) {

        String[] words = name.split(" ");
        String apellido = "";
        int spaceCount = countWhiteSpace(name);
        int i = 0;

        if (spaceCount == 0) {
            return name;
        }

        for (String token : words) {
            apellido = apellido + token;
            if (spaceCount - 1 == i || i == 1) {
                return apellido.trim();
            }
            apellido = apellido + " ";
            i++;
        }

        return "";
    }

    public String extractFirstName(String name) {

        String[] words = name.split(" ");
        String nombre = "";
        int spaceCount = countWhiteSpace(name);
        int i = 0;

        for (String token : words) {
            if (spaceCount == i || i > 1) {
                nombre = nombre + token;
                nombre = nombre + " ";
            }
            i++;
        }

        return nombre;
    }

    int countWhiteSpace(String text) {
        int spaceCount = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                spaceCount++;
            }
        }
        return spaceCount;
    }
}
