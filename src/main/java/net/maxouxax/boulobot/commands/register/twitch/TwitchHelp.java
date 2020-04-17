package net.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.SimpleTwitchCommand;
import net.maxouxax.boulobot.commands.TwitchCommand;

public class TwitchHelp {

    private final CommandMap commandMap;
    private final BOT botDiscord;

    public TwitchHelp(BOT botDiscord, CommandMap commandMap) {
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name="help",rank = TwitchCommand.ExecutorRank.EVERYONE,description="Affiche l'entièreté des commandes disponibles", help = "&help", example = "&help")
    private void help(User user, String broadcaster, String[] args){
        StringBuilder builder = new StringBuilder();
        builder.append("Aide » Liste des commandes");

        for(SimpleTwitchCommand command : commandMap.getTwitchCommands()){
            //if(command.getExecutorRank().getPower() > )

            builder.append("\n | » ").append(command.getName()).append(" • ").append(command.getDescription());
        }

        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, user.getDisplayName().toLowerCase()+" (faites pas gaff', c'est un test!)");
        botDiscord.getTwitchClient().getChat().sendPrivateMessage(user.getDisplayName().toLowerCase(), builder.toString());
        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, user.getDisplayName()+", veuillez regarder vos message privés.");

    }

}
