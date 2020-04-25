package net.maxouxax.boulobot.commands.register.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;
import net.maxouxax.boulobot.util.Reference;

public class CommandEmbed {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandEmbed(BOT bot, CommandMap commandMap){
        this.bot = bot;
        this.commandMap = commandMap;
    }

    @Command(name="embed",type = Command.ExecutorType.ALL,power = 100,help = ".embed <titre>-²<description>-²<image (url)>",example = ".embed Ceci est une annonce-²Juste pour vous dire que les bananes c'est assez bon mais que la raclette reste au dessus.-²https://lien-de-l-image.fr/image32.png")
    public void embed(User user, TextChannel textChannel, String[] args) {
        try {
            EmbedBuilder em = commandMap.getHelpEmbed("embed");
            if (args.length == 0) {
                textChannel.sendMessage(em.build()).queue();
            } else {
                StringBuilder str = new StringBuilder();

                for (String arg : args) {
                    str.append(arg).append(" ");
                }

                if (!str.toString().contains("-²")) {
                    textChannel.sendMessage(em.build()).queue();
                    return;
                }

                String[] argsReal = str.toString().split("-²");

                String title = argsReal[0];

                String description = "Aucune description n'a été fournie !";
                if (argsReal.length >= 2) {
                    description = argsReal[1];
                }

                String imageURL = null;
                if (argsReal.length == 3) {
                    imageURL = argsReal[2];
                }

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(title, "https://twitch.lyorine.com");
                embedBuilder.setFooter(Reference.EmbedFooter.asDate(), Reference.EmbedIcon.getString());
                embedBuilder.setColor(15844367);
                embedBuilder.setDescription(description);
                if (imageURL != null) {
                    embedBuilder.setImage(imageURL);
                }
                textChannel.sendMessage(embedBuilder.build()).queue();
            }
        }catch (Exception e){
            bot.getErrorHandler().handleException(e);
        }
    }

}
