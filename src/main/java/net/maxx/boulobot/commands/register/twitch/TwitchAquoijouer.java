package net.maxx.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.GameTopList;
import com.github.twitch4j.helix.domain.User;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.commands.TwitchCommand;


public class TwitchAquoijouer {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public TwitchAquoijouer(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name = "aquoijouer", example = "&aquoijouer", help = "&aquoijouer", description = "Récupérer les jeux les plus regardés en ce moment", rank = TwitchCommand.ExecutorRank.EVERYONE)
    private void aquoijouer(User user, String broadcaster, String[] args){
        StringBuilder str = new StringBuilder();
        GameTopList resultList = botDiscord.getTwitchClient().getHelix().getTopGames(null, null, null, "10").execute();
        resultList.getGames().forEach(game -> {
            str.append(game.getName()).append(" SMOrc ");
        });
        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Voici quelques idées de jeux: ");
        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, str.toString());
    }

}
