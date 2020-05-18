package net.maxouxax.boulobot.commands.register.twitch;

import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;

public class TwitchVersion {

    private final CommandMap commandMap;
    private final BOT bot;

    public TwitchVersion(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @TwitchCommand(name="version",description="Affiche les informations sur la version du BOT", help = "&version", example = "&version")
    private void version(String broadcaster){
        try {
            bot.getTwitchClient().getChat().sendMessage(broadcaster, "BouloBOT by MAXOUXAX • Amazingly powerful. | Je suis en version " + bot.getVersion() + " | Je gère " + commandMap.getDiscordCommands().size() + " commandes Discord | Je gère " + commandMap.getTwitchCommands().size() + " commandes Twitch | J'ai souhaité la bienvenue à " + commandMap.getUserIds().size() + " viewers !");
        }catch (Exception e){
            bot.getErrorHandler().handleException(e);
            e.printStackTrace();
            bot.getTwitchClient().getChat().sendMessage(broadcaster, "An error occured. > "+e.getMessage());
        }
    }

}
