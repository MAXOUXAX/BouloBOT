package me.maxouxax.boulobot.util;

import io.sentry.Sentry;
import me.maxouxax.boulobot.BOT;

import java.util.Arrays;
import java.util.logging.Level;

public class ErrorHandler {

    private final BOT bot;

    public ErrorHandler() {
        this.bot = BOT.getInstance();
    }

    public void handleException(Throwable exception){
        bot.getLogger().log(Level.SEVERE, "Une erreur est survenue !\n"+exception.getMessage());
        exception.printStackTrace();
        bot.getLogger().log(Level.SEVERE, exception.getMessage()+"\n"+Arrays.toString(exception.getStackTrace()), false);
        Sentry.captureException(exception);
    }



}
