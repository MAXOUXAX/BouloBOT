package net.maxouxax.boulobot.commands.register.twitch;

import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.commands.TwitchCommand;

public class TwitchKappa {

    private final CommandMap commandMap;
    private final BOT botDiscord;
    private final String kappaSongURL = "https://www.youtube.com/watch?v=33E5P5gd9CY";

    public TwitchKappa(BOT botDiscord, CommandMap commandMap) {
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name="kappa",rank = TwitchCommand.ExecutorRank.VIP,description="Ajoute la KappaSong au songrequest", help = "&kappa", example = "&kappa")
    private void kappa(String broadcaster){
        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "!sr "+kappaSongURL);
        botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Attention, le tchat va devenir indisponible pendant la Kappa Song, gardez votre calme et BALANCEZ VOS KAPPAS.");
    }

}
