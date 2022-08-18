package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class CommandVersion {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandVersion(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name = "version", description = "Affiche les informations sur la version du BOT", help = ".version", example = ".version", guildOnly = false)
    private void version(TextChannel channel, SlashCommandEvent slashCommandEvent) {
        try {
            EmbedCrafter embedCrafter = new EmbedCrafter();
            embedCrafter.setTitle("BouloBOT by MAXOUXAX • Amazingly powerful.", bot.getConfigurationManager().getStringValue("websiteUrl"))
                    .setColor(3447003)
                    .addField("Je suis en version", bot.getVersion(), true)
                    .addField("Je gère", commandMap.getDiscordCommands().size() + " commandes Discord", true)
                    .addField("Je gère", commandMap.getTwitchCommands().size() + " commandes Twitch", true)
                    .addField("J'ai souhaité la bienvenue à", commandMap.getUserIds().size() + " viewers", true);
            slashCommandEvent.replyEmbeds(embedCrafter.build()).queue();
        } catch (Exception e) {
            bot.getErrorHandler().handleException(e);
            slashCommandEvent.reply("An error occured. > " + e.getMessage()).queue();
        }
    }

}
