package net.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.CreateClipList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TwitchClipThat {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public TwitchClipThat(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name = "clipthat", example = "&clipthat", help = "&clipthat", description = "Créer un clip automatiquement", rank = TwitchCommand.ExecutorRank.EVERYONE)
    private void clipThat(User user, String broadcaster, String[] args){
        UserList resultList = botDiscord.getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(broadcaster)).execute();
        final User[] broadcasterUser = new User[1];
        resultList.getUsers().forEach(user1 -> {
            broadcasterUser[0] = user1;
        });

        CreateClipList clipData = botDiscord.getTwitchClient().getHelix().createClip(botDiscord.getConfigurationManager().getStringValue("oauth2Token"), broadcasterUser[0].getId(), false).execute();
        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Création du clip... ClappyHype ");
        botDiscord.getScheduler().schedule(() -> clipData.getData().forEach(clip -> {
            botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Clappy Clip créé et disponible ici (le lien peut ne pas fonctionner immédiatement) > "+clip.getEditUrl());
        }), 2, TimeUnit.SECONDS);
    }

}
