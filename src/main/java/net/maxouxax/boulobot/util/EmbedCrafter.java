package net.maxouxax.boulobot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.maxouxax.boulobot.BOT;

import java.time.LocalDate;
import java.util.List;

public class EmbedCrafter {

    public static EmbedBuilder craftEmbedBuilder(String title, int color, String description, List<MessageEmbed.Field> fields, String thumbnailUrl, String imageUrl){
        BOT bot = BOT.getInstance();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setFooter(TextFormatter.asDate(bot.getConfigurationManager().getStringValue("embedFooter")), bot.getConfigurationManager().getStringValue("embedIconUrl"));
        embedBuilder.setColor(color);
        embedBuilder.setTimestamp(new LocalDate());
        embedBuilder.setDescription(description);
        fields.forEach(embedBuilder::addField);
        embedBuilder.setThumbnail(thumbnailUrl);
        embedBuilder.setImage(imageUrl);
    }

}
