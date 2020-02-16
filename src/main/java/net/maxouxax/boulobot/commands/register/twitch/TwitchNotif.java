package net.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;

public class TwitchNotif {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public TwitchNotif(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name = "notif", example = "&notif", help = "&notif", description = "Permet de manuellement déclencher l'envoi de la notif de début de live", rank = TwitchCommand.ExecutorRank.MOD)
    private void notif(User user, String broadcaster, String[] args){
        //botDiscord.sendGoLiveNotif();
    }

}
