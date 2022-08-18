package me.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.CreateClipList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.TwitchCommand;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TwitchClipThat {

    private final BOT bot;
    private final CommandMap commandMap;

    public TwitchClipThat(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @TwitchCommand(name = "clipthat", example = "&clipthat", help = "&clipthat", description = "Créer un clip automatiquement", rank = TwitchCommand.ExecutorRank.EVERYONE)
    private void clipThat(User user, String broadcaster, String[] args) {
        UserList resultList = bot.getTwitchClient().getHelix().getUsers(bot.getConfigurationManager().getStringValue("oauth2Token"), null, Arrays.asList(broadcaster)).execute();
        final User[] broadcasterUser = new User[1];
        resultList.getUsers().forEach(user1 -> {
            broadcasterUser[0] = user1;
        });

        CreateClipList clipData = bot.getTwitchClient().getHelix().createClip(bot.getConfigurationManager().getStringValue("oauth2Token"), broadcasterUser[0].getId(), false).execute();
        bot.getTwitchClient().getChat().sendMessage(broadcaster, "\uD83C\uDFAC Création du clip... [5s \uD83D\uDEAB]");
        bot.getScheduler().schedule(() -> clipData.getData().forEach(clip -> {
            bot.getTwitchClient().getChat().sendMessage(broadcaster, "\uD83C\uDFA5 Clip créé et disponible ici » " + clip.getEditUrl().replaceFirst("/edit", ""));
        }), 5, TimeUnit.SECONDS);
    }

}
