package net.maxouxax.boulobot.commands.register.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;

public class CommandSession {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandSession(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name = "session", description = "Permet d'accéder à toutes les sessions", help = ".session help", example = ".session info <id>", type = Command.ExecutorType.USER)
    public void session(User user, TextChannel textChannel, Message message, String[] args){
        //TODO: Session command
    }

}
