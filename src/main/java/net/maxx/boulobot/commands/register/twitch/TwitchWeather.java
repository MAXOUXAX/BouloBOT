package net.maxx.boulobot.commands.register.twitch;

import com.github.twitch4j.helix.domain.User;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.maxx.boulobot.BOT;
import net.maxx.boulobot.commands.CommandMap;
import net.maxx.boulobot.commands.TwitchCommand;

public class TwitchWeather {

    private CommandMap commandMap;
    private BOT botDiscord;

    public TwitchWeather(BOT botDiscord, CommandMap commandMap) {
        this.commandMap = commandMap;
        this.botDiscord = botDiscord;
    }

    @TwitchCommand(name = "météo", rank = TwitchCommand.ExecutorRank.EVERYONE, description = "Permet de récupérer la météo actuelle dans une ville donnée", example = "&météo Paris", help = "&météo")
    public void weather(User user, String broadcaster, String[] args) {
        try {
            String em = commandMap.getTwitchHelpString("météo");
            if (args.length == 0) {
                botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, em);
            } else {
                StringBuilder str = new StringBuilder();

                for (String arg : args) {
                    str.append(arg).append(" ");
                }

                String city = str.toString();
                OWM owm = new OWM(botDiscord.getConfigurationManager().getStringValue("owmApiKey"));
                owm.setLanguage(OWM.Language.FRENCH);
                owm.setUnit(OWM.Unit.METRIC);
                CurrentWeather cwd = owm.currentWeatherByCityName(city);

                StringBuilder weatherString = new StringBuilder();

                weatherString.append("Ville: ").append(city);


                if (cwd.getMainData() != null && cwd.getMainData().hasTemp()) {
                    weatherString.append(" | Température » ").append(cwd.getMainData().getTemp()).append("°C");
                } else {
                    weatherString.append(" | Température » Aucune donnée");
                }
                weatherString.append(" | Température min » ").append(cwd.getMainData().getTempMin()).append("°C");
                weatherString.append(" | Température max » ").append(cwd.getMainData().getTempMax()).append("°C");
                //TODO: Réactiver ceci lorsque OWM-JAPIS sera de nouveau fonctionnel avec les RainData.
                /*if (cwd.getRainData() != null && cwd.getRainData().hasPrecipVol3h()) {
                    weatherString.append(" | Précipitations » ").append(cwd.getRainData().getPrecipVol3h()).append("mm");
                } else {
                    weatherString.append(" | Précipitations » Aucune donnée");
                }
                */
                if (cwd.getWindData() != null && cwd.getWindData().hasSpeed()) {
                    weatherString.append(" | Vent » ").append(cwd.getWindData().getSpeed()).append(" m/s");
                } else {
                    weatherString.append(" | Vent » Aucune donnée");
                }
                botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, weatherString.toString());
            }
        } catch (Exception e) {
            String errorString = "Une erreur est survenue"+" | Message d'erreur » "+ e.getMessage();
            botDiscord.getTwitchClient().getChat().sendMessage(broadcaster, errorString);
            botDiscord.getErrorHandler().handleException(e);
        }
    }

}
