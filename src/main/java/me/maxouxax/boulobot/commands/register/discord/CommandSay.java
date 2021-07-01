package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.ConsoleCommand;

public class CommandSay {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandSay(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @ConsoleCommand(name = "say", description = "Permet d'envoyer un message personnalisé dans le tchat twitch", help = "say")
    public void say(String[] args){
        StringBuilder messageStr = new StringBuilder();
        for (String arg : args) {
            messageStr.append(arg).append(" ");
        }
        bot.getTwitchClient().getChat().sendMessage(bot.getChannelName().toLowerCase(), messageStr.toString());
    }

}
