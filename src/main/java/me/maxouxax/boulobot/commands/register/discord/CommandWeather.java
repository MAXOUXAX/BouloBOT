package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.slashannotations.Option;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandWeather {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandWeather(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Option(name = "Ville", description = "Ville depuis laquelle vous voulez récupérer la météo", type = OptionType.STRING, isRequired = true)
    @Command(name = "météo", help = ".météo <ville>", example = ".météo Paris", description = "Permet d'obtenir la météo sur la ville spécifiée")
    private void météo(User user, TextChannel textChannel, Guild guild, SlashCommandEvent slashCommandEvent) {
        try {
            String city = slashCommandEvent.getOption("Ville").getAsString();
            OWM owm = new OWM(bot.getConfigurationManager().getStringValue("owmApiKey"));
            owm.setLanguage(OWM.Language.FRENCH);
            owm.setUnit(OWM.Unit.METRIC);
            CurrentWeather cwd = owm.currentWeatherByCityName(city);

            EmbedCrafter weatherEmbed = new EmbedCrafter();

            weatherEmbed.setTitle("Météo pour " + cwd.getCityName(), "https://openweathermap.org")
                    .setDescription("Voici la météo actuelle pour la ville: " + cwd.getCityName())
                    .setColor(3066993);


            if (cwd.getMainData() != null && cwd.getMainData().hasTemp()) {
                weatherEmbed.addField("Température", cwd.getMainData().getTemp() + "°C", true);
            } else {
                weatherEmbed.addField("Température", "Aucune donnée", true);
            }
            weatherEmbed.addField("Température min", cwd.getMainData().getTempMin() + "°C", true);
            weatherEmbed.addField("Température max", cwd.getMainData().getTempMax() + "°C", true);
            if (cwd.getWindData() != null && cwd.getWindData().hasSpeed()) {
                weatherEmbed.addField("Vent", cwd.getWindData().getSpeed() + " m/s", true);
            } else {
                weatherEmbed.addField("Vent", "Aucune donnée", true);
            }
            slashCommandEvent.replyEmbeds(weatherEmbed.build()).queue();

        } catch (Exception e) {
            EmbedCrafter errorEmbed = new EmbedCrafter();

            errorEmbed.setTitle("Une erreur est survenue", "https://openweathermap.org")
                    .addField("Message d'erreur", "» " + e.getMessage(), true)
                    .setColor(15158332);
            slashCommandEvent.replyEmbeds(errorEmbed.build()).queue();
            bot.getErrorHandler().handleException(e);
        }
    }
}
