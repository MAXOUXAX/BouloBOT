package me.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.User;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.TwitchCommand;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class TwitchNotif {

    private final BOT bot;
    private final CommandMap commandMap;

    public TwitchNotif(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @TwitchCommand(name = "notif", example = "&notif", help = "&notif", description = "Permet de manuellement déclencher l'envoi de la notif de début de live", rank = TwitchCommand.ExecutorRank.MOD)
    private void notif(User user, Long broadcasterIdLong, String broadcaster, String[] args){
        String broadcasterId = broadcasterIdLong.toString();
        StreamList streamResultList = bot.getTwitchClient().getHelix().getStreams(bot.getConfigurationManager().getStringValue("oauth2Token"), "", "", null, null, null, Collections.singletonList(broadcasterId), null).execute();
        AtomicReference<Stream> currentStream = new AtomicReference<>();
        streamResultList.getStreams().forEach(currentStream::set);
        if(currentStream.get() == null){
            bot.getTwitchClient().getChat().sendMessage(broadcaster, "Aucun stream n'est en cours !");
            return;
        }
        String title = currentStream.get().getTitle();
        String gameId = currentStream.get().getGameId();
        bot.getSessionManager().streamStarted(title, gameId, broadcasterId);
    }

}
