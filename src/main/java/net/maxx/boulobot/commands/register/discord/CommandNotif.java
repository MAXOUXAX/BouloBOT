package net.maxx.boulobot.commands.register.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.Command;
import net.maxx.boulobot.commands.CommandMap;

public class CommandNotif {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public CommandNotif(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @Command(name = "notif", description = "Permet de manuellement déclencher l'envoi de la notif de début de live", help = ".notif", example = ".notif", power = 100, type = Command.ExecutorType.USER)
    public void notif(User user, TextChannel textChannel, Message message, String[] args){
        //botDiscord.sendGoLiveNotif();
    }

}
