package net.maxouxax.boulobot.commands.register.discord;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.UserList;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;

import java.util.Collections;

public class CommandNotif {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public CommandNotif(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @Command(name = "notif", description = "Permet de manuellement déclencher l'envoi de la notif de début de live", help = ".notif", example = ".notif", power = 100, type = Command.ExecutorType.USER)
    public void notif(){
        UserList resultList = botDiscord.getTwitchClient().getHelix().getUsers(null, null, Collections.singletonList(botDiscord.getChannelName())).execute();
        final com.github.twitch4j.helix.domain.User[] broadcasterUser = new com.github.twitch4j.helix.domain.User[1];
        resultList.getUsers().forEach(user1 -> {
            broadcasterUser[0] = user1;
        });
        String broadcasterId = broadcasterUser[0].getId();
        StreamList streamResultList = botDiscord.getTwitchClient().getHelix().getStreams(botDiscord.getConfigurationManager().getStringValue("oauth2Token"), "", "", null, null, null, null, Collections.singletonList(broadcasterId), null).execute();
        final Stream[] currentStream = new Stream[1];
        streamResultList.getStreams().forEach(stream -> {
            currentStream[0] = stream;
        });
        String title = currentStream[0].getTitle();
        String gameId = currentStream[0].getGameId();
        botDiscord.sendGoLiveNotif(title, gameId, broadcasterId);
    }

}
