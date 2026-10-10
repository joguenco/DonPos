/*
 * Copyright (C) 2025 Jorge Luis
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
package dev.resolvedor.util;

import com.unicenta.basic.BasicException;
import com.unicenta.format.Formats;
import com.unicenta.pos.forms.AppConfig;
import com.unicenta.pos.forms.AppLocal;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author Jorge Luis
 * @web https://joguenco.dev
 * @mail jorgeluis@joguenco.dev
 */
public class Read {

    public static Double currency(String sValue) {
        try {
            return (Double) Formats.CURRENCY.parseValue(sValue);
        } catch (BasicException e) {
            return null;
        }
    }

    public static Date dateValidate(String dateString) {
        
        String format = getDateFormat();

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate localDate = LocalDate.parse(dateString, formatter);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String getDateFormat() {
        AppConfig config = new AppConfig(new File((System.getProperty("user.home")), AppLocal.APP_ID + ".properties"));
        String format = config.getProperty("format.date");
        
        if (format == null || format.trim().isEmpty()) {
            format = getSystemDateFormat();
        }
        
        return format;
    }

    private static String getSystemDateFormat() {
        Locale locale = Locale.getDefault();
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        if (df instanceof SimpleDateFormat) {
            return ((SimpleDateFormat) df).toPattern();
        } else {
            return "yyyy-MM-dd";
        }
    }
}
