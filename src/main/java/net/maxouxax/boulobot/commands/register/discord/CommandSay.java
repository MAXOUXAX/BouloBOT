package net.maxouxax.boulobot.commands.register.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;

public class CommandSay {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandSay(BOT bot, CommandMap commandMap){
        this.bot = bot;
        this.commandMap = commandMap;
    }

    @Command(name = "say", description = "Permet d'envoyer un message personnalisé dans le tchat twitch", help = ".say", example = ".say", power = 100, type = Command.ExecutorType.CONSOLE)
    public void say(User user, TextChannel textChannel, Message message, String[] args){
        StringBuilder messageStr = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            messageStr.append(args[i]).append(" ");
        }
        bot.getTwitchClient().getChat().sendMessage(bot.getChannelName().toLowerCase(), messageStr.toString());
    }

}
