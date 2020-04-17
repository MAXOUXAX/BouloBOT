package net.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;

import java.util.Collections;

public class TwitchNotif {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public TwitchNotif(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name = "notif", example = "&notif", help = "&notif", description = "Permet de manuellement déclencher l'envoi de la notif de début de live", rank = TwitchCommand.ExecutorRank.MOD)
    private void notif(User user, Long broadcasterIdLong, String[] args){
        String broadcasterId = broadcasterIdLong.toString();
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
