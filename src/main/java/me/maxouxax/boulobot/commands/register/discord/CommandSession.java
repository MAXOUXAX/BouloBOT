package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class CommandSession {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandSession(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name = "session", description = "Permet d'accéder à toutes les sessions", help = ".session help", example = ".session info <id>")
    public void session(User user, TextChannel textChannel, Message message, String[] args){
        //TODO: Session command
    }

}
