package me.maxouxax.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.User;
import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.TwitchCommand;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;

public class TwitchWeather {

    private final CommandMap commandMap;
    private final BOT bot;

    public TwitchWeather(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @TwitchCommand(name = "météo", rank = TwitchCommand.ExecutorRank.EVERYONE, description = "Permet de récupérer la météo actuelle dans une ville donnée", example = "&météo Paris", help = "&météo")
    public void weather(User user, String broadcaster, String[] args) {
        try {
            String em = commandMap.getTwitchHelpString("météo");
            if (args.length == 0) {
                bot.getTwitchClient().getChat().sendMessage(broadcaster, em);
            } else {
                StringBuilder str = new StringBuilder();

                for (String arg : args) {
                    str.append(arg).append(" ");
                }

                String city = str.toString();
                OWM owm = new OWM(bot.getConfigurationManager().getStringValue("owmApiKey"));
                owm.setLanguage(OWM.Language.FRENCH);
                owm.setUnit(OWM.Unit.METRIC);
                CurrentWeather cwd = owm.currentWeatherByCityName(city);

                StringBuilder weatherString = new StringBuilder();

                weatherString.append("Météo pour ").append(city);


                if (cwd.getMainData() != null && cwd.getMainData().hasTemp()) {
                    weatherString.append(" • Temp. » ").append(cwd.getMainData().getTemp()).append("°C");
                } else {
                    weatherString.append(" • Temp. » Aucune donnée");
                }
                weatherString.append(" • Temp. min » ").append(cwd.getMainData().getTempMin()).append("°C");
                weatherString.append(" • Temp. max » ").append(cwd.getMainData().getTempMax()).append("°C");
                if (cwd.getWindData() != null && cwd.getWindData().hasSpeed()) {
                    weatherString.append(" • Vent » ").append(cwd.getWindData().getSpeed()).append(" m/s");
                } else {
                    weatherString.append(" • Vent » Aucune donnée");
                }
                bot.getTwitchClient().getChat().sendMessage(broadcaster, weatherString.toString());
            }
        } catch (Exception e) {
            bot.getTwitchClient().getChat().sendMessage(broadcaster, "Une erreur est survenue • Message d'erreur » "+ e.getMessage());
            bot.getErrorHandler().handleException(e);
        }
    }

}
