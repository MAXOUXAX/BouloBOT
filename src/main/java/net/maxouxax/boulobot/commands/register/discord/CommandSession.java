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
        if(botDiscord.getSessionManager().getSessions().isEmpty()){
            textChannel.sendMessage("Aucune session n'a été trouvée !").queue();
        }else {
            botDiscord.getSessionManager().getSessions().forEach(session -> {
                strB.append("\nsession.getUuid() = ").append(session.getUuid());
                strB.append("\nsession.getAvgViewers() = ").append(session.getAvgViewers());
                strB.append("\nsession.getBansAndTimeouts() = ").append(session.getBansAndTimeouts());
                strB.append("\nsession.getChannelId() = ").append(session.getChannelId());
                strB.append("\nsession.getCommandsUsed() = ").append(session.getCommandsUsed());
                strB.append("\nsession.getGameIds().toString() = ").append(session.getGameIds().toString());
                strB.append("\nsession.getStartDate() = ").append(session.getStartDate());
                strB.append("\n\n");
            });
            textChannel.sendMessage(strB.toString()).queue();
        }
    }

}
