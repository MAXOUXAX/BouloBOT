package net.maxouxax.boulobot.commands.register.twitch;

import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;

public class TwitchVersion {

    private final CommandMap commandMap;
    private final BOT botDiscord;

    public TwitchVersion(BOT botDiscord, CommandMap commandMap) {
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name="version",description="Affiche les informations sur la version du BOT", help = "&version", example = "&version")
    private void version(String broadcaster){
        try {
            botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "BouloBOT by MAXOUXAX • Amazingly powerful. | Je suis en version " + botDiscord.getVersion() + " | Je gère " + commandMap.getDiscordCommands().size() + " commandes Discord | Je gère " + commandMap.getTwitchCommands().size() + " commandes Twitch | J'ai souhaité la bienvenue à " + commandMap.getUserIds().size() + " viewers !");
        }catch (Exception e){
            botDiscord.getErrorHandler().handleException(e);
            e.printStackTrace();
            botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "An error occured. > "+e.getMessage());
        }
    }

}
