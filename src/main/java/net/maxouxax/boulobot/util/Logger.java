package net.maxouxax.boulobot.util;

import net.maxouxax.boulobot.BOT;

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

    public Logger(BOT bot) throws IOException {
        this.bot = bot;
        this.date = new Date();
        this.file = new File("logs", "currentlogs.log");
        this.file.mkdirs();
        this.file.createNewFile();
    }

    public void log(Level level, String str){
        String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("("+date+")"+" | BouloBOT > ["+level.getName()+"] "+str);
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(this.file, true))) {
            bw.write("("+date+")"+" | BouloBOT > ["+level.getName()+"] "+str+"\n");
        }catch (IOException e){
            bot.getErrorHandler().handleException(e);
        }
    }

    public void log(Level level, String str, boolean sendConsoleMessage){
        if(sendConsoleMessage){
            log(level, str);
        }else {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.file, true))) {
                String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
                bw.write("("+date+")"+" | BouloBOT > ["+level.getName()+"] "+str+"\n");
            } catch (IOException e) {
                bot.getErrorHandler().handleException(e);
            }
        }
    }

    public void save() {
        String str = new SimpleDateFormat("dd-MM-yyyy-HH-mm").format(this.date);
        if(file.renameTo(new File("logs/old-logs-" + str.toLowerCase()))){
            log(Level.INFO, "Logs saved");
        }
    }
}
