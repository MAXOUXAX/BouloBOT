package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class CommandSay {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandSay(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name = "say", description = "Permet d'envoyer un message personnalisé dans le tchat twitch", help = ".say", example = ".say", power = 100, type = Command.ExecutorType.CONSOLE)
    public void say(User user, TextChannel textChannel, Message message, String[] args){
        StringBuilder messageStr = new StringBuilder();
        for (String arg : args) {
            messageStr.append(arg).append(" ");
        }
        bot.getTwitchClient().getChat().sendMessage(bot.getChannelName().toLowerCase(), messageStr.toString());
    }

}
