package com.sinnovations.mediaguide.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Patil on 04-Dec-17.
 *
 */

public class DateUtils {

    public static String getCustomDate(String date) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date newDate = parser.parse(date);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return formatter.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
