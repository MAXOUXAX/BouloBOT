package me.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.helix.domain.User;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.SimpleTwitchCommand;
import me.maxouxax.boulobot.commands.TwitchCommand;

import java.util.Set;

public class TwitchHelp {

    private final CommandMap commandMap;
    private final BOT bot;

    public TwitchHelp(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @TwitchCommand(name="help",rank = TwitchCommand.ExecutorRank.EVERYONE,description="Affiche l'entièreté des commandes disponibles", help = "&help", example = "&help")
    private void help(User user, String broadcaster, Set<CommandPermission> commandPermissions, String[] args){
        StringBuilder builder = new StringBuilder();
        builder.append("Aide » Liste des commandes");

        for(SimpleTwitchCommand command : commandMap.getTwitchCommands()){
            if(command.getExecutorRank().getPower() <= commandMap.getRank(commandPermissions).getPower()) {
                builder.append("\n | » ").append(command.getName()).append(" • ").append(command.getDescription());
            }
        }

        bot.getTwitchClient().getChat().sendPrivateMessage(user.getDisplayName().toLowerCase(), builder.toString());
        bot.getTwitchClient().getChat().sendMessage(broadcaster, user.getDisplayName()+", veuillez regarder vos message privés.");

    }

}
