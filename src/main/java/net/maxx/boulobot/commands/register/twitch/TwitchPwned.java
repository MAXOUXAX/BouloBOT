package net.maxx.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.User;
import me.legrange.haveibeenpwned.Breach;
import me.legrange.haveibeenpwned.HaveIBeenPwndApi;
import me.legrange.haveibeenpwned.HaveIBeenPwndException;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.commands.TwitchCommand;

import java.text.SimpleDateFormat;
import java.util.List;

public class TwitchPwned {

    private final BOT botDiscord;
    private final CommandMap commandMap;

    public TwitchPwned(BOT botDiscord, CommandMap commandMap){
        this.botDiscord = botDiscord;
        this.commandMap = commandMap;
    }

    @TwitchCommand(name="pwned", description = "Permet de récupérer les brèches trouvées via votre adresse e-mail", example = "&pwned example@test.com", help = "&pwned <adresse email>")
    private void pwned(User user, String broadcaster, String[] args){
        if(args.length == 0){
            botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Veuillez spécifier votre email.\n» `.pwned example@test.com`");
        }else {
            String email = args[0];
            try {
                HaveIBeenPwndApi hibp = new HaveIBeenPwndApi("PwnedDiscordBotCommand");
                List<Breach> breaches = hibp.getAllBreachesForAccount(email);
                if (breaches.size() <= 0) {
                    botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "';--have i been pwned? n'a trouvé aucune brèche !");
                } else {
                    botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "';--have i been pwned? a trouvé " + breaches.size() + " " + (breaches.size() == 1 ? "brèche !" : "brèches !") + " | Retrouvez plus de détails en MP !");
                    breaches.forEach(breach -> {
                        botDiscord.getTwitchClient().getChat().sendPrivateMessage(user.getDisplayName().toLowerCase(), breach.getName() + " via " + breach.getDomain() + " | " + breach.getDescription().replaceAll("\\<[^>]*>", "") + " | " + new SimpleDateFormat("EEE, d MMM yyyy").format(breach.getBreachDate()));
                    });
                }

            } catch (HaveIBeenPwndException | NoSuchMethodError e) {
                botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, "Failure!\nAPI returned error code: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
