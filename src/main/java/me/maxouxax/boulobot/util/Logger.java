package me.maxouxax.boulobot.util;

import me.maxouxax.boulobot.BOT;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class Logger {

    private final File file;
    private final Date date;
    private final BOT bot;

    public Logger() throws IOException {
        this.bot = BOT.getInstance();
        this.date = new Date();
        this.file = new File("logs", "latest.log");
        this.file.mkdirs();
        this.file.createNewFile();
    }

    public void log(Level level, String str) {
        log(level, str, true);
    }

    public void log(Level level, String str, boolean sendConsoleMessage) {
        String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
        if (sendConsoleMessage) System.out.println("(" + date + ")" + " | BouloBOT > [" + level.getName() + "] " + str);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.file, true))) {
            bw.write("(" + date + ")" + " | BouloBOT > [" + level.getName() + "] " + str + "\n");
        } catch (IOException e) {
            bot.getErrorHandler().handleException(e);
        }
    }

    public void save() {
        String str = new SimpleDateFormat("dd-MM-yyyy-HH-mm").format(this.date);
        if (file.renameTo(new File("logs/" + str.toLowerCase()))) {
            log(Level.INFO, "Logs saved");
        }
    }
}
