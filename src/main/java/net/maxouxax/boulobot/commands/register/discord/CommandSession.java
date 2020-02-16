package net.maxouxax.boulobot.commands.register.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;

public class CommandSession {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public CommandSession(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @Command(name = "session", description = "Permet d'accéder à toutes les sessions", help = ".session help", example = ".session info <id>", type = Command.ExecutorType.USER)
    public void session(User user, TextChannel textChannel, Message message, String[] args){
        textChannel.sendTyping().queue();
        StringBuilder strB = new StringBuilder();
        botDiscord.getSessionManager().getSessions().forEach(session -> {
            strB.append("session.getUuid() = ").append(session.getUuid());
            strB.append("session.getAvgViewers() = ").append(session.getAvgViewers());
            strB.append("session.getBansAndTimeouts() = ").append(session.getBansAndTimeouts());
            strB.append("session.getChannelId() = ").append(session.getChannelId());
            strB.append("session.getCommandsUsed() = ").append(session.getCommandsUsed());
            strB.append("session.getCurrentGameId() = ").append(session.getCurrentGameId());
            strB.append("session.getStartDate() = ").append(session.getStartDate());
        });
        textChannel.sendMessage(strB.toString()).queue();
    }

}
