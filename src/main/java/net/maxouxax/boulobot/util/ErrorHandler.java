package net.maxouxax.boulobot.util;

import io.sentry.Sentry;
import net.maxouxax.boulobot.BOT;

import java.util.Arrays;
import java.util.logging.Level;

public class ErrorHandler {

    private final BOT bot;

    public ErrorHandler(BOT bot) {
        this.bot = bot;
    }

    public void handleException(Throwable exception){
        bot.getLogger().log(Level.SEVERE, "Une erreur est survenue !\n"+exception.getMessage());
        exception.printStackTrace();
        bot.getLogger().log(Level.SEVERE, exception.getMessage()+"\n"+Arrays.toString(exception.getStackTrace()), false);
        Sentry.capture(exception);
    }



}
