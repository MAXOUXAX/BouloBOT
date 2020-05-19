package net.maxouxax.boulobot.commands.register.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.maxouxax.boulobot.BOT;
import net.maxouxax.boulobot.commands.Command;
import net.maxouxax.boulobot.commands.CommandMap;

public class CommandVersion {

    private final BOT bot;
    private final CommandMap commandMap;

    public CommandVersion(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.bot = BOT.getInstance();
    }

    @Command(name="version",type= Command.ExecutorType.USER,description="Affiche les informations sur la version du BOT", help = ".version", example = ".version")
    private void version(MessageChannel channel){
        try{
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("BouloBOT by MAXOUXAX • Amazingly powerful.", bot.getConfigurationManager().getStringValue("websiteUrl"));
            builder.setColor(3447003);
            builder.addField("Je suis en version", bot.getVersion(), true);
            builder.addField("Je gère", commandMap.getDiscordCommands().size()+" commandes Discord", true);
            builder.addField("Je gère", commandMap.getTwitchCommands().size()+" commandes Twitch", true);
            builder.addField("J'ai souhaité la bienvenue à", commandMap.getUserIds().size()+" viewers", true);
            channel.sendMessage(builder.build()).queue();
        }catch (Exception e) {
            e.printStackTrace();
            channel.sendMessage("An error occured. > " + e.getMessage()).queue();
        }
    }

}
