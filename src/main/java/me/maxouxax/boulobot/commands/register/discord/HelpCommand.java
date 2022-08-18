package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.ConsoleCommand;
import me.maxouxax.boulobot.commands.SimpleConsoleCommand;

import java.util.logging.Level;

public class HelpCommand {

    private final CommandMap commandMap;

    public HelpCommand(CommandMap commandMap) {
        this.commandMap = commandMap;
    }

    /*@Command(name="help", description="Affiche l'entièreté des commandes disponibles", help = ".help", example = ".help")
    private void help(User user, MessageChannel channel, Guild guild){
        EmbedCrafter embedCrafter = new EmbedCrafter();
        embedCrafter.setTitle("Aide » Liste des commandes")
            .setColor(3447003);

        for(SimpleCommand command : commandMap.getDiscordCommands()){
            if(guild != null && command.getPower() > commandMap.getPowerUser(guild, user)) continue;

            embedCrafter.addField(command.getName(), command.getDescription(), true);
        }

        if(!user.hasPrivateChannel()) user.openPrivateChannel().complete();
        ((UserImpl)user).getPrivateChannel().sendMessage(embedCrafter.build()).queue();

        channel.sendMessage(user.getAsMention()+", veuillez regarder vos message privés.").queue();

    }*/

    @ConsoleCommand(name = "help", description = "Affiche l'entièreté des commandes disponibles", help = "help")
    private void help() {
        for (SimpleConsoleCommand command : commandMap.getConsoleCommands()) {
            BOT.getInstance().getLogger().log(Level.INFO, command.getName() + " - " + command.getDescription() + " - " + command.getHelp(), true);
        }
    }

}
