package me.maxouxax.boulobot.commands.register.discord;

import me.maxouxax.boulobot.BOT;
import me.maxouxax.boulobot.commands.Command;
import me.maxouxax.boulobot.commands.CommandMap;
import me.maxouxax.boulobot.commands.slashannotations.Option;
import me.maxouxax.boulobot.util.EmbedCrafter;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CommandEmbed {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandEmbed(CommandMap commandMap){
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Option(name = "titre", description = "Titre de l'embed", isRequired = true, type = OptionType.STRING)
    @Option(name = "description", description = "Description de l'embed", isRequired = true, type = OptionType.STRING)
    @Option(name = "lien-de-limage", description = "Image de l'embed", isRequired = false, type = OptionType.STRING)
    @Command(name="embed",power = 100,help = ".embed <titre>-²<description>-²<image (url)>",example = ".embed Ceci est une annonce-²Juste pour vous dire que les bananes c'est assez bon mais que la raclette reste au dessus.-²https://lien-de-l-image.fr/image32.png")
    public void embed(User user, TextChannel textChannel, SlashCommandEvent slashCommandEvent) {
        String title = slashCommandEvent.getOption("titre").getAsString();
        String description = slashCommandEvent.getOption("description").getAsString();
        String image = slashCommandEvent.getOption("lien-de-limage").getAsString();
        EmbedCrafter embedCrafter = new EmbedCrafter();
        embedCrafter.setTitle(title, "https://lyor.in/twitch")
                .setColor(15844367)
                .setDescription(description);
        if (!image.equals("")) {
            embedCrafter.setImageUrl(image);
        }
        slashCommandEvent.replyEmbeds(embedCrafter.build()).queue();
    }

}
