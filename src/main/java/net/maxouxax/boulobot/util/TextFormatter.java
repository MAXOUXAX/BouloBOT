package net.maxouxax.boulobot.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TextFormatter {

    private static final String format = "EEE d MMM yyyy HH:mm:ss";

    public static String asDate(String text) {
        return text+" • "+new SimpleDateFormat(format).format(new Date());
    }

}
