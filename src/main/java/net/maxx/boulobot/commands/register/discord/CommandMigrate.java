package net.maxx.boulobot.commands.register.discord;

import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.Command;
import net.maxx.boulobot.commands.CommandMap;

import java.util.logging.Level;

public class CommandMigrate {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public CommandMigrate(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @Command(name = "migrate", description = "Migration.", help = ".migrate", example = ".migrate", type = Command.ExecutorType.CONSOLE)
    public void migrate(){
        botDiscord.getLogger().log(Level.INFO, "Migration en cours...");
        commandMap.setIds(commandMap.migrateFromUsernamesToUserIds());
        botDiscord.getLogger().log(Level.INFO,"Migration terminée !");
        botDiscord.getLogger().log(Level.INFO, "Redémarrage en cours...");
        botDiscord.setRunning(false);
    }

}
