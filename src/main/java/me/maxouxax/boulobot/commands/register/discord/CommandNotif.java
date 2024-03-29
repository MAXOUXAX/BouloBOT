package me.maxouxax.boulobot.commands.register.discord;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.ConsoleCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class CommandNotif {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandNotif(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @ConsoleCommand(name = "notif", description = "Permet de manuellement déclencher l'envoi de la notification de début de live", help = "notif")
    @Command(name = "notif", description = "Permet de manuellement déclencher l'envoi de la notifification de début de live", help = ".notif", example = ".notif", power = 100)
    public void notification(SlashCommandEvent slashCommandEvent) {
        UserList resultList = bot.getTwitchClient().getHelix().getUsers(bot.getConfigurationManager().getStringValue("oauth2Token"), null, Collections.singletonList(bot.getChannelName())).execute();
        AtomicReference<User> broadcasterUser = new AtomicReference<>();
        resultList.getUsers().stream().findFirst().ifPresent(broadcasterUser::set);
        String broadcasterId = broadcasterUser.get().getId();
        StreamList streamResultList = bot.getTwitchClient().getHelix().getStreams(bot.getConfigurationManager().getStringValue("oauth2Token"), "", "", null, null, null, null, Collections.singletonList(broadcasterId), null).execute();
        AtomicReference<Stream> currentStream = new AtomicReference<>();
        streamResultList.getStreams().stream().findFirst().ifPresent(currentStream::set);
        if (currentStream.get() == null) {
            slashCommandEvent.reply("Aucun stream est en cours").setEphemeral(true).queue();
        } else {
            String title = currentStream.get().getTitle();
            String gameId = currentStream.get().getGameId();
            bot.getSessionManager().streamStarted(title, gameId, broadcasterId);
            slashCommandEvent.reply("Notification envoyée").setEphemeral(true).queue();
        }
    }

}
