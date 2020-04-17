package net.maxouxax.boulobot.util;

import net.maxouxax.boulobot.BOT;

import java.util.Arrays;
import java.util.logging.Level;

public class ErrorHandler {

    private BOT botDiscord;

    public ErrorHandler(BOT botDiscord) {
        this.botDiscord = botDiscord;
    }

    public void handleException(Exception exception){
        botDiscord.getLogger().log(Level.SEVERE, "Une erreur est survenue !\n"+exception.getMessage());
        exception.printStackTrace();
        botDiscord.getLogger().log(Level.SEVERE, exception.getMessage()+"\n"+Arrays.toString(exception.getStackTrace()), false);
    }



}
