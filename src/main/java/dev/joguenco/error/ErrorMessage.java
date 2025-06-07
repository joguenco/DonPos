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
package dev.joguenco.error;

import lombok.Getter;

/**
 *
 * @author <Jorge Luis from resolvedor.dev>
 */
public class ErrorMessage {

    @Getter
    private Boolean isError;
    @Getter
    private String message;

    public ErrorMessage() {
        isError = false;
        message = "";
    }

    public ErrorMessage(String message) {
        isError = true;
        this.message = message;
    }
}
